package com.newoether.agora.viewmodel

import com.newoether.agora.api.util.ContextUsage
import com.newoether.agora.api.util.contextWindowRetainedMessageIds
import com.newoether.agora.api.util.contextWindowUsage
import com.newoether.agora.data.repository.ConversationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

internal data class ConversationContextProjection(
    val inputs: ContextProjectionInputs = ContextProjectionInputs(),
    val usage: ContextUsage? = null,
    val retainedMessageIds: Set<String>? = null,
    val loading: Boolean = false,
    val completed: Boolean = false,
    val failed: Boolean = false,
) {
    constructor(
        conversationId: String?,
        selectedBranchesJson: String?,
        usage: ContextUsage?,
        retainedMessageIds: Set<String>?,
        loading: Boolean = false,
        completed: Boolean = false,
        failed: Boolean = false,
    ) : this(
        inputs = ContextProjectionInputs(
            conversationId = conversationId,
            selectedBranchesJson = selectedBranchesJson,
            tokenBudget = usage?.tokenBudget ?: com.newoether.agora.model.ContextBudget.DEFAULT_TOKENS,
        ),
        usage = usage,
        retainedMessageIds = retainedMessageIds,
        loading = loading,
        completed = completed,
        failed = failed,
    )

    val conversationId: String? get() = inputs.conversationId
    val selectedBranchesJson: String? get() = inputs.selectedBranchesJson
    val selectedModelId: String get() = inputs.selectedModelId
    val tokenBudget: Int get() = inputs.tokenBudget
}

/** Builds UI context accounting from the same canonical durable projection used by generation. */
internal class ConversationContextProjector(
    private val conversations: ConversationRepository,
    private val requestBuilder: GenerationRequestBuilder,
    private val generationManager: () -> GenerationManager,
    private val newChatSystemPromptId: () -> String? = { null },
    private val contextLoader: DurableSelectedContextLoader =
        DurableSelectedContextLoader(conversations),
) {
    private val requestIds = AtomicLong(0L)
    private val _projection = MutableStateFlow(ConversationContextProjection())
    val projection: StateFlow<ConversationContextProjection> = _projection.asStateFlow()

    fun invalidate(conversationId: String?) {
        val previousUsage = _projection.value.usage
        requestIds.incrementAndGet()
        _projection.value = ConversationContextProjection(
            inputs = ContextProjectionInputs(conversationId = conversationId),
            usage = previousUsage,
            loading = true,
        )
    }

    suspend fun project(inputs: ContextProjectionInputs): ConversationContextProjection {
        val previousUsage = _projection.value.usage
        val requestId = requestIds.incrementAndGet()
        _projection.value = ConversationContextProjection(
            inputs = inputs,
            usage = previousUsage,
            loading = true,
        )
        val result = try {
            val effectiveConversationId = inputs.conversationId ?: CONTEXT_PREVIEW_CONVERSATION_ID
            val snapshot = inputs.selectedModelId.takeIf(String::isNotBlank)?.let { modelId ->
                try {
                    requestBuilder.captureContextProjectionSnapshot(
                        conversationId = effectiveConversationId,
                        modelId = modelId,
                        systemPromptIdOverride = if (inputs.conversationId == null) {
                            newChatSystemPromptId()
                        } else {
                            null
                        },
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }
            }
            val durableProviderMessages = inputs.conversationId?.let {
                contextLoader.load(
                    DurableSelectedContextRequest(
                        conversationId = it,
                        followSelectedBranch = true,
                        includeStoredTranscriptions =
                            snapshot?.context?.imageTranscriptionEnabled ?: true,
                    ),
                ).messages
            }.orEmpty()
            val contextMessages = snapshot?.let {
                projectGenerationInputMessages(
                    messages = durableProviderMessages,
                    // Transcription-enabled models receive descriptions instead of raw images at
                    // dispatch; the bottom-bar estimate must match.
                    includeImages = !it.context.imageTranscriptionEnabled,
                    userPrepend = it.config.userPrepend,
                    userPostpend = it.config.userPostpend,
                )
            } ?: durableProviderMessages
            val fixedTokenCost = snapshot?.let {
                generationManager().fixedContextTokenCost(it.config, it.context)
            } ?: 0
            val effectiveBudget = if (inputs.tokenBudget > 0) {
                inputs.tokenBudget
            } else {
                snapshot?.config?.maxContextWindow ?: com.newoether.agora.model.ContextBudget.DEFAULT_TOKENS
            }
            ConversationContextProjection(
                inputs = inputs.copy(tokenBudget = effectiveBudget),
                usage = contextWindowUsage(
                    messages = contextMessages,
                    tokenBudget = effectiveBudget,
                    fixedTokenCost = fixedTokenCost,
                ),
                retainedMessageIds = contextWindowRetainedMessageIds(
                    messages = contextMessages,
                    tokenBudget = effectiveBudget,
                    fixedTokenCost = fixedTokenCost,
                ),
                completed = true,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            ConversationContextProjection(
                inputs = inputs,
                completed = true,
                failed = true,
            )
        }
        if (requestIds.get() == requestId) {
            _projection.value = result
        }
        return result
    }

    suspend fun project(
        conversationId: String?,
        selectedBranchesJson: String?,
        selectedModelId: String,
        tokenBudget: Int,
    ): ConversationContextProjection = project(
        ContextProjectionInputs(
            conversationId = conversationId,
            selectedBranchesJson = selectedBranchesJson,
            selectedModelId = selectedModelId,
            tokenBudget = tokenBudget,
        ),
    )

    private companion object {
        const val CONTEXT_PREVIEW_CONVERSATION_ID = "context-preview-conversation"
    }
}
