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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.ContextBudget
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.settings.ModelSettingsPatch
import kotlin.math.roundToInt

@Composable
fun ConversationNumericParamsSection(
    resolved: ResolvedConversationFields,
    capabilities: ModelCapabilities,
    currentPatch: ModelSettingsPatch,
    onPatchChange: (ModelSettingsPatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Context Window
        val maxContext = capabilities.maxContextTokens
        val currentContextWindow = resolved.contextWindow.value.coerceIn(1000, maxContext)
        SettingHeaderWithOrigin(
            title = stringResource(R.string.context_window),
            origin = resolved.contextWindow.origin,
            onReset = {
                onPatchChange(currentPatch.copy(contextWindow = null))
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = currentContextWindow.toFloat(),
                onValueChange = {
                    onPatchChange(currentPatch.copy(contextWindow = it.roundToInt()))
                },
                valueRange = 1000f..maxContext.toFloat(),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = ContextBudget.compactLabel(currentContextWindow),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Temperature
        if (capabilities.supportsTemperature) {
            val range = capabilities.temperatureRange ?: ModelCapabilities.TEMPERATURE_STANDARD
            val currentTemp = resolved.temperature.value ?: range.default
            SettingHeaderWithOrigin(
                title = stringResource(R.string.gen_temperature),
                origin = resolved.temperature.origin,
                onReset = {
                    onPatchChange(currentPatch.copy(temperature = null))
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = currentTemp.coerceIn(range.min, range.max),
                    onValueChange = {
                        onPatchChange(currentPatch.copy(temperature = (it * 100).roundToInt() / 100f))
                    },
                    valueRange = range.min..range.max,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", currentTemp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Max Tokens
        val maxOutput = capabilities.maxOutputTokens ?: 4096
        val currentMaxTokens = (resolved.maxTokens.value ?: maxOutput).coerceIn(1, maxOutput)
        SettingHeaderWithOrigin(
            title = stringResource(R.string.gen_max_tokens),
            origin = resolved.maxTokens.origin,
            onReset = {
                onPatchChange(currentPatch.copy(maxTokens = null))
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = currentMaxTokens.toFloat(),
                onValueChange = {
                    onPatchChange(currentPatch.copy(maxTokens = it.roundToInt()))
                },
                valueRange = 1f..maxOutput.toFloat(),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$currentMaxTokens",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top P
        if (capabilities.supportsTopP) {
            val range = capabilities.topPRange ?: ModelCapabilities.TOP_P_STANDARD
            val currentTopP = resolved.topP.value ?: range.default
            SettingHeaderWithOrigin(
                title = stringResource(R.string.gen_top_p),
                origin = resolved.topP.origin,
                onReset = {
                    onPatchChange(currentPatch.copy(topP = null))
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = currentTopP.coerceIn(range.min, range.max),
                    onValueChange = {
                        onPatchChange(currentPatch.copy(topP = (it * 100).roundToInt() / 100f))
                    },
                    valueRange = range.min..range.max,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", currentTopP),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Frequency Penalty
        if (capabilities.supportsFrequencyPenalty) {
            val range = capabilities.frequencyPenaltyRange ?: ModelCapabilities.PENALTY_RANGE_DEFAULT
            val currentFreq = resolved.frequencyPenalty.value ?: range.default
            SettingHeaderWithOrigin(
                title = stringResource(R.string.gen_frequency_penalty),
                origin = resolved.frequencyPenalty.origin,
                onReset = {
                    onPatchChange(currentPatch.copy(frequencyPenalty = null))
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = currentFreq.coerceIn(range.min, range.max),
                    onValueChange = {
                        onPatchChange(currentPatch.copy(frequencyPenalty = (it * 100).roundToInt() / 100f))
                    },
                    valueRange = range.min..range.max,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", currentFreq),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Presence Penalty
        if (capabilities.supportsPresencePenalty) {
            val range = capabilities.presencePenaltyRange ?: ModelCapabilities.PENALTY_RANGE_DEFAULT
            val currentPres = resolved.presencePenalty.value ?: range.default
            SettingHeaderWithOrigin(
                title = stringResource(R.string.gen_presence_penalty),
                origin = resolved.presencePenalty.origin,
                onReset = {
                    onPatchChange(currentPatch.copy(presencePenalty = null))
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = currentPres.coerceIn(range.min, range.max),
                    onValueChange = {
                        onPatchChange(currentPatch.copy(presencePenalty = (it * 100).roundToInt() / 100f))
                    },
                    valueRange = range.min..range.max,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", currentPres),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
