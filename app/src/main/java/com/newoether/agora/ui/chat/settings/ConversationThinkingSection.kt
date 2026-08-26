package com.newoether.agora.ui.chat.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.ThinkingLevels
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.settings.ModelSettingsPatch
import kotlin.math.roundToInt

@Composable
fun ConversationThinkingSection(
    resolved: ResolvedConversationFields,
    capabilities: ModelCapabilities,
    currentPatch: ModelSettingsPatch,
    onPatchChange: (ModelSettingsPatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thinkingCap = capabilities.thinking
    if (!thinkingCap.supported) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.thinking_unsupported_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val thinkingEnabled = resolved.thinkingEnabled.value
    val currentLevel = resolved.thinkingLevel.value ?: ThinkingLevels.DefaultEffort
    val budgetEnabled = resolved.thinkingBudgetEnabled.value
    val currentBudget = resolved.thinkingBudgetTokens.value ?: ThinkingLevels.DefaultBudgetTokens

    Column(modifier = modifier.fillMaxWidth()) {
        // Thinking Enabled Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SettingHeaderWithOrigin(
                    title = stringResource(R.string.gen_thinking_enabled),
                    origin = resolved.thinkingEnabled.origin,
                    onReset = {
                        onPatchChange(currentPatch.copy(thinkingEnabled = null))
                    },
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = thinkingEnabled,
                onCheckedChange = { checked ->
                    onPatchChange(currentPatch.copy(thinkingEnabled = checked))
                },
            )
        }

        if (thinkingEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Thinking Effort
            val supportedLevels = if (thinkingCap.supportedLevels.isNotEmpty()) {
                thinkingCap.supportedLevels
            } else {
                ThinkingLevels.ALL_LEVELS
            }

            val currentIndex = supportedLevels.indexOf(currentLevel).coerceAtLeast(0)
            SettingHeaderWithOrigin(
                title = stringResource(R.string.thinking_effort_label),
                origin = resolved.thinkingLevel.origin,
                onReset = {
                    onPatchChange(currentPatch.copy(thinkingLevel = null))
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = currentIndex.toFloat(),
                    onValueChange = {
                        val idx = it.roundToInt().coerceIn(0, supportedLevels.lastIndex)
                        onPatchChange(currentPatch.copy(thinkingLevel = supportedLevels[idx]))
                    },
                    valueRange = 0f..supportedLevels.lastIndex.toFloat(),
                    steps = if (supportedLevels.size > 2) supportedLevels.size - 2 else 0,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentLevel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Budget Controls
            if (thinkingCap.supportsBudget) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SettingHeaderWithOrigin(
                            title = stringResource(R.string.thinking_use_budget),
                            origin = resolved.thinkingBudgetEnabled.origin,
                            onReset = {
                                onPatchChange(currentPatch.copy(thinkingBudgetEnabled = null))
                            },
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = budgetEnabled,
                        onCheckedChange = { checked ->
                            onPatchChange(currentPatch.copy(thinkingBudgetEnabled = checked))
                        },
                    )
                }

                if (budgetEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val budgetRange = thinkingCap.budgetRange
                    val minBudget = budgetRange?.min ?: 1024
                    val maxBudget = budgetRange?.max ?: 65536
                    SettingHeaderWithOrigin(
                        title = stringResource(R.string.thinking_budget_input_label),
                        origin = resolved.thinkingBudgetTokens.origin,
                        onReset = {
                            onPatchChange(currentPatch.copy(thinkingBudgetTokens = null))
                        },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = currentBudget.coerceIn(minBudget, maxBudget).toFloat(),
                            onValueChange = {
                                onPatchChange(currentPatch.copy(thinkingBudgetTokens = it.roundToInt()))
                            },
                            valueRange = minBudget.toFloat()..maxBudget.toFloat(),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$currentBudget",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
