package com.newoether.agora.model.settings

import com.newoether.agora.model.ModelId
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.profile.ModelProfile

/**
 * Authoritative, immutable, fully resolved runtime settings object for one model in one context.
 */
data class EffectiveModelSettings(
    val modelId: ModelId,
    val profile: ModelProfile,
    val contextWindow: Int,
    val temperature: Float?,
    val maxTokens: Int?,
    val topP: Float?,
    val frequencyPenalty: Float?,
    val presencePenalty: Float?,
    val thinkingEnabled: Boolean,
    val thinkingBudgetEnabled: Boolean,
    val thinkingBudgetTokens: Int?,
    val thinkingLevel: String?,
    val codeExecutionEnabled: Boolean,
    val googleSearchEnabled: Boolean,
    val openAiWebSearchEnabled: Boolean,
    val openAiServiceTierEnabled: Boolean,
    val openAiServiceTier: String?,
    val webSearchEnabled: Boolean,
    val shellEnabled: Boolean,
) {
    val capabilities: ModelCapabilities get() = profile.capabilities

    fun toLegacy(): com.newoether.agora.data.ConversationSettings = com.newoether.agora.data.ConversationSettings(
        contextWindow = contextWindow,
        temperature = temperature,
        maxTokens = maxTokens,
        topP = topP,
        frequencyPenalty = frequencyPenalty,
        presencePenalty = presencePenalty,
        codeExecutionEnabled = codeExecutionEnabled,
        googleSearchEnabled = googleSearchEnabled,
        openAiWebSearchEnabled = openAiWebSearchEnabled,
        thinkingEnabled = thinkingEnabled,
        thinkingLevel = thinkingLevel,
        thinkingBudgetEnabled = thinkingBudgetEnabled,
        thinkingBudgetTokens = thinkingBudgetTokens,
        openAiServiceTierEnabled = openAiServiceTierEnabled,
        openAiServiceTier = openAiServiceTier,
        webSearchEnabled = webSearchEnabled,
        shellEnabled = shellEnabled,
    )
}
