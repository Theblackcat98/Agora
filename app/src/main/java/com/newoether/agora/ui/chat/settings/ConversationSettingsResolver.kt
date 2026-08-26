package com.newoether.agora.ui.chat.settings

import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.settings.ModelSettingsPatch

data class SettingFieldState<T>(
    val value: T,
    val origin: SettingOrigin,
    val isOverridden: Boolean = origin == SettingOrigin.OVERRIDDEN,
)

data class ResolvedConversationFields(
    val contextWindow: SettingFieldState<Int>,
    val temperature: SettingFieldState<Float?>,
    val maxTokens: SettingFieldState<Int?>,
    val topP: SettingFieldState<Float?>,
    val frequencyPenalty: SettingFieldState<Float?>,
    val presencePenalty: SettingFieldState<Float?>,
    val thinkingEnabled: SettingFieldState<Boolean>,
    val thinkingLevel: SettingFieldState<String?>,
    val thinkingBudgetEnabled: SettingFieldState<Boolean>,
    val thinkingBudgetTokens: SettingFieldState<Int?>,
    val codeExecutionEnabled: SettingFieldState<Boolean>,
    val googleSearchEnabled: SettingFieldState<Boolean>,
    val openAiWebSearchEnabled: SettingFieldState<Boolean>,
    val openAiServiceTierEnabled: SettingFieldState<Boolean>,
    val openAiServiceTier: SettingFieldState<String?>,
    val webSearchEnabled: SettingFieldState<Boolean>,
    val shellEnabled: SettingFieldState<Boolean>,
)

object ConversationSettingsResolver {

    private fun <T> resolveField(
        convVal: T?,
        modelVal: T?,
        providerVal: T?,
        globalVal: T,
    ): SettingFieldState<T> {
        return when {
            convVal != null -> SettingFieldState(convVal, SettingOrigin.OVERRIDDEN)
            modelVal != null -> SettingFieldState(modelVal, SettingOrigin.INHERITED_MODEL)
            providerVal != null -> SettingFieldState(providerVal, SettingOrigin.INHERITED_PROVIDER)
            else -> SettingFieldState(globalVal, SettingOrigin.INHERITED_GLOBAL)
        }
    }

    private fun <T> resolveNullableField(
        convVal: T?,
        modelVal: T?,
        providerVal: T?,
        globalVal: T?,
    ): SettingFieldState<T?> {
        return when {
            convVal != null -> SettingFieldState(convVal, SettingOrigin.OVERRIDDEN)
            modelVal != null -> SettingFieldState(modelVal, SettingOrigin.INHERITED_MODEL)
            providerVal != null -> SettingFieldState(providerVal, SettingOrigin.INHERITED_PROVIDER)
            else -> SettingFieldState(globalVal, SettingOrigin.INHERITED_GLOBAL)
        }
    }

    fun resolve(
        conversationPatch: ModelSettingsPatch?,
        modelPatch: ModelSettingsPatch?,
        providerPatch: ModelSettingsPatch?,
        globalPatch: ModelSettingsPatch,
        capabilities: ModelCapabilities,
    ): ResolvedConversationFields {
        val conv = conversationPatch ?: ModelSettingsPatch()
        val model = modelPatch ?: ModelSettingsPatch()
        val provider = providerPatch ?: ModelSettingsPatch()

        return ResolvedConversationFields(
            contextWindow = resolveField(
                conv.contextWindow,
                model.contextWindow,
                provider.contextWindow,
                globalPatch.contextWindow ?: capabilities.maxContextTokens,
            ),
            temperature = resolveNullableField(
                conv.temperature,
                model.temperature,
                provider.temperature,
                globalPatch.temperature,
            ),
            maxTokens = resolveNullableField(
                conv.maxTokens,
                model.maxTokens,
                provider.maxTokens,
                globalPatch.maxTokens,
            ),
            topP = resolveNullableField(
                conv.topP,
                model.topP,
                provider.topP,
                globalPatch.topP,
            ),
            frequencyPenalty = resolveNullableField(
                conv.frequencyPenalty,
                model.frequencyPenalty,
                provider.frequencyPenalty,
                globalPatch.frequencyPenalty,
            ),
            presencePenalty = resolveNullableField(
                conv.presencePenalty,
                model.presencePenalty,
                provider.presencePenalty,
                globalPatch.presencePenalty,
            ),
            thinkingEnabled = resolveField(
                conv.thinkingEnabled,
                model.thinkingEnabled,
                provider.thinkingEnabled,
                globalPatch.thinkingEnabled ?: false,
            ),
            thinkingLevel = resolveNullableField(
                conv.thinkingLevel,
                model.thinkingLevel,
                provider.thinkingLevel,
                globalPatch.thinkingLevel,
            ),
            thinkingBudgetEnabled = resolveField(
                conv.thinkingBudgetEnabled,
                model.thinkingBudgetEnabled,
                provider.thinkingBudgetEnabled,
                globalPatch.thinkingBudgetEnabled ?: false,
            ),
            thinkingBudgetTokens = resolveNullableField(
                conv.thinkingBudgetTokens,
                model.thinkingBudgetTokens,
                provider.thinkingBudgetTokens,
                globalPatch.thinkingBudgetTokens,
            ),
            codeExecutionEnabled = resolveField(
                conv.codeExecutionEnabled,
                model.codeExecutionEnabled,
                provider.codeExecutionEnabled,
                globalPatch.codeExecutionEnabled ?: false,
            ),
            googleSearchEnabled = resolveField(
                conv.googleSearchEnabled,
                model.googleSearchEnabled,
                provider.googleSearchEnabled,
                globalPatch.googleSearchEnabled ?: false,
            ),
            openAiWebSearchEnabled = resolveField(
                conv.openAiWebSearchEnabled,
                model.openAiWebSearchEnabled,
                provider.openAiWebSearchEnabled,
                globalPatch.openAiWebSearchEnabled ?: true,
            ),
            openAiServiceTierEnabled = resolveField(
                conv.openAiServiceTierEnabled,
                model.openAiServiceTierEnabled,
                provider.openAiServiceTierEnabled,
                globalPatch.openAiServiceTierEnabled ?: false,
            ),
            openAiServiceTier = resolveNullableField(
                conv.openAiServiceTier,
                model.openAiServiceTier,
                provider.openAiServiceTier,
                globalPatch.openAiServiceTier,
            ),
            webSearchEnabled = resolveField(
                conv.webSearchEnabled,
                model.webSearchEnabled,
                provider.webSearchEnabled,
                globalPatch.webSearchEnabled ?: true,
            ),
            shellEnabled = resolveField(
                conv.shellEnabled,
                model.shellEnabled,
                provider.shellEnabled,
                globalPatch.shellEnabled ?: true,
            ),
        )
    }
}
