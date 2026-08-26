package com.newoether.agora.api

import com.newoether.agora.api.util.ContextUsage
import com.newoether.agora.model.ChatMessage
import com.newoether.agora.model.TokenUsage
import com.newoether.agora.model.settings.EffectiveModelSettings

/**
 * Boundary between the provider-neutral generation pipeline and concrete provider wire-format logic.
 *
 * Each provider (OpenAI, Anthropic, Gemini, Ollama, etc.) implements this interface to isolate
 * all provider-specific parameter naming, placement, quirks, and constraints.
 *
 * The core generation pipeline works exclusively with [EffectiveModelSettings] and hands off
 * to an adapter for request construction and response reconciliation, keeping provider-specific
 * conditionals out of shared code.
 *
 * ## Contract
 * - Adapters receive fully-resolved, capability-filtered settings; they must not re-check model
 *   capabilities already filtered by the effective-settings resolver.
 * - A missing or unresolvable adapter must surface a [ProviderAdapterError] through the adapter
 *   registry rather than crash.
 * - Streaming, tool calls, multimodal content, retries, stop behavior, and response parsing are
 *   all owned by the adapter or its underlying provider implementation.
 */
interface ProviderRequestAdapter {
    /** Stable identifier matching the provider name used in [EffectiveModelSettings.profile]. */
    val providerId: String

    /**
     * Reconcile provider-reported [TokenUsage] with the [estimatedUsage] produced before the
     * request.
     *
     * Returns a [ContextUsage] that prefers authoritative values when available, falling back to
     * the estimate for any missing component.  Providers that do not report usage should return
     * [estimatedUsage] unchanged.
     */
    fun reconcileContextUsage(
        estimatedUsage: ContextUsage,
        reportedUsage: TokenUsage?,
    ): ContextUsage = if (reportedUsage != null) {
        ContextUsage.fromTokenUsage(
            estimatedTokenCount = estimatedUsage.estimatedTokenCount,
            tokenBudget = estimatedUsage.tokenBudget,
            reportedUsage = reportedUsage,
            logicalMessageCount = estimatedUsage.logicalMessageCount,
            hasCompactBoundary = estimatedUsage.hasCompactBoundary,
        )
    } else {
        estimatedUsage
    }

    /**
     * Provider-specific description of the context breakdown for diagnostics.
     *
     * Returns a human-readable breakdown of context token attribution (messages, system prompt,
     * tools, images, etc.) when [EffectiveModelSettings] and the message list are available.
     * Implementations may return null when detailed breakdown is not supported.
     */
    fun describeContextBreakdown(
        settings: EffectiveModelSettings,
        messages: List<ChatMessage>,
        reportedUsage: TokenUsage?,
    ): ContextBreakdown? = null
}

/**
 * Itemised context breakdown for one generation call.
 *
 * All counts are approximate unless [isAuthoritative] is true.
 */
data class ContextBreakdown(
    val messages: Int,
    val systemPrompt: Int,
    val tools: Int,
    val images: Int,
    val other: Int,
    val total: Int,
    val isAuthoritative: Boolean,
)

/**
 * Thrown when the adapter registry cannot resolve an adapter for the requested provider.
 */
class ProviderAdapterError(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
