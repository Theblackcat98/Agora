package com.newoether.agora.model.profile

import kotlinx.serialization.Serializable

/**
 * Bounds, defaults, and optional step size for a numeric model parameter.
 */
@Serializable
data class NumericConstraint<T : Comparable<T>>(
    val min: T,
    val max: T,
    val default: T,
    val step: T? = null,
) {
    fun clamp(value: T): T = when {
        value < min -> min
        value > max -> max
        else -> value
    }
}

/**
 * Thinking/reasoning capability descriptor for models supporting extended thinking/reasoning.
 */
@Serializable
data class ThinkingCapability(
    val supported: Boolean = false,
    val supportsBudget: Boolean = false,
    val budgetRange: NumericConstraint<Int>? = null,
    val supportedLevels: List<String> = emptyList(),
)

/**
 * Authoritative capability descriptor for an LLM provider/model combination.
 */
@Serializable
data class ModelCapabilities(
    val supportsTemperature: Boolean = true,
    val temperatureRange: NumericConstraint<Float>? = NumericConstraint(min = 0.0f, max = 2.0f, default = 1.0f),
    val supportsTopP: Boolean = true,
    val topPRange: NumericConstraint<Float>? = NumericConstraint(min = 0.0f, max = 1.0f, default = 1.0f),
    val supportsFrequencyPenalty: Boolean = false,
    val frequencyPenaltyRange: NumericConstraint<Float>? = null,
    val supportsPresencePenalty: Boolean = false,
    val presencePenaltyRange: NumericConstraint<Float>? = null,
    val maxContextTokens: Int = 128_000,
    val maxOutputTokens: Int? = 4_096,
    val thinking: ThinkingCapability = ThinkingCapability(),
    val supportsToolCalling: Boolean = true,
    val supportsVision: Boolean = false,
    val supportsStructuredOutput: Boolean = false,
    val supportsNativeSearch: Boolean = false,
    val supportsCodeExecution: Boolean = false,
    val supportsStreaming: Boolean = true,
) {
    companion object {
        val PENALTY_RANGE_DEFAULT = NumericConstraint(min = -2.0f, max = 2.0f, default = 0.0f)
        val TEMPERATURE_STANDARD = NumericConstraint(min = 0.0f, max = 2.0f, default = 1.0f)
        val TEMPERATURE_ZERO_TO_ONE = NumericConstraint(min = 0.0f, max = 1.0f, default = 1.0f)
        val TOP_P_STANDARD = NumericConstraint(min = 0.0f, max = 1.0f, default = 1.0f)

        /**
         * Conservative fallback capability set for unknown models or arbitrary custom providers.
         */
        val GENERIC_FALLBACK = ModelCapabilities(
            supportsTemperature = true,
            temperatureRange = TEMPERATURE_STANDARD,
            supportsTopP = true,
            topPRange = TOP_P_STANDARD,
            supportsFrequencyPenalty = false,
            frequencyPenaltyRange = null,
            supportsPresencePenalty = false,
            presencePenaltyRange = null,
            maxContextTokens = 128_000,
            maxOutputTokens = 4_096,
            thinking = ThinkingCapability(supported = false),
            supportsToolCalling = true,
            supportsVision = false,
            supportsStructuredOutput = false,
            supportsNativeSearch = false,
            supportsCodeExecution = false,
            supportsStreaming = true,
        )
    }
}
