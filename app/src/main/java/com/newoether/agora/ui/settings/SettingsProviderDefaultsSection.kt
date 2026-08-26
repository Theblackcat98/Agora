package com.newoether.agora.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.settings.ModelSettingsPatch
import com.newoether.agora.viewmodel.ChatViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsProviderDefaultsSection(
    providerName: String,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
) {
    val providerSettings by viewModel.settings.providerSettings.collectAsState()
    val defaultTemp by viewModel.settings.defaultTemperature.collectAsState()
    val defaultMaxTokens by viewModel.settings.defaultMaxTokens.collectAsState()
    val defaultTopP by viewModel.settings.defaultTopP.collectAsState()
    val defaultFreq by viewModel.settings.defaultFrequencyPenalty.collectAsState()
    val defaultPres by viewModel.settings.defaultPresencePenalty.collectAsState()

    val currentPatch = providerSettings[providerName] ?: ModelSettingsPatch()
    val isAnyOverridden = !currentPatch.isAllNull()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.provider_defaults_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.provider_defaults_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isAnyOverridden) {
                    TextButton(
                        onClick = {
                            viewModel.settings.resetProviderSettingsPatch(providerName)
                        },
                    ) {
                        Text(stringResource(R.string.gen_reset))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            val tempVal = currentPatch.temperature ?: defaultTemp ?: 1.0f
            SettingsParamHeader(
                title = stringResource(R.string.gen_temperature),
                isOverridden = currentPatch.temperature != null,
                inheritedLabel = stringResource(R.string.setting_origin_inherited_global),
                onReset = {
                    viewModel.settings.updateProviderSettingsPatch(
                        providerName,
                        currentPatch.copy(temperature = null),
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = tempVal.coerceIn(0.0f, 2.0f),
                    onValueChange = {
                        val rounded = (it * 100).roundToInt() / 100f
                        viewModel.settings.updateProviderSettingsPatch(
                            providerName,
                            currentPatch.copy(temperature = rounded),
                        )
                    },
                    valueRange = 0.0f..2.0f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", tempVal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Max Tokens
            val maxTokensVal = currentPatch.maxTokens ?: defaultMaxTokens ?: 4096
            SettingsParamHeader(
                title = stringResource(R.string.gen_max_tokens),
                isOverridden = currentPatch.maxTokens != null,
                inheritedLabel = stringResource(R.string.setting_origin_inherited_global),
                onReset = {
                    viewModel.settings.updateProviderSettingsPatch(
                        providerName,
                        currentPatch.copy(maxTokens = null),
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = maxTokensVal.coerceIn(1, 32768).toFloat(),
                    onValueChange = {
                        viewModel.settings.updateProviderSettingsPatch(
                            providerName,
                            currentPatch.copy(maxTokens = it.roundToInt()),
                        )
                    },
                    valueRange = 1f..32768f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$maxTokensVal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top P
            val topPVal = currentPatch.topP ?: defaultTopP ?: 1.0f
            SettingsParamHeader(
                title = stringResource(R.string.gen_top_p),
                isOverridden = currentPatch.topP != null,
                inheritedLabel = stringResource(R.string.setting_origin_inherited_global),
                onReset = {
                    viewModel.settings.updateProviderSettingsPatch(
                        providerName,
                        currentPatch.copy(topP = null),
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = topPVal.coerceIn(0.0f, 1.0f),
                    onValueChange = {
                        val rounded = (it * 100).roundToInt() / 100f
                        viewModel.settings.updateProviderSettingsPatch(
                            providerName,
                            currentPatch.copy(topP = rounded),
                        )
                    },
                    valueRange = 0.0f..1.0f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", topPVal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency Penalty
            val freqVal = currentPatch.frequencyPenalty ?: defaultFreq ?: 0.0f
            SettingsParamHeader(
                title = stringResource(R.string.gen_frequency_penalty),
                isOverridden = currentPatch.frequencyPenalty != null,
                inheritedLabel = stringResource(R.string.setting_origin_inherited_global),
                onReset = {
                    viewModel.settings.updateProviderSettingsPatch(
                        providerName,
                        currentPatch.copy(frequencyPenalty = null),
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = freqVal.coerceIn(-2.0f, 2.0f),
                    onValueChange = {
                        val rounded = (it * 100).roundToInt() / 100f
                        viewModel.settings.updateProviderSettingsPatch(
                            providerName,
                            currentPatch.copy(frequencyPenalty = rounded),
                        )
                    },
                    valueRange = -2.0f..2.0f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", freqVal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presence Penalty
            val presVal = currentPatch.presencePenalty ?: defaultPres ?: 0.0f
            SettingsParamHeader(
                title = stringResource(R.string.gen_presence_penalty),
                isOverridden = currentPatch.presencePenalty != null,
                inheritedLabel = stringResource(R.string.setting_origin_inherited_global),
                onReset = {
                    viewModel.settings.updateProviderSettingsPatch(
                        providerName,
                        currentPatch.copy(presencePenalty = null),
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = presVal.coerceIn(-2.0f, 2.0f),
                    onValueChange = {
                        val rounded = (it * 100).roundToInt() / 100f
                        viewModel.settings.updateProviderSettingsPatch(
                            providerName,
                            currentPatch.copy(presencePenalty = rounded),
                        )
                    },
                    valueRange = -2.0f..2.0f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.2f", presVal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
