package com.newoether.agora.model.settings

import com.newoether.agora.model.ContextBudget
import com.newoether.agora.model.ModelId
import com.newoether.agora.model.OpenAiServiceTiers
import com.newoether.agora.model.profile.ModelProfile
import com.newoether.agora.model.profile.ModelProfileRegistry
import kotlin.math.min

/**
 * Pure settings resolution engine. Implements the 4-tier hierarchy:
 * Global Defaults -> Provider Defaults -> Model Defaults -> Conversation Overrides -> Overlay -> EffectiveModelSettings
 * and applies capability bounds and parameter pruning.
 */
class ModelSettingsResolver(
    private val globalDefaults: () -> ModelSettingsPatch,
    private val providerDefaults: (String) -> ModelSettingsPatch? = { null },
    private val modelDefaults: (String) -> ModelSettingsPatch? = { null },
    private val conversationOverrides: (String) -> ModelSettingsPatch? = { null },
    private val profileLookup: (String) -> ModelProfile = ModelProfileRegistry::resolve,
) {
    /**
     * Resolves the canonical [EffectiveModelSettings] for [modelId].
     */
    fun resolve(
        modelId: String,
        conversationId: String? = null,
        conversationPatchOverride: ModelSettingsPatch? = null,
        overlayPatch: ModelSettingsPatch? = null,
    ): EffectiveModelSettings {
        val profile = profileLookup(modelId)
        val parsedModelId = ModelId.parse(modelId)
        val capabilities = profile.capabilities

        val global = globalDefaults()
        val provider = providerDefaults(parsedModelId.providerName) ?: ModelSettingsPatch.EMPTY
        val model = modelDefaults(parsedModelId.prefixed) ?: ModelSettingsPatch.EMPTY
        val conversation = conversationPatchOverride
            ?: (conversationId?.let(conversationOverrides) ?: ModelSettingsPatch.EMPTY)
        val overlay = overlayPatch ?: ModelSettingsPatch.EMPTY

        val merged = global
            .merge(provider)
            .merge(model)
            .merge(conversation)
            .merge(overlay)

        // 1. Context Window: normalize and clamp to model capabilities
        val rawContext = merged.contextWindow ?: profile.defaultContextWindow
        val normalizedBudget = ContextBudget.normalize(rawContext)
        val contextWindow = min(normalizedBudget, capabilities.maxContextTokens)

        // 2. Temperature: clamp if supported, null if unsupported
        val temperature = if (capabilities.supportsTemperature) {
            val rawTemp = merged.temperature ?: capabilities.temperatureRange?.default ?: 1.0f
            capabilities.temperatureRange?.clamp(rawTemp) ?: rawTemp
        } else {
            null
        }

        // 3. Top-P: clamp if supported, null if unsupported
        val topP = if (capabilities.supportsTopP) {
            val rawTopP = merged.topP ?: capabilities.topPRange?.default ?: 1.0f
            capabilities.topPRange?.clamp(rawTopP) ?: rawTopP
        } else {
            null
        }

        // 4. Frequency Penalty
        val frequencyPenalty = if (capabilities.supportsFrequencyPenalty) {
            val raw = merged.frequencyPenalty ?: capabilities.frequencyPenaltyRange?.default ?: 0.0f
            capabilities.frequencyPenaltyRange?.clamp(raw) ?: raw
        } else {
            null
        }

        // 5. Presence Penalty
        val presencePenalty = if (capabilities.supportsPresencePenalty) {
            val raw = merged.presencePenalty ?: capabilities.presencePenaltyRange?.default ?: 0.0f
            capabilities.presencePenaltyRange?.clamp(raw) ?: raw
        } else {
            null
        }

        // 6. Max output tokens
        val maxTokens = if (capabilities.maxOutputTokens != null) {
            val raw = merged.maxTokens ?: capabilities.maxOutputTokens
            min(raw, capabilities.maxOutputTokens)
        } else {
            merged.maxTokens
        }

        // 7. Thinking / Reasoning
        val thinkingEnabled = capabilities.thinking.supported && (merged.thinkingEnabled == true)
        val thinkingBudgetEnabled = thinkingEnabled && capabilities.thinking.supportsBudget &&
            (merged.thinkingBudgetEnabled == true)
        val thinkingBudgetTokens = if (thinkingBudgetEnabled) {
            val rawBudget = merged.thinkingBudgetTokens ?: capabilities.thinking.budgetRange?.default ?: 1024
            capabilities.thinking.budgetRange?.clamp(rawBudget) ?: rawBudget
        } else {
            null
        }
        val thinkingLevel = if (thinkingEnabled && capabilities.thinking.supportedLevels.isNotEmpty()) {
            merged.thinkingLevel?.takeIf { it in capabilities.thinking.supportedLevels }
                ?: capabilities.thinking.supportedLevels.firstOrNull()
        } else {
            null
        }

        // 8. Tool flags (subject to model capabilities)
        val codeExecutionEnabled = capabilities.supportsCodeExecution && (merged.codeExecutionEnabled == true)
        val googleSearchEnabled = capabilities.supportsNativeSearch && (merged.googleSearchEnabled == true)
        val openAiWebSearchEnabled = capabilities.supportsNativeSearch && (merged.openAiWebSearchEnabled != false)
        val webSearchEnabled = merged.webSearchEnabled ?: true
        val shellEnabled = merged.shellEnabled ?: true

        // 9. Service tier
        val openAiServiceTierEnabled = merged.openAiServiceTierEnabled == true
        val openAiServiceTier = OpenAiServiceTiers.normalize(merged.openAiServiceTier)

        return EffectiveModelSettings(
            modelId = parsedModelId,
            profile = profile,
            contextWindow = contextWindow,
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            frequencyPenalty = frequencyPenalty,
            presencePenalty = presencePenalty,
            thinkingEnabled = thinkingEnabled,
            thinkingBudgetEnabled = thinkingBudgetEnabled,
            thinkingBudgetTokens = thinkingBudgetTokens,
            thinkingLevel = thinkingLevel,
            codeExecutionEnabled = codeExecutionEnabled,
            googleSearchEnabled = googleSearchEnabled,
            openAiWebSearchEnabled = openAiWebSearchEnabled,
            openAiServiceTierEnabled = openAiServiceTierEnabled,
            openAiServiceTier = openAiServiceTier,
            webSearchEnabled = webSearchEnabled,
            shellEnabled = shellEnabled,
        )
    }
}
