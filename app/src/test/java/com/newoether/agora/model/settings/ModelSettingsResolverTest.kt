package com.newoether.agora.model.settings

import com.newoether.agora.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelSettingsResolverTest {

    private val defaultGlobalPatch = ModelSettingsPatch(
        contextWindow = 50_000,
        temperature = 0.7f,
        maxTokens = 2_048,
        topP = 0.9f,
        frequencyPenalty = 0.1f,
        presencePenalty = 0.1f,
        thinkingEnabled = false,
        codeExecutionEnabled = false,
        googleSearchEnabled = false,
        webSearchEnabled = true,
        shellEnabled = true,
    )

    @Test
    fun resolvesGlobalDefaultsWhenNoOverridesPresent() {
        val resolver = ModelSettingsResolver(
            globalDefaults = { defaultGlobalPatch }
        )
        val settings = resolver.resolve("Google:gemini-2.5-flash")
        assertEquals(50_000, settings.contextWindow)
        assertEquals(0.7f, settings.temperature)
        assertEquals(2_048, settings.maxTokens)
        assertEquals(0.9f, settings.topP)
        assertFalse(settings.thinkingEnabled)
        assertTrue(settings.webSearchEnabled)
    }

    @Test
    fun conversationOverrideTakesPrecedenceOverGlobalDefaults() {
        val conversationOverrides = mapOf(
            "conv-1" to ModelSettingsPatch(
                temperature = 1.8f,
                maxTokens = 4_000,
                thinkingEnabled = true,
                thinkingBudgetEnabled = true,
                thinkingBudgetTokens = 8_192,
            )
        )
        val resolver = ModelSettingsResolver(
            globalDefaults = { defaultGlobalPatch },
            conversationOverrides = { conversationOverrides[it] }
        )
        val settings = resolver.resolve("Google:gemini-2.5-flash", conversationId = "conv-1")
        assertEquals(1.8f, settings.temperature)
        assertEquals(4_000, settings.maxTokens)
        assertTrue(settings.thinkingEnabled)
        assertTrue(settings.thinkingBudgetEnabled)
        assertEquals(8_192, settings.thinkingBudgetTokens)
    }

    @Test
    fun clampsContextWindowToModelMaxContextTokens() {
        val resolver = ModelSettingsResolver(
            globalDefaults = {
                ModelSettingsPatch(contextWindow = 500_000)
            }
        )
        // GPT-4o has a 128k context limit
        val settings = resolver.resolve("OpenAI:gpt-4o")
        assertEquals(128_000, settings.contextWindow)
    }

    @Test
    fun omitsUnsupportedParametersForReasoningModels() {
        val resolver = ModelSettingsResolver(
            globalDefaults = {
                ModelSettingsPatch(
                    temperature = 0.5f,
                    topP = 0.8f,
                    frequencyPenalty = 0.5f,
                    thinkingEnabled = true,
                )
            }
        )
        val settings = resolver.resolve("OpenAI:o3-mini")
        assertNull(settings.temperature)
        assertNull(settings.topP)
        assertNull(settings.frequencyPenalty)
        assertTrue(settings.thinkingEnabled)
    }

    @Test
    fun clampsTemperatureToModelRange() {
        val resolver = ModelSettingsResolver(
            globalDefaults = {
                ModelSettingsPatch(temperature = 1.9f)
            }
        )
        // Claude allows max 1.0 temperature
        val settings = resolver.resolve("Anthropic:claude-3-5-sonnet-20241022")
        assertEquals(1.0f, settings.temperature)
    }

    @Test
    fun overlayPatchOverridesConversationAndGlobalSettings() {
        val conversationOverrides = mapOf(
            "conv-1" to ModelSettingsPatch(temperature = 0.8f, codeExecutionEnabled = true)
        )
        val resolver = ModelSettingsResolver(
            globalDefaults = { defaultGlobalPatch },
            conversationOverrides = { conversationOverrides[it] }
        )
        val compactionOverlay = ModelSettingsPatch(
            temperature = 0.2f,
            codeExecutionEnabled = false,
            webSearchEnabled = false,
            shellEnabled = false,
        )
        val settings = resolver.resolve(
            modelId = "Google:gemini-2.5-flash",
            conversationId = "conv-1",
            overlayPatch = compactionOverlay,
        )
        assertEquals(0.2f, settings.temperature)
        assertFalse(settings.codeExecutionEnabled)
        assertFalse(settings.webSearchEnabled)
        assertFalse(settings.shellEnabled)
    }
}
