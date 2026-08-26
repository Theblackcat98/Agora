package com.newoether.agora.model.profile

import com.newoether.agora.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelProfileRegistryTest {

    @Test
    fun builtInGeminiFlashResolvesWithCorrectCapabilities() {
        val profile = ModelProfileRegistry.resolve("Google:gemini-2.5-flash")
        assertEquals(Constants.PROVIDER_GOOGLE, profile.providerReference)
        assertEquals("gemini-2.5-flash", profile.modelName)
        assertEquals(1_048_576, profile.capabilities.maxContextTokens)
        assertTrue(profile.capabilities.thinking.supported)
        assertTrue(profile.capabilities.thinking.supportsBudget)
        assertTrue(profile.capabilities.supportsCodeExecution)
        assertTrue(profile.capabilities.supportsNativeSearch)
        assertTrue(profile.capabilities.supportsVision)
    }

    @Test
    fun builtInOpenAiGpt4oResolvesWithPenalties() {
        val profile = ModelProfileRegistry.resolve("OpenAI:gpt-4o")
        assertEquals(Constants.PROVIDER_OPENAI, profile.providerReference)
        assertEquals(128_000, profile.capabilities.maxContextTokens)
        assertTrue(profile.capabilities.supportsFrequencyPenalty)
        assertTrue(profile.capabilities.supportsPresencePenalty)
        assertFalse(profile.capabilities.thinking.supported)
    }

    @Test
    fun openAiReasoningModelsDisableTemperatureAndPenalties() {
        val profile = ModelProfileRegistry.resolve("OpenAI:o3-mini")
        assertFalse(profile.capabilities.supportsTemperature)
        assertFalse(profile.capabilities.supportsTopP)
        assertFalse(profile.capabilities.supportsFrequencyPenalty)
        assertTrue(profile.capabilities.thinking.supported)
        assertEquals(200_000, profile.capabilities.maxContextTokens)
    }

    @Test
    fun claude37SonnetResolvesWithExtendedThinkingBudget() {
        val profile = ModelProfileRegistry.resolve("Anthropic:claude-3-7-sonnet-20250219")
        assertTrue(profile.capabilities.thinking.supported)
        assertTrue(profile.capabilities.thinking.supportsBudget)
        assertNotNull(profile.capabilities.thinking.budgetRange)
        assertEquals(1_024, profile.capabilities.thinking.budgetRange?.min)
        assertEquals(64_000, profile.capabilities.thinking.budgetRange?.max)
    }

    @Test
    fun unknownCustomModelFallsBackSafely() {
        val profile = ModelProfileRegistry.resolve("Custom-1234:custom-unknown-model-v1")
        assertEquals("Custom-1234", profile.providerReference)
        assertEquals("custom-unknown-model-v1", profile.modelName)
        assertEquals(128_000, profile.capabilities.maxContextTokens)
        assertTrue(profile.capabilities.supportsTemperature)
        assertFalse(profile.capabilities.thinking.supported)
        assertFalse(profile.capabilities.supportsNativeSearch)
        assertFalse(profile.capabilities.supportsCodeExecution)
    }

    @Test
    fun unprefixedLegacyModelIdResolvesCorrectly() {
        val profile = ModelProfileRegistry.resolve("gpt-4o")
        assertEquals(Constants.PROVIDER_OPENAI, profile.providerReference)
        assertEquals("gpt-4o", profile.modelName)
    }
}
