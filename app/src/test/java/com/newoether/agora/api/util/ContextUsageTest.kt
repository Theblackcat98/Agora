package com.newoether.agora.api.util

import com.newoether.agora.model.ChatMessage
import com.newoether.agora.model.MessageStatus
import com.newoether.agora.model.Participant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextUsageTest {
    @Test
    fun progressIsZeroWhenBudgetIsZeroOrNegative() {
        val zeroBudget = ContextUsage(
            estimatedTokenCount = 500,
            tokenBudget = 0,
            logicalMessageCount = 2,
            hasCompactBoundary = false,
        )
        val negativeBudget = ContextUsage(
            estimatedTokenCount = 500,
            tokenBudget = -10,
            logicalMessageCount = 2,
            hasCompactBoundary = false,
        )
        assertEquals(0f, zeroBudget.progress, 0.0001f)
        assertEquals(0f, negativeBudget.progress, 0.0001f)
    }

    @Test
    fun progressIsCalculatedAndClampedBetweenZeroAndOne() {
        val half = ContextUsage(
            estimatedTokenCount = 2_048,
            tokenBudget = 4_096,
            logicalMessageCount = 4,
            hasCompactBoundary = false,
        )
        val overflow = ContextUsage(
            estimatedTokenCount = 8_192,
            tokenBudget = 4_096,
            logicalMessageCount = 10,
            hasCompactBoundary = true,
        )
        assertEquals(0.5f, half.progress, 0.0001f)
        assertEquals(1.0f, overflow.progress, 0.0001f)
    }

    @Test
    fun compactThresholdEvaluationMatchesCompactorCeilingLogic() {
        val usage = ContextUsage(
            estimatedTokenCount = 90,
            tokenBudget = 100,
            logicalMessageCount = 5,
            hasCompactBoundary = false,
        )
        // For budget 100 and threshold 90%, ceiling threshold is 90 tokens.
        assertTrue(usage.exceedsCompactThreshold(90))
        assertFalse(usage.copy(estimatedTokenCount = 89).exceedsCompactThreshold(90))
        assertTrue(usage.copy(estimatedTokenCount = 91).exceedsCompactThreshold(90))

        // Threshold 50% on 100 budget -> 50 tokens
        assertTrue(usage.copy(estimatedTokenCount = 50).exceedsCompactThreshold(50))
        assertFalse(usage.copy(estimatedTokenCount = 49).exceedsCompactThreshold(50))

        // Zero or negative budget never exceeds threshold
        assertFalse(usage.copy(tokenBudget = 0).exceedsCompactThreshold(90))
        assertFalse(usage.copy(tokenBudget = -1).exceedsCompactThreshold(90))
    }

    @Test
    fun capacityCheckTriggersWhenEstimatedTokensReachBudget() {
        val usage = ContextUsage(
            estimatedTokenCount = 100,
            tokenBudget = 100,
            logicalMessageCount = 3,
            hasCompactBoundary = false,
        )
        assertTrue(usage.isAtCapacity())
        assertTrue(usage.copy(estimatedTokenCount = 101).isAtCapacity())
        assertFalse(usage.copy(estimatedTokenCount = 99).isAtCapacity())
        assertFalse(usage.copy(tokenBudget = 0).isAtCapacity())
    }

    @Test
    fun contextWindowUsageFactoryComputesCanonicalUsage() {
        val user = ChatMessage(
            id = "u1",
            text = "hello world",
            participant = Participant.USER,
            status = MessageStatus.SUCCESS,
        )
        val assistant = ChatMessage(
            id = "a1",
            text = "greetings",
            participant = Participant.MODEL,
            status = MessageStatus.SUCCESS,
        )
        val usage = contextWindowUsage(
            messages = listOf(user, assistant),
            tokenBudget = 4_096,
            fixedTokenCost = 50,
        )
        assertTrue(usage.estimatedTokenCount > 50)
        assertEquals(4_096, usage.tokenBudget)
        assertEquals(2, usage.logicalMessageCount)
        assertFalse(usage.hasCompactBoundary)
    }
}
