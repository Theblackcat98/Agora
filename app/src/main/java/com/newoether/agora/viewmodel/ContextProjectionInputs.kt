package com.newoether.agora.viewmodel

import com.newoether.agora.model.ContextBudget

/**
 * Encapsulates all conversation and model parameters needed to project provider-visible context.
 */
data class ContextProjectionInputs(
    val conversationId: String? = null,
    val selectedBranchesJson: String? = null,
    val selectedModelId: String = "",
    val tokenBudget: Int = ContextBudget.DEFAULT_TOKENS,
)
