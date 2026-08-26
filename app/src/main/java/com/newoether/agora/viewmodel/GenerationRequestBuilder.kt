package com.newoether.agora.viewmodel

import android.content.Context
import com.newoether.agora.R
import com.newoether.agora.data.ConversationSettings
import com.newoether.agora.data.MemoryManager
import com.newoether.agora.data.SkillManager
import com.newoether.agora.data.PredefinedVariables
import com.newoether.agora.data.SystemPromptEntry
import com.newoether.agora.data.providerDisplayName
import com.newoether.agora.data.isResponsesApiEnabledForProvider
import com.newoether.agora.data.local.ChatEntity
import com.newoether.agora.data.repository.ConversationRepository
import com.newoether.agora.data.repository.SettingsRepository
import com.newoether.agora.model.ModelId
import com.newoether.agora.model.ContextBudget
import com.newoether.agora.model.OpenAiServiceTiers
import com.newoether.agora.model.apiModelName
import com.newoether.agora.model.settings.EffectiveModelSettings
import com.newoether.agora.model.settings.ModelSettingsPatch
import com.newoether.agora.model.settings.ModelSettingsResolver
import com.newoether.agora.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Stateless builder for the LLM generation request. Extracted from ChatViewModel.
 * Reads configuration singletons only; holds NO mutable UI state.
 */
class GenerationRequestBuilder(
    private val settings: SettingsRepository,
    private val convRepo: ConversationRepository,
    private val memoryManager: MemoryManager,
    private val skillManager: SkillManager,
    private val providerRegistry: ProviderRegistry,
    private val ragManager: RagManager,
    private val appContext: Context,
    // This remains a StateFlow because buildEffectiveConversationSettings reads its current value.
    private val pendingConversationSettings: StateFlow<ConversationSettings?>,
    // resolveProviderKey uses this callback to emit snackbar messages.
    private val onSnackbar: (String) -> Unit,
) {
    data class ProviderKey(val providerName: String, val apiKey: String)

    /** Resolves the active provider+key for [modelId] and verifies configuration.
     *  Emits a snackbar and returns null when the provider is not configured. */
    internal fun resolveProviderKey(modelId: String): ProviderKey? {
        val providerName = providerRegistry.providerForModel(modelId)
        val activeKey = settings.resolveActiveKey(providerName) ?: ""
        if (!providerRegistry.isConfigured(providerName, activeKey)) {
            val displayProviderName = providerDisplayName(
                providerName,
                settings.customProviders.value,
            )
            onSnackbar(
                appContext.getString(
                    R.string.no_api_key_for_provider,
                    displayProviderName,
                )
            )
            return null
        }
        return ProviderKey(providerName, activeKey)
    }

    private fun resolveTranscriptionProviderName(model: String?): String =
        model?.let { providerRegistry.providerForModel(it) } ?: ""

    private fun resolveTranscriptionModelId(model: String?): String =
        model?.let {
            ModelId.parse(providerRegistry.canonicalModelId(it)).modelName
        } ?: ""

    private fun resolveTranscriptionApiKey(model: String?): String {
        model ?: return ""
        val providerName = providerRegistry.providerForModel(model)
        if (providerName == Constants.PROVIDER_LOCAL) return ""
        return settings.resolveActiveKey(providerName) ?: ""
    }

    private fun resolveTranscriptionBaseUrl(model: String?): String? {
        model ?: return null
        return providerRegistry.getEffectiveBaseUrl(providerRegistry.providerForModel(model))
    }

    // Image generation reuses the selected model's provider credentials (mirrors transcription).
    private fun resolveImageGenModelId(model: String?): String =
        model?.let {
            ModelId.parse(providerRegistry.canonicalModelId(it)).apiModelName
        } ?: ""

    private fun resolveImageGenApiKey(model: String?): String {
        model ?: return ""
        val providerName = providerRegistry.providerForModel(model)
        if (providerName == Constants.PROVIDER_LOCAL) return ""
        return settings.resolveActiveKey(providerName) ?: ""
    }

    private fun resolveImageGenBaseUrl(model: String?): String {
        model ?: return ""
        return providerRegistry.getEffectiveBaseUrl(providerRegistry.providerForModel(model)) ?: ""
    }

    private val modelSettingsResolver = ModelSettingsResolver(
        globalDefaults = {
            ModelSettingsPatch(
                contextWindow = settings.maxContextWindow.value,
                temperature = settings.defaultTemperature.value,
                maxTokens = settings.defaultMaxTokens.value,
                topP = settings.defaultTopP.value,
                frequencyPenalty = settings.defaultFrequencyPenalty.value,
                presencePenalty = settings.defaultPresencePenalty.value,
                codeExecutionEnabled = settings.codeExecutionEnabled.value,
                googleSearchEnabled = settings.googleSearchEnabled.value,
                openAiWebSearchEnabled = true,
                thinkingEnabled = settings.thinkingEnabled.value,
                thinkingLevel = settings.thinkingLevel.value,
                thinkingBudgetEnabled = settings.thinkingBudgetEnabled.value,
                thinkingBudgetTokens = settings.thinkingBudgetTokens.value,
                openAiServiceTierEnabled = settings.openAiServiceTierEnabled.value,
                openAiServiceTier = settings.openAiServiceTier.value,
                webSearchEnabled = settings.webSearchEnabled.value,
                shellEnabled = settings.shellEnabled.value,
            )
        },
        providerDefaults = { providerName ->
            settings.providerSettings.value[providerName]
        },
        modelDefaults = { modelKey ->
            settings.modelSettings.value[modelKey]
        },
        conversationOverrides = { convId ->
            val legacy = settings.conversationSettings.value[convId]
                ?: pendingConversationSettings.value
            ModelSettingsPatch.fromLegacy(legacy)
        }
    )

    fun resolveEffectiveModelSettings(
        modelId: String,
        conversationId: String,
        conversationPatchOverride: ModelSettingsPatch? = null,
        overlayPatch: ModelSettingsPatch? = null,
    ): EffectiveModelSettings {
        val canonicalModel = providerRegistry.canonicalModelId(modelId)
        return modelSettingsResolver.resolve(
            modelId = canonicalModel,
            conversationId = conversationId,
            conversationPatchOverride = conversationPatchOverride,
            overlayPatch = overlayPatch,
        )
    }

    fun buildEffectiveConversationSettings(
        conversationId: String,
        modelId: String = settings.selectedModel.value,
    ): ConversationSettings =
        resolveEffectiveModelSettings(modelId, conversationId).toLegacy()

    /**
     * Captures every setting owned by one generation before its Room graph is admitted.
     *
     * The returned value contains only immutable/copy-on-capture data. Later settings edits can
     * affect the next Run, but not Compact preflight, Provider passes, or tool continuation for
     * this Run.
     */
    internal suspend fun captureAdmissionSnapshot(
        conversationId: String,
        runId: String,
        modelId: String,
        conversationOverride: ChatEntity? = null,
        resolvedPromptOverride: ResolvedPrompt? = null,
    ): GenerationAdmissionSnapshot {
        val selectedModelId = providerRegistry.canonicalModelId(modelId)
        val providerName = providerRegistry.providerForModel(selectedModelId)
        val effectiveSettings = resolveEffectiveModelSettings(selectedModelId, conversationId)
        val frozenKey = settings.awaitActiveKey(providerName).orEmpty()
        check(providerRegistry.isConfigured(providerName, frozenKey)) {
            "Provider is no longer configured: $providerName"
        }
        val (baseConfig, context) = buildGenerationPair(
            providerName = providerName,
            modelId = selectedModelId,
            activeKey = frozenKey,
            resolvedSystemPrompt = null,
            resolvedUserPrepend = null,
            resolvedUserPostpend = null,
            effectiveSettings = effectiveSettings,
            currentId = conversationId,
        )
        val compactModel = settings.contextCompactModel.value
            ?.takeIf(String::isNotBlank)
            ?.let(providerRegistry::canonicalModelId)
            ?: selectedModelId
        val compactProviderName = providerRegistry.providerForModel(compactModel)
        val providerInstances = providerRegistry.all.toMap()
        val compactKey = if (compactProviderName == providerName) {
            frozenKey
        } else {
            settings.resolveActiveKey(compactProviderName).orEmpty()
        }
        val effectiveCompactSettings = resolveEffectiveModelSettings(
            modelId = compactModel,
            conversationId = conversationId,
            overlayPatch = COMPACTION_OVERLAY_PATCH,
        )
        val (compactGenerationConfig, compactGenerationContext) = buildGenerationPair(
            providerName = compactProviderName,
            modelId = compactModel,
            activeKey = compactKey,
            resolvedSystemPrompt = settings.contextCompactPrompt.value,
            resolvedUserPrepend = null,
            resolvedUserPostpend = null,
            effectiveSettings = effectiveCompactSettings,
            currentId = conversationId,
        )
        val automaticCompact = AutomaticCompactConfig(
            enabled = settings.contextCompactEnabled.value,
            thresholdPercent = settings.contextCompactThresholdPercent.value,
            request = CompactRequest(
                model = compactModel,
                prompt = settings.contextCompactPrompt.value,
                retainLogicalMessages = settings.contextCompactRetainCount.value,
            ),
            providerName = compactProviderName,
            apiKey = compactKey,
            baseUrl = providerRegistry.getEffectiveBaseUrl(compactProviderName),
            responsesApiEnabled = isResponsesApiEnabledForProvider(
                providerName = compactProviderName,
                builtInOpenAiEnabled = settings.openAiResponsesApiEnabled.value,
                customProviders = settings.customProviders.value,
            ),
            provider = providerInstances[compactProviderName],
            configured = providerRegistry.isConfigured(compactProviderName, compactKey),
            generationConfig = compactGenerationConfig,
            providerInstances = providerInstances,
            generationContext = compactGenerationContext.copy(
                webSearchApiKeys = context.webSearchApiKeys.toMap(),
                shellDevices = context.shellDevices.toList(),
            ),
        )
        val titleGenerationEnabled = settings.titleGenerationEnabled.value
        val promptSettings = capturePromptSettings()
        val resolved = resolvedPromptOverride ?: buildEffectiveSystemPrompt(
            currentId = conversationId,
            activeModel = selectedModelId,
            conversationOverride = conversationOverride,
            promptSettings = promptSettings,
        )
        return GenerationAdmissionSnapshot(
            conversationId = conversationId,
            runId = runId,
            selectedModelId = selectedModelId,
            config = baseConfig.copy(
                effectiveSystemPrompt = resolved.systemPrompt,
                userPrepend = resolved.userPrepend,
                userPostpend = resolved.userPostpend,
            ),
            context = context.copy(
                webSearchApiKeys = context.webSearchApiKeys.toMap(),
                shellDevices = context.shellDevices.toList(),
            ),
            providerInstances = providerInstances,
            automaticCompact = automaticCompact.copy(
                userPrepend = resolved.userPrepend,
                userPostpend = resolved.userPostpend,
            ),
            titleGenerationEnabled = titleGenerationEnabled,
        )
    }

    /**
     * Captures only the system-prompt and tool-definition inputs needed by the context indicator.
     * Unlike Run admission this must work before a Provider has a usable key or endpoint.
     */
    internal suspend fun captureContextProjectionSnapshot(
        conversationId: String,
        modelId: String,
        systemPromptIdOverride: String? = null,
    ): GenerationContextProjectionSnapshot {
        val selectedModelId = providerRegistry.canonicalModelId(modelId)
        val providerName = providerRegistry.providerForModel(selectedModelId)
        val effectiveSettings = resolveEffectiveModelSettings(selectedModelId, conversationId)
        val (baseConfig, context) = buildGenerationPair(
            providerName = providerName,
            modelId = selectedModelId,
            activeKey = "",
            resolvedSystemPrompt = null,
            resolvedUserPrepend = null,
            resolvedUserPostpend = null,
            effectiveSettings = effectiveSettings,
            currentId = conversationId,
        )
        val resolved = buildEffectiveSystemPrompt(
            currentId = conversationId,
            activeModel = selectedModelId,
            conversationOverride = null,
            promptSettings = capturePromptSettings(),
            systemPromptIdOverride = systemPromptIdOverride,
        )
        return GenerationContextProjectionSnapshot(
            config = baseConfig.copy(
                effectiveSystemPrompt = resolved.systemPrompt,
                userPrepend = resolved.userPrepend,
                userPostpend = resolved.userPostpend,
            ),
            context = context.copy(
                webSearchApiKeys = context.webSearchApiKeys.toMap(),
                shellDevices = context.shellDevices.toList(),
            ),
        )
    }

    private fun buildGenerationPair(
        providerName: String,
        modelId: String,
        activeKey: String,
        resolvedSystemPrompt: String?,
        resolvedUserPrepend: String?,
        resolvedUserPostpend: String?,
        effectiveSettings: EffectiveModelSettings,
        currentId: String
    ): Pair<GenerationConfig, GenerationContext> {
        val imageGenModel = settings.imageGenModel.value
        val transcriptionModel = settings.imageTranscriptionModel.value
        val skillReadAccess = settings.accessSkills.value
        val skillModifyAccess = skillReadAccess && settings.accessSkillsModify.value
        val skillCatalog = if (skillReadAccess) skillManager.catalog() else ""
        val effectiveSystemPromptWithSkills = listOfNotNull(
            resolvedSystemPrompt?.takeIf(String::isNotBlank),
            skillCatalog.takeIf(String::isNotBlank),
        ).joinToString("\n\n").ifBlank { null }
        val responsesApiEnabled = isResponsesApiEnabledForProvider(
            providerName = providerName,
            builtInOpenAiEnabled = settings.openAiResponsesApiEnabled.value,
            customProviders = settings.customProviders.value,
        )
        val config = GenerationConfig(
            providerName = providerName,
            modelId = ModelId.parse(providerRegistry.canonicalModelId(modelId)).modelName,
            apiKey = activeKey,
            effectiveSystemPrompt = effectiveSystemPromptWithSkills,
            maxContextWindow = effectiveSettings.contextWindow,
            codeExecutionEnabled = effectiveSettings.codeExecutionEnabled,
            googleSearchEnabled = effectiveSettings.googleSearchEnabled,
            thinkingEnabled = effectiveSettings.thinkingEnabled,
            thinkingLevel = com.newoether.agora.model.ThinkingLevels.normalize(effectiveSettings.thinkingLevel),
            thinkingBudgetEnabled = effectiveSettings.thinkingBudgetEnabled,
            thinkingBudgetTokens = effectiveSettings.thinkingBudgetTokens
                ?: com.newoether.agora.model.ThinkingLevels.DefaultBudgetTokens,
            openAiServiceTier = OpenAiServiceTiers.requestValue(
                enabled = effectiveSettings.openAiServiceTierEnabled,
                value = effectiveSettings.openAiServiceTier,
                responsesApiEnabled = responsesApiEnabled,
            ),
            responsesApiEnabled = responsesApiEnabled,
            openAiWebSearchEnabled =
                effectiveSettings.openAiWebSearchEnabled && responsesApiEnabled,
            baseUrl = providerRegistry.getEffectiveBaseUrl(providerName),
            userPrepend = resolvedUserPrepend,
            userPostpend = resolvedUserPostpend,
            temperature = effectiveSettings.temperature,
            maxTokens = effectiveSettings.maxTokens,
            topP = effectiveSettings.topP,
            frequencyPenalty = effectiveSettings.frequencyPenalty,
            presencePenalty = effectiveSettings.presencePenalty
        )
        val genCtx = GenerationContext(
            conversationId = currentId,
            accessSavedMemories = settings.accessSavedMemories.value,
            accessActiveMemory = settings.accessActiveMemory.value,
            skillReadAccess = skillReadAccess,
            skillModifyAccess = skillModifyAccess,
            skillCatalog = skillCatalog,
            accessPastConversations = settings.accessPastConversations.value,
            modelSearchMethod = settings.modelSearchMethod.value,
            activeEmbeddingConfig = ragManager.activeEmbeddingModel.value,
            embeddingApiKey = ragManager.resolveEmbeddingApiKey() ?: "",
            ragThreshold = settings.ragThreshold.value,
            searchMatchLimit = settings.searchMatchLimit.value,
            searchContextWindow = settings.searchContextWindow.value,
            webSearchEnabled = effectiveSettings.webSearchEnabled,
            webSearchApiKeys = settings.webSearchApiKeys.value,
            webSearchProvider = settings.webSearchProvider.value,
            webSearchNumResults = settings.webSearchNumResults.value,
            webSearchBaseUrl = settings.webSearchBaseUrl.value,
            imageGenEnabled = settings.imageGenEnabled.value && imageGenModel?.contains(":") == true,
            imageGenApiKey = resolveImageGenApiKey(imageGenModel),
            imageGenBaseUrl = resolveImageGenBaseUrl(imageGenModel),
            imageGenModel = resolveImageGenModelId(imageGenModel),
            imageGenSize = settings.imageGenSize.value,
            automationToolsEnabled = settings.automationToolsEnabled.value,
            shellEnabled = effectiveSettings.shellEnabled,
            shellDevices = settings.shellDevices.value,
            sandboxEnabled = settings.sandboxEnabled.value,
            sandboxSharedStorageEnabled = settings.sandboxSharedStorageEnabled.value,
            // Keyed on THIS generation's model, not the UI's currently-selected one — a queued
            // or parallel-conversation generation must not inherit another conversation's model.
            imageTranscriptionEnabled =
                settings.imageTranscriptionEnabled.value &&
                    settings.imageTranscriptionEnabledModels.value.contains(modelId),
            imageTranscriptionModel = transcriptionModel,
            imageTranscriptionBatchSize = settings.imageTranscriptionBatchSize.value,
            imageTranscriptionPrompt = settings.imageTranscriptionPrompt.value,
            transcriptionProviderName = resolveTranscriptionProviderName(transcriptionModel),
            transcriptionModelId = resolveTranscriptionModelId(transcriptionModel),
            transcriptionApiKey = resolveTranscriptionApiKey(transcriptionModel),
            transcriptionBaseUrl = resolveTranscriptionBaseUrl(transcriptionModel)
        )
        return Pair(config, genCtx)
    }

    private data class PromptSettingsSnapshot(
        val includeActiveMemory: Boolean,
        val activeSystemPromptId: String?,
        val systemPrompts: List<SystemPromptEntry>,
    )

    private fun capturePromptSettings() = PromptSettingsSnapshot(
        includeActiveMemory = settings.accessActiveMemory.value,
        activeSystemPromptId = settings.activeSystemPromptId.value,
        systemPrompts = settings.systemPrompts.value.toList(),
    )

    data class ResolvedPrompt(
        val systemPrompt: String?,
        val userPrepend: String?,
        val userPostpend: String?
    )

    private suspend fun buildEffectiveSystemPrompt(
        currentId: String,
        activeModel: String,
        conversationOverride: ChatEntity?,
        promptSettings: PromptSettingsSnapshot,
        systemPromptIdOverride: String? = null,
    ): ResolvedPrompt = withContext(Dispatchers.Default) {
        coroutineScope {
            val includeActiveMemory = promptSettings.includeActiveMemory
            // Room and the optional memory-file read are independent. Running both immediately avoids
            // adding their latencies serially to the visible Sending phase.
            val conversationDeferred = async {
                conversationOverride ?: convRepo.getConversation(currentId)
            }
            val activeMemoryDeferred = async(Dispatchers.IO) {
                if (includeActiveMemory) memoryManager.getActiveMemory() else ""
            }
            val conversation = conversationDeferred.await()
            val targetPromptId = systemPromptIdOverride
                ?: conversation?.systemPromptId
                ?: promptSettings.activeSystemPromptId
            val entry = promptSettings.systemPrompts.find { it.id == targetPromptId }
            val activeMemory = activeMemoryDeferred.await()
            val modelId = ModelId.parse(providerRegistry.canonicalModelId(activeModel)).modelName

            val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
            val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val now = java.util.Date()

            val runtimeValues = mapOf(
                PredefinedVariables.TIME to sdf.format(now),
                PredefinedVariables.DATE to dateSdf.format(now),
                PredefinedVariables.SENT_TIME to sdf.format(now),
                PredefinedVariables.SENT_DATE to dateSdf.format(now),
                PredefinedVariables.MODEL_ID to modelId,
                PredefinedVariables.ACTIVE_MEMORY to if (includeActiveMemory && activeMemory.isNotBlank()) activeMemory else ""
            )

            if (entry != null) {
                val systemItems = entry.resolvedSystemItems
                // Prepend/postpend: {sent_time}/{sent_date} stay as placeholders resolved per-message in applyUserTemplate
                val perMsgValues = runtimeValues.filterKeys { it !in PredefinedVariables.PER_MESSAGE_VARS }
                return@coroutineScope ResolvedPrompt(
                    systemPrompt = PredefinedVariables.compile(systemItems, runtimeValues).ifBlank { null },
                    userPrepend = PredefinedVariables.compile(entry.userPrependItems, perMsgValues, emptyMap()).ifBlank { null },
                    userPostpend = PredefinedVariables.compile(entry.userPostpendItems, perMsgValues, emptyMap()).ifBlank { null }
                )
            }

            ResolvedPrompt(null, null, null)
        }
    }

    companion object {
        val COMPACTION_OVERLAY_PATCH = ModelSettingsPatch(
            thinkingEnabled = false,
            codeExecutionEnabled = false,
            googleSearchEnabled = false,
            openAiWebSearchEnabled = false,
            webSearchEnabled = false,
            shellEnabled = false,
        )
    }
}
