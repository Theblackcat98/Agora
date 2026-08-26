package com.newoether.agora.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.ContextBudget
import com.newoether.agora.model.ModelId
import com.newoether.agora.model.ThinkingLevels
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.profile.ModelProfileRegistry
import com.newoether.agora.model.settings.ModelSettingsPatch
import com.newoether.agora.ui.components.clearFocusOnTap
import com.newoether.agora.viewmodel.ChatViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelSettingsDialog(
    modelId: String,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parsed = remember(modelId) { ModelId.parse(modelId) }
    val canonicalKey = "${parsed.providerName}:${parsed.modelName}"
    val providerName = parsed.providerName

    val profile = remember(modelId) { ModelProfileRegistry.resolve(modelId) }
    val capabilities = profile.capabilities

    val providerSettings by viewModel.settings.providerSettings.collectAsState()
    val modelSettings by viewModel.settings.modelSettings.collectAsState()
    val defaultTemp by viewModel.settings.defaultTemperature.collectAsState()
    val defaultMaxTokens by viewModel.settings.defaultMaxTokens.collectAsState()
    val defaultTopP by viewModel.settings.defaultTopP.collectAsState()
    val defaultFreq by viewModel.settings.defaultFrequencyPenalty.collectAsState()
    val defaultPres by viewModel.settings.defaultPresencePenalty.collectAsState()

    val providerPatch = providerSettings[providerName] ?: ModelSettingsPatch()
    val initialModelPatch = modelSettings[canonicalKey] ?: modelSettings[modelId] ?: ModelSettingsPatch()

    var currentPatch by remember(initialModelPatch) { mutableStateOf(initialModelPatch) }

    // Inherited values (Provider → Global)
    val inheritedTemp = providerPatch.temperature ?: defaultTemp ?: 1.0f
    val isTempFromProvider = providerPatch.temperature != null

    val inheritedMaxTokens = providerPatch.maxTokens ?: defaultMaxTokens ?: capabilities.maxOutputTokens ?: 4096
    val isMaxTokensFromProvider = providerPatch.maxTokens != null

    val inheritedTopP = providerPatch.topP ?: defaultTopP ?: 1.0f
    val isTopPFromProvider = providerPatch.topP != null

    val inheritedFreq = providerPatch.frequencyPenalty ?: defaultFreq ?: 0.0f
    val isFreqFromProvider = providerPatch.frequencyPenalty != null

    val inheritedPres = providerPatch.presencePenalty ?: defaultPres ?: 0.0f
    val isPresFromProvider = providerPatch.presencePenalty != null

    val providerInheritedLabel = stringResource(R.string.setting_origin_inherited_provider)
    val globalInheritedLabel = stringResource(R.string.setting_origin_inherited_global)

    AlertDialog(
        modifier = modifier.clearFocusOnTap(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = profile.displayName.ifBlank { parsed.modelName },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "${parsed.providerName} • ${parsed.modelName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Capability chips
                Text(
                    text = stringResource(R.string.model_capabilities_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Context: ${ContextBudget.compactLabel(capabilities.maxContextTokens)}") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                    capabilities.maxOutputTokens?.let {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Max Output: $it") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    }
                    if (capabilities.thinking.supported) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.thinking)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                    if (capabilities.supportsVision) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.supports_vision)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }
                    if (capabilities.supportsToolCalling) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.supports_tools)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temperature
                if (capabilities.supportsTemperature) {
                    val range = capabilities.temperatureRange ?: ModelCapabilities.TEMPERATURE_STANDARD
                    val tempVal = currentPatch.temperature ?: inheritedTemp
                    SettingsParamHeader(
                        title = stringResource(R.string.gen_temperature),
                        isOverridden = currentPatch.temperature != null,
                        inheritedLabel = if (isTempFromProvider) providerInheritedLabel else globalInheritedLabel,
                        onReset = { currentPatch = currentPatch.copy(temperature = null) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = tempVal.coerceIn(range.min, range.max),
                            onValueChange = {
                                val rounded = (it * 100).roundToInt() / 100f
                                currentPatch = currentPatch.copy(temperature = rounded)
                            },
                            valueRange = range.min..range.max,
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
                }

                // Max Tokens
                val maxOutput = capabilities.maxOutputTokens ?: 32768
                val maxTokensVal = (currentPatch.maxTokens ?: inheritedMaxTokens).coerceIn(1, maxOutput)
                SettingsParamHeader(
                    title = stringResource(R.string.gen_max_tokens),
                    isOverridden = currentPatch.maxTokens != null,
                    inheritedLabel = if (isMaxTokensFromProvider) providerInheritedLabel else globalInheritedLabel,
                    onReset = { currentPatch = currentPatch.copy(maxTokens = null) },
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = maxTokensVal.toFloat(),
                        onValueChange = {
                            currentPatch = currentPatch.copy(maxTokens = it.roundToInt())
                        },
                        valueRange = 1f..maxOutput.toFloat(),
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
                if (capabilities.supportsTopP) {
                    val range = capabilities.topPRange ?: ModelCapabilities.TOP_P_STANDARD
                    val topPVal = currentPatch.topP ?: inheritedTopP
                    SettingsParamHeader(
                        title = stringResource(R.string.gen_top_p),
                        isOverridden = currentPatch.topP != null,
                        inheritedLabel = if (isTopPFromProvider) providerInheritedLabel else globalInheritedLabel,
                        onReset = { currentPatch = currentPatch.copy(topP = null) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = topPVal.coerceIn(range.min, range.max),
                            onValueChange = {
                                val rounded = (it * 100).roundToInt() / 100f
                                currentPatch = currentPatch.copy(topP = rounded)
                            },
                            valueRange = range.min..range.max,
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
                }

                // Frequency Penalty
                if (capabilities.supportsFrequencyPenalty) {
                    val range = capabilities.frequencyPenaltyRange ?: ModelCapabilities.PENALTY_RANGE_DEFAULT
                    val freqVal = currentPatch.frequencyPenalty ?: inheritedFreq
                    SettingsParamHeader(
                        title = stringResource(R.string.gen_frequency_penalty),
                        isOverridden = currentPatch.frequencyPenalty != null,
                        inheritedLabel = if (isFreqFromProvider) providerInheritedLabel else globalInheritedLabel,
                        onReset = { currentPatch = currentPatch.copy(frequencyPenalty = null) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = freqVal.coerceIn(range.min, range.max),
                            onValueChange = {
                                val rounded = (it * 100).roundToInt() / 100f
                                currentPatch = currentPatch.copy(frequencyPenalty = rounded)
                            },
                            valueRange = range.min..range.max,
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
                }

                // Presence Penalty
                if (capabilities.supportsPresencePenalty) {
                    val range = capabilities.presencePenaltyRange ?: ModelCapabilities.PENALTY_RANGE_DEFAULT
                    val presVal = currentPatch.presencePenalty ?: inheritedPres
                    SettingsParamHeader(
                        title = stringResource(R.string.gen_presence_penalty),
                        isOverridden = currentPatch.presencePenalty != null,
                        inheritedLabel = if (isPresFromProvider) providerInheritedLabel else globalInheritedLabel,
                        onReset = { currentPatch = currentPatch.copy(presencePenalty = null) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = presVal.coerceIn(range.min, range.max),
                            onValueChange = {
                                val rounded = (it * 100).roundToInt() / 100f
                                currentPatch = currentPatch.copy(presencePenalty = rounded)
                            },
                            valueRange = range.min..range.max,
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Thinking
                if (capabilities.thinking.supported) {
                    val thinkingCap = capabilities.thinking
                    val supportedLevels = if (thinkingCap.supportedLevels.isNotEmpty()) {
                        thinkingCap.supportedLevels
                    } else {
                        ThinkingLevels.ALL_LEVELS
                    }
                    val currentEffort = currentPatch.thinkingLevel ?: ThinkingLevels.DefaultEffort
                    val currentIndex = supportedLevels.indexOf(currentEffort).coerceAtLeast(0)

                    SettingsParamHeader(
                        title = stringResource(R.string.gen_thinking_level),
                        isOverridden = currentPatch.thinkingLevel != null,
                        inheritedLabel = globalInheritedLabel,
                        onReset = { currentPatch = currentPatch.copy(thinkingLevel = null) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = currentIndex.toFloat(),
                            onValueChange = {
                                val idx = it.roundToInt().coerceIn(0, supportedLevels.lastIndex)
                                currentPatch = currentPatch.copy(thinkingLevel = supportedLevels[idx])
                            },
                            valueRange = 0f..supportedLevels.lastIndex.toFloat(),
                            steps = if (supportedLevels.size > 2) supportedLevels.size - 2 else 0,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = currentEffort,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPatch.isAllNull()) {
                        viewModel.settings.resetModelSettingsPatch(canonicalKey)
                        viewModel.settings.resetModelSettingsPatch(modelId)
                    } else {
                        viewModel.settings.updateModelSettingsPatch(canonicalKey, currentPatch)
                    }
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.provider_save))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        currentPatch = ModelSettingsPatch()
                        viewModel.settings.resetModelSettingsPatch(canonicalKey)
                        viewModel.settings.resetModelSettingsPatch(modelId)
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.gen_reset))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.provider_cancel))
                }
            }
        },
    )
}
