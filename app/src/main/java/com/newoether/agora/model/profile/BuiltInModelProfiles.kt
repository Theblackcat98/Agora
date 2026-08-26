package com.newoether.agora.model.profile

import com.newoether.agora.model.ModelId
import com.newoether.agora.util.Constants

/**
 * Static catalog of known capabilities and profiles for standard built-in models.
 */
object BuiltInModelProfiles {

    private fun profile(
        provider: String,
        modelName: String,
        displayName: String,
        capabilities: ModelCapabilities,
        defaultContextWindow: Int = capabilities.maxContextTokens,
    ): ModelProfile = ModelProfile(
        canonicalId = "$provider:$modelName",
        providerReference = provider,
        modelName = modelName,
        displayName = displayName,
        capabilities = capabilities,
        defaultContextWindow = defaultContextWindow,
    )

    // ── Google Gemini Profiles ──────────────────────────────
    private val GEMINI_2_5_FLASH_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 1_048_576,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(
            supported = true,
            supportsBudget = true,
            budgetRange = NumericConstraint(min = 0, max = 24_576, default = 0),
            supportedLevels = listOf("low", "medium", "high"),
        ),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsNativeSearch = true,
        supportsCodeExecution = true,
        supportsStreaming = true,
    )

    private val GEMINI_2_5_PRO_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 2_097_152,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(
            supported = true,
            supportsBudget = true,
            budgetRange = NumericConstraint(min = 0, max = 32_768, default = 0),
            supportedLevels = listOf("low", "medium", "high"),
        ),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsNativeSearch = true,
        supportsCodeExecution = true,
        supportsStreaming = true,
    )

    private val GEMINI_1_5_PRO_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 2_097_152,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(supported = false),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsNativeSearch = true,
        supportsCodeExecution = true,
        supportsStreaming = true,
    )

    private val GEMINI_1_5_FLASH_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 1_048_576,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(supported = false),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsNativeSearch = true,
        supportsCodeExecution = true,
        supportsStreaming = true,
    )

    // ── OpenAI Profiles ─────────────────────────────────────
    private val OPENAI_GPT4O_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        supportsFrequencyPenalty = true,
        frequencyPenaltyRange = ModelCapabilities.PENALTY_RANGE_DEFAULT,
        supportsPresencePenalty = true,
        presencePenaltyRange = ModelCapabilities.PENALTY_RANGE_DEFAULT,
        maxContextTokens = 128_000,
        maxOutputTokens = 16_384,
        thinking = ThinkingCapability(supported = false),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsNativeSearch = true,
        supportsCodeExecution = false,
        supportsStreaming = true,
    )

    private val OPENAI_REASONING_CAPABILITIES = ModelCapabilities(
        supportsTemperature = false,
        temperatureRange = null,
        supportsTopP = false,
        topPRange = null,
        supportsFrequencyPenalty = false,
        frequencyPenaltyRange = null,
        supportsPresencePenalty = false,
        presencePenaltyRange = null,
        maxContextTokens = 200_000,
        maxOutputTokens = 100_000,
        thinking = ThinkingCapability(
            supported = true,
            supportsBudget = false,
            supportedLevels = listOf("low", "medium", "high"),
        ),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsNativeSearch = true,
        supportsCodeExecution = false,
        supportsStreaming = true,
    )

    // ── Anthropic Profiles ──────────────────────────────────
    private val ANTHROPIC_SONNET_3_7_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_ZERO_TO_ONE,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 200_000,
        maxOutputTokens = 64_000,
        thinking = ThinkingCapability(
            supported = true,
            supportsBudget = true,
            budgetRange = NumericConstraint(min = 1_024, max = 64_000, default = 1_024),
            supportedLevels = emptyList(),
        ),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = false,
        supportsNativeSearch = false,
        supportsCodeExecution = false,
        supportsStreaming = true,
    )

    private val ANTHROPIC_SONNET_3_5_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_ZERO_TO_ONE,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 200_000,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(supported = false),
        supportsToolCalling = true,
        supportsVision = true,
        supportsStructuredOutput = false,
        supportsNativeSearch = false,
        supportsCodeExecution = false,
        supportsStreaming = true,
    )

    // ── DeepSeek Profiles ───────────────────────────────────
    private val DEEPSEEK_REASONER_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 64_000,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(supported = true),
        supportsToolCalling = false,
        supportsVision = false,
        supportsStructuredOutput = false,
        supportsNativeSearch = false,
        supportsCodeExecution = false,
        supportsStreaming = true,
    )

    private val DEEPSEEK_CHAT_CAPABILITIES = ModelCapabilities(
        supportsTemperature = true,
        temperatureRange = ModelCapabilities.TEMPERATURE_STANDARD,
        supportsTopP = true,
        topPRange = ModelCapabilities.TOP_P_STANDARD,
        maxContextTokens = 64_000,
        maxOutputTokens = 8_192,
        thinking = ThinkingCapability(supported = false),
        supportsToolCalling = true,
        supportsVision = false,
        supportsStructuredOutput = false,
        supportsNativeSearch = false,
        supportsCodeExecution = false,
        supportsStreaming = true,
    )

    val BUILT_IN_PROFILES: Map<String, ModelProfile> = listOf(
        // Google
        profile(Constants.PROVIDER_GOOGLE, "gemini-2.5-flash", "Gemini 2.5 Flash", GEMINI_2_5_FLASH_CAPABILITIES),
        profile(Constants.PROVIDER_GOOGLE, "gemini-2.5-pro", "Gemini 2.5 Pro", GEMINI_2_5_PRO_CAPABILITIES),
        profile(Constants.PROVIDER_GOOGLE, "gemini-2.0-flash", "Gemini 2.0 Flash", GEMINI_1_5_FLASH_CAPABILITIES),
        profile(Constants.PROVIDER_GOOGLE, "gemini-1.5-pro", "Gemini 1.5 Pro", GEMINI_1_5_PRO_CAPABILITIES),
        profile(Constants.PROVIDER_GOOGLE, "gemini-1.5-flash", "Gemini 1.5 Flash", GEMINI_1_5_FLASH_CAPABILITIES),

        // OpenAI
        profile(Constants.PROVIDER_OPENAI, "gpt-4o", "GPT-4o", OPENAI_GPT4O_CAPABILITIES),
        profile(Constants.PROVIDER_OPENAI, "gpt-4o-mini", "GPT-4o mini", OPENAI_GPT4O_CAPABILITIES),
        profile(Constants.PROVIDER_OPENAI, "o1", "o1", OPENAI_REASONING_CAPABILITIES),
        profile(Constants.PROVIDER_OPENAI, "o3-mini", "o3-mini", OPENAI_REASONING_CAPABILITIES),

        // Anthropic
        profile(Constants.PROVIDER_ANTHROPIC, "claude-3-7-sonnet-20250219", "Claude 3.7 Sonnet", ANTHROPIC_SONNET_3_7_CAPABILITIES),
        profile(Constants.PROVIDER_ANTHROPIC, "claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet", ANTHROPIC_SONNET_3_5_CAPABILITIES),
        profile(Constants.PROVIDER_ANTHROPIC, "claude-3-5-haiku-20241022", "Claude 3.5 Haiku", ANTHROPIC_SONNET_3_5_CAPABILITIES),
        profile(Constants.PROVIDER_ANTHROPIC, "claude-3-opus-20240229", "Claude 3 Opus", ANTHROPIC_SONNET_3_5_CAPABILITIES),

        // DeepSeek
        profile(Constants.PROVIDER_DEEPSEEK, "deepseek-reasoner", "DeepSeek Reasoner", DEEPSEEK_REASONER_CAPABILITIES),
        profile(Constants.PROVIDER_DEEPSEEK, "deepseek-chat", "DeepSeek Chat", DEEPSEEK_CHAT_CAPABILITIES),
    ).associateBy { it.canonicalId }

    /**
     * Creates a safe default fallback profile for an unknown or custom model.
     */
    fun createFallbackProfile(
        canonicalId: String,
        providerReference: String,
        modelName: String,
    ): ModelProfile {
        val cleanModelName = modelName.removePrefix("models/")
        val inferredCapabilities = when {
            cleanModelName.contains("gemini-2.5-pro", ignoreCase = true) -> GEMINI_2_5_PRO_CAPABILITIES
            cleanModelName.contains("gemini-2.5-flash", ignoreCase = true) -> GEMINI_2_5_FLASH_CAPABILITIES
            cleanModelName.contains("gemini", ignoreCase = true) -> GEMINI_1_5_FLASH_CAPABILITIES
            cleanModelName.contains("claude-3-7", ignoreCase = true) -> ANTHROPIC_SONNET_3_7_CAPABILITIES
            cleanModelName.contains("claude-3-5", ignoreCase = true) -> ANTHROPIC_SONNET_3_5_CAPABILITIES
            cleanModelName.contains("claude", ignoreCase = true) -> ANTHROPIC_SONNET_3_5_CAPABILITIES
            cleanModelName.contains("o1", ignoreCase = true) || cleanModelName.contains("o3", ignoreCase = true) -> OPENAI_REASONING_CAPABILITIES
            cleanModelName.contains("gpt-4", ignoreCase = true) -> OPENAI_GPT4O_CAPABILITIES
            cleanModelName.contains("deepseek-reasoner", ignoreCase = true) || cleanModelName.contains("deepseek-r1", ignoreCase = true) -> DEEPSEEK_REASONER_CAPABILITIES
            cleanModelName.contains("deepseek", ignoreCase = true) -> DEEPSEEK_CHAT_CAPABILITIES
            else -> ModelCapabilities.GENERIC_FALLBACK
        }
        return ModelProfile(
            canonicalId = canonicalId,
            providerReference = providerReference,
            modelName = modelName,
            displayName = cleanModelName,
            capabilities = inferredCapabilities,
            defaultContextWindow = inferredCapabilities.maxContextTokens,
        )
    }
}
