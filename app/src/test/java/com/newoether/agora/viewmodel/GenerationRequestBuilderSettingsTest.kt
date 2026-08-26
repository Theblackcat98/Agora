package com.newoether.agora.viewmodel

import com.newoether.agora.data.ConversationSettings
import com.newoether.agora.data.repository.SettingsRepository
import com.newoether.agora.model.settings.ModelSettingsPatch
import com.newoether.agora.util.Constants
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerationRequestBuilderSettingsTest {

    @Test
    fun resolvesEffectiveSettingsWithConversationOverridesAndCapabilityClamping() {
        val settings = mockk<SettingsRepository>(relaxed = true)
        val providerRegistry = mockk<ProviderRegistry>(relaxed = true)

        val conversationSettingsMap = mapOf(
            "conv-1" to ConversationSettings(
                temperature = 1.5f,
                maxTokens = 4_000,
                thinkingEnabled = true,
                thinkingBudgetEnabled = true,
                thinkingBudgetTokens = 12_000,
            )
        )

        every { settings.maxContextWindow } returns MutableStateFlow(128_000)
        every { settings.defaultTemperature } returns MutableStateFlow(0.7f)
        every { settings.defaultMaxTokens } returns MutableStateFlow(2_048)
        every { settings.defaultTopP } returns MutableStateFlow(0.9f)
        every { settings.defaultFrequencyPenalty } returns MutableStateFlow(0.0f)
        every { settings.defaultPresencePenalty } returns MutableStateFlow(0.0f)
        every { settings.codeExecutionEnabled } returns MutableStateFlow(false)
        every { settings.googleSearchEnabled } returns MutableStateFlow(false)
        every { settings.thinkingEnabled } returns MutableStateFlow(false)
        every { settings.thinkingLevel } returns MutableStateFlow("medium")
        every { settings.thinkingBudgetEnabled } returns MutableStateFlow(false)
        every { settings.thinkingBudgetTokens } returns MutableStateFlow(1_024)
        every { settings.openAiServiceTierEnabled } returns MutableStateFlow(false)
        every { settings.openAiServiceTier } returns MutableStateFlow("auto")
        every { settings.webSearchEnabled } returns MutableStateFlow(true)
        every { settings.shellEnabled } returns MutableStateFlow(true)
        every { settings.conversationSettings } returns MutableStateFlow(conversationSettingsMap)
        every { settings.selectedModel } returns MutableStateFlow("Google:gemini-2.5-flash")
        every { providerRegistry.canonicalModelId(any()) } answers { firstArg() }

        val requestBuilder = GenerationRequestBuilder(
            settings = settings,
            convRepo = mockk(relaxed = true),
            memoryManager = mockk(relaxed = true),
            skillManager = mockk(relaxed = true),
            providerRegistry = providerRegistry,
            ragManager = mockk(relaxed = true),
            appContext = mockk(relaxed = true),
            pendingConversationSettings = MutableStateFlow(null),
            onSnackbar = {},
        )

        val effective = requestBuilder.resolveEffectiveModelSettings(
            modelId = "Google:gemini-2.5-flash",
            conversationId = "conv-1",
        )

        assertEquals(1.5f, effective.temperature)
        assertEquals(4_000, effective.maxTokens)
        assertTrue(effective.thinkingEnabled)
        assertTrue(effective.thinkingBudgetEnabled)
        assertEquals(12_000, effective.thinkingBudgetTokens)
    }

    @Test
    fun compactionOverlayDisablesThinkingAndTools() {
        val settings = mockk<SettingsRepository>(relaxed = true)
        val providerRegistry = mockk<ProviderRegistry>(relaxed = true)

        val conversationSettingsMap = mapOf(
            "conv-1" to ConversationSettings(
                temperature = 1.0f,
                codeExecutionEnabled = true,
                thinkingEnabled = true,
                webSearchEnabled = true,
                shellEnabled = true,
            )
        )

        every { settings.maxContextWindow } returns MutableStateFlow(128_000)
        every { settings.defaultTemperature } returns MutableStateFlow(0.7f)
        every { settings.defaultMaxTokens } returns MutableStateFlow(2_048)
        every { settings.defaultTopP } returns MutableStateFlow(0.9f)
        every { settings.defaultFrequencyPenalty } returns MutableStateFlow(0.0f)
        every { settings.defaultPresencePenalty } returns MutableStateFlow(0.0f)
        every { settings.codeExecutionEnabled } returns MutableStateFlow(true)
        every { settings.googleSearchEnabled } returns MutableStateFlow(true)
        every { settings.thinkingEnabled } returns MutableStateFlow(true)
        every { settings.thinkingLevel } returns MutableStateFlow("medium")
        every { settings.thinkingBudgetEnabled } returns MutableStateFlow(false)
        every { settings.thinkingBudgetTokens } returns MutableStateFlow(1_024)
        every { settings.openAiServiceTierEnabled } returns MutableStateFlow(false)
        every { settings.openAiServiceTier } returns MutableStateFlow("auto")
        every { settings.webSearchEnabled } returns MutableStateFlow(true)
        every { settings.shellEnabled } returns MutableStateFlow(true)
        every { settings.conversationSettings } returns MutableStateFlow(conversationSettingsMap)
        every { providerRegistry.canonicalModelId(any()) } answers { firstArg() }

        val requestBuilder = GenerationRequestBuilder(
            settings = settings,
            convRepo = mockk(relaxed = true),
            memoryManager = mockk(relaxed = true),
            skillManager = mockk(relaxed = true),
            providerRegistry = providerRegistry,
            ragManager = mockk(relaxed = true),
            appContext = mockk(relaxed = true),
            pendingConversationSettings = MutableStateFlow(null),
            onSnackbar = {},
        )

        val compactEffective = requestBuilder.resolveEffectiveModelSettings(
            modelId = "Google:gemini-2.5-flash",
            conversationId = "conv-1",
            overlayPatch = GenerationRequestBuilder.COMPACTION_OVERLAY_PATCH,
        )

        assertFalse(compactEffective.thinkingEnabled)
        assertFalse(compactEffective.codeExecutionEnabled)
        assertFalse(compactEffective.googleSearchEnabled)
        assertFalse(compactEffective.webSearchEnabled)
        assertFalse(compactEffective.shellEnabled)
    }
}
