package com.newoether.agora.ui.chat.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.ui.common.openAiServiceTierShortLabel
import com.newoether.agora.ui.common.thinkingControlShortLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatComposerToolsMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    isModelValid: Boolean,
    capabilities: ModelCapabilities,
    thinkingEnabled: Boolean,
    thinkingLevel: String,
    thinkingBudgetEnabled: Boolean,
    thinkingBudgetTokens: Int,
    onThinkingToggle: (Boolean) -> Unit,
    onOpenThinkingSheet: () -> Unit,
    codeExecutionEnabled: Boolean,
    onCodeExecutionToggle: (Boolean) -> Unit,
    googleSearchEnabled: Boolean,
    onGoogleSearchToggle: (Boolean) -> Unit,
    openAiWebSearchAvailable: Boolean,
    openAiWebSearchEnabled: Boolean,
    onOpenAiWebSearchToggle: (Boolean) -> Unit,
    openAiServiceTierAvailable: Boolean,
    openAiServiceTierEnabled: Boolean,
    openAiServiceTier: String,
    onOpenAiServiceTierToggle: (Boolean) -> Unit,
    onOpenOpenAiServiceTierSheet: () -> Unit,
    showWebSearch: Boolean,
    webSearchEnabled: Boolean,
    onWebSearchToggle: (Boolean) -> Unit,
    showShell: Boolean,
    shellEnabled: Boolean,
    onShellToggle: (Boolean) -> Unit,
    canCompact: Boolean,
    isCompacting: Boolean,
    onCompactClick: () -> Unit,
    onAdvancedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        IconButton(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier
                .size(32.dp)
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
        ) {
            Icon(
                Icons.Default.MoreVert,
                stringResource(R.string.tools),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ExposedDropdownMenu(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            matchTextFieldWidth = false,
            shape = CHAT_DROPDOWN_MENU_SHAPE,
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(id = R.drawable.neurology_24),
                            null,
                            modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.thinking))
                            Text(
                                text = thinkingControlShortLabel(
                                    thinkingEnabled,
                                    thinkingLevel,
                                    thinkingBudgetEnabled,
                                    thinkingBudgetTokens,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                trailingIcon = {
                    Switch(
                        checked = thinkingEnabled,
                        onCheckedChange = { onThinkingToggle(it) },
                        modifier = Modifier.scale(0.7f),
                    )
                },
                onClick = {
                    onDismissRequest()
                    onOpenThinkingSheet()
                },
            )

            if (capabilities.supportsCodeExecution && isModelValid) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Terminal,
                                null,
                                modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.code_execution))
                            Spacer(modifier = Modifier.width(10.dp))
                            ProviderBadge("Gemini")
                        }
                    },
                    trailingIcon = {
                        Switch(
                            checked = codeExecutionEnabled,
                            onCheckedChange = { onCodeExecutionToggle(it) },
                            modifier = Modifier.scale(0.7f),
                        )
                    },
                    onClick = { onCodeExecutionToggle(!codeExecutionEnabled) },
                )
            }

            if (capabilities.supportsNativeSearch && isModelValid) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.provider_google),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.google_search))
                            Spacer(modifier = Modifier.width(10.dp))
                            ProviderBadge("Gemini")
                        }
                    },
                    trailingIcon = {
                        Switch(
                            checked = googleSearchEnabled,
                            onCheckedChange = { onGoogleSearchToggle(it) },
                            modifier = Modifier.scale(0.7f),
                        )
                    },
                    onClick = { onGoogleSearchToggle(!googleSearchEnabled) },
                )
            }

            if (openAiServiceTierAvailable && isModelValid) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.openai_service_tier_title))
                                Text(
                                    text = openAiServiceTierShortLabel(
                                        openAiServiceTierEnabled,
                                        openAiServiceTier,
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        Switch(
                            checked = openAiServiceTierEnabled,
                            onCheckedChange = onOpenAiServiceTierToggle,
                            modifier = Modifier.scale(0.7f),
                        )
                    },
                    onClick = {
                        onDismissRequest()
                        onOpenOpenAiServiceTierSheet()
                    },
                )
            }

            if (openAiWebSearchAvailable && isModelValid) {
                NativeSearchMenuItem(
                    checked = openAiWebSearchEnabled,
                    provider = "OpenAI",
                    onCheckedChange = onOpenAiWebSearchToggle,
                )
            }

            if (showWebSearch) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Language,
                                null,
                                modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.web_search))
                        }
                    },
                    trailingIcon = {
                        Switch(
                            checked = webSearchEnabled,
                            onCheckedChange = { onWebSearchToggle(it) },
                            modifier = Modifier.scale(0.7f),
                        )
                    },
                    onClick = { onWebSearchToggle(!webSearchEnabled) },
                )
            }

            if (showShell) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Terminal,
                                null,
                                modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.shell_title))
                        }
                    },
                    trailingIcon = {
                        Switch(
                            checked = shellEnabled,
                            onCheckedChange = { onShellToggle(it) },
                            modifier = Modifier.scale(0.7f),
                        )
                    },
                    onClick = { onShellToggle(!shellEnabled) },
                )
            }

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Compress,
                            null,
                            modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.context_compact))
                    }
                },
                enabled = canCompact && !isCompacting,
                onClick = {
                    onDismissRequest()
                    onCompactClick()
                },
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            null,
                            modifier = Modifier.size(CHAT_DROPDOWN_MENU_ICON_SIZE_DP.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.advanced_settings))
                    }
                },
                onClick = {
                    onDismissRequest()
                    onAdvancedClick()
                },
            )
        }
    }
}
