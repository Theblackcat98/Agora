package com.newoether.agora.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.newoether.agora.data.ConversationSettings
import com.newoether.agora.data.CustomProviderConfig
import com.newoether.agora.model.ContextBudget
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.profile.ModelProfileRegistry
import com.newoether.agora.viewmodel.ChatViewModel

internal data class ChatEffectiveSettings(
    val codeExecutionEnabled: Boolean,
    val googleSearchEnabled: Boolean,
    val thinkingEnabled: Boolean,
    val thinkingLevel: String,
    val thinkingBudgetEnabled: Boolean,
    val thinkingBudgetTokens: Int,
    val selectedProviderName: String,
    val openAiServiceTierState: OpenAiConversationServiceTierState,
    val openAiWebSearchAvailable: Boolean,
    val openAiWebSearchEnabled: Boolean,
    val webSearchEnabled: Boolean,
    val shellEnabled: Boolean,
    val contextWindow: Int,
    val capabilities: ModelCapabilities,
)

@Composable
internal fun rememberChatEffectiveSettings(
    viewModel: ChatViewModel,
    currentConversationId: String?,
    selectedModel: String,
    customProviders: List<CustomProviderConfig>,
    openAiResponsesApiEnabled: Boolean,
    globalCodeExecution: Boolean,
    globalGoogleSearch: Boolean,
    globalThinkingEnabled: Boolean,
    globalThinkingLevel: String,
    globalThinkingBudgetEnabled: Boolean,
    globalThinkingBudgetTokens: Int,
    globalWebSearch: Boolean,
    globalShell: Boolean,
    maxContextWindow: Int,
): ChatEffectiveSettings {
    val conversationSettings by viewModel.settings.conversationSettings.collectAsState()
    val pendingSettings by viewModel.pendingConversationSettings.collectAsState()

    val convId = currentConversationId
    val convOverride = if (convId != null) conversationSettings[convId] else pendingSettings

    val codeExecutionEnabled = convOverride?.codeExecutionEnabled ?: globalCodeExecution
    val googleSearchEnabled = convOverride?.googleSearchEnabled ?: globalGoogleSearch
    val thinkingEnabled = convOverride?.thinkingEnabled ?: globalThinkingEnabled
    val thinkingLevel = convOverride?.thinkingLevel ?: globalThinkingLevel
    val thinkingBudgetEnabled = convOverride?.thinkingBudgetEnabled ?: globalThinkingBudgetEnabled
    val thinkingBudgetTokens = convOverride?.thinkingBudgetTokens ?: globalThinkingBudgetTokens
    val selectedProviderName = viewModel.getProviderForModel(selectedModel)

    val openAiServiceTierState = openAiConversationServiceTierState(
        viewModel,
        convOverride,
        selectedProviderName,
        openAiResponsesApiEnabled,
        customProviders,
    )
    val openAiWebSearchAvailable = resolveOpenAiNativeSearchAvailability(
        selectedProviderName,
        openAiResponsesApiEnabled,
        customProviders,
    )
    val openAiWebSearchEnabled = convOverride?.openAiWebSearchEnabled ?: true
    val webSearchEnabled = globalWebSearch && (convOverride?.webSearchEnabled ?: true)
    val shellEnabled = globalShell && (convOverride?.shellEnabled ?: true)
    val contextWindow = ContextBudget.normalize(convOverride?.contextWindow ?: maxContextWindow)

    val capabilities = remember(selectedModel) {
        ModelProfileRegistry.resolve(selectedModel).capabilities
    }

    return ChatEffectiveSettings(
        codeExecutionEnabled = codeExecutionEnabled,
        googleSearchEnabled = googleSearchEnabled,
        thinkingEnabled = thinkingEnabled,
        thinkingLevel = thinkingLevel,
        thinkingBudgetEnabled = thinkingBudgetEnabled,
        thinkingBudgetTokens = thinkingBudgetTokens,
        selectedProviderName = selectedProviderName,
        openAiServiceTierState = openAiServiceTierState,
        openAiWebSearchAvailable = openAiWebSearchAvailable,
        openAiWebSearchEnabled = openAiWebSearchEnabled,
        webSearchEnabled = webSearchEnabled,
        shellEnabled = shellEnabled,
        contextWindow = contextWindow,
        capabilities = capabilities,
    )
}
