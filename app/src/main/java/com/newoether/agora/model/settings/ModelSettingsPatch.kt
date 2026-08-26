package com.newoether.agora.model.settings

import com.newoether.agora.data.ConversationSettings
import kotlinx.serialization.Serializable

/**
 * Nullable configuration patch representing settings overrides at the global,
 * provider, model, conversation, or runtime overlay level.
 */
@Serializable
data class ModelSettingsPatch(
    val contextWindow: Int? = null,
    val temperature: Float? = null,
    val maxTokens: Int? = null,
    val topP: Float? = null,
    val frequencyPenalty: Float? = null,
    val presencePenalty: Float? = null,
    val thinkingEnabled: Boolean? = null,
    val thinkingBudgetEnabled: Boolean? = null,
    val thinkingBudgetTokens: Int? = null,
    val thinkingLevel: String? = null,
    val codeExecutionEnabled: Boolean? = null,
    val googleSearchEnabled: Boolean? = null,
    val openAiWebSearchEnabled: Boolean? = null,
    val openAiServiceTierEnabled: Boolean? = null,
    val openAiServiceTier: String? = null,
    val webSearchEnabled: Boolean? = null,
    val shellEnabled: Boolean? = null,
) {
    fun isAllNull(): Boolean =
        contextWindow == null && temperature == null && maxTokens == null && topP == null &&
            frequencyPenalty == null && presencePenalty == null &&
            thinkingEnabled == null && thinkingBudgetEnabled == null && thinkingBudgetTokens == null &&
            thinkingLevel == null && codeExecutionEnabled == null && googleSearchEnabled == null &&
            openAiWebSearchEnabled == null && openAiServiceTierEnabled == null && openAiServiceTier == null &&
            webSearchEnabled == null && shellEnabled == null

    /**
     * Merges a child patch over this parent patch. Any non-null field in [child] overrides this patch.
     */
    fun merge(child: ModelSettingsPatch?): ModelSettingsPatch {
        if (child == null || child.isAllNull()) return this
        return ModelSettingsPatch(
            contextWindow = child.contextWindow ?: this.contextWindow,
            temperature = child.temperature ?: this.temperature,
            maxTokens = child.maxTokens ?: this.maxTokens,
            topP = child.topP ?: this.topP,
            frequencyPenalty = child.frequencyPenalty ?: this.frequencyPenalty,
            presencePenalty = child.presencePenalty ?: this.presencePenalty,
            thinkingEnabled = child.thinkingEnabled ?: this.thinkingEnabled,
            thinkingBudgetEnabled = child.thinkingBudgetEnabled ?: this.thinkingBudgetEnabled,
            thinkingBudgetTokens = child.thinkingBudgetTokens ?: this.thinkingBudgetTokens,
            thinkingLevel = child.thinkingLevel ?: this.thinkingLevel,
            codeExecutionEnabled = child.codeExecutionEnabled ?: this.codeExecutionEnabled,
            googleSearchEnabled = child.googleSearchEnabled ?: this.googleSearchEnabled,
            openAiWebSearchEnabled = child.openAiWebSearchEnabled ?: this.openAiWebSearchEnabled,
            openAiServiceTierEnabled = child.openAiServiceTierEnabled ?: this.openAiServiceTierEnabled,
            openAiServiceTier = child.openAiServiceTier ?: this.openAiServiceTier,
            webSearchEnabled = child.webSearchEnabled ?: this.webSearchEnabled,
            shellEnabled = child.shellEnabled ?: this.shellEnabled,
        )
    }

    fun toLegacy(): ConversationSettings = ConversationSettings(
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

    companion object {
        val EMPTY = ModelSettingsPatch()

        fun fromLegacy(legacy: ConversationSettings?): ModelSettingsPatch {
            legacy ?: return EMPTY
            return ModelSettingsPatch(
                contextWindow = legacy.contextWindow,
                temperature = legacy.temperature,
                maxTokens = legacy.maxTokens,
                topP = legacy.topP,
                frequencyPenalty = legacy.frequencyPenalty,
                presencePenalty = legacy.presencePenalty,
                thinkingEnabled = legacy.thinkingEnabled,
                thinkingBudgetEnabled = legacy.thinkingBudgetEnabled,
                thinkingBudgetTokens = legacy.thinkingBudgetTokens,
                thinkingLevel = legacy.thinkingLevel,
                codeExecutionEnabled = legacy.codeExecutionEnabled,
                googleSearchEnabled = legacy.googleSearchEnabled,
                openAiWebSearchEnabled = legacy.openAiWebSearchEnabled,
                openAiServiceTierEnabled = legacy.openAiServiceTierEnabled,
                openAiServiceTier = legacy.openAiServiceTier,
                webSearchEnabled = legacy.webSearchEnabled,
                shellEnabled = legacy.shellEnabled,
            )
        }
    }
}
