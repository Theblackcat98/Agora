package com.newoether.agora.ui.chat.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.settings.ModelSettingsPatch

@Composable
fun ConversationToolsSection(
    resolved: ResolvedConversationFields,
    capabilities: ModelCapabilities,
    currentPatch: ModelSettingsPatch,
    onPatchChange: (ModelSettingsPatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Code Execution
        if (capabilities.supportsCodeExecution) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SettingHeaderWithOrigin(
                        title = stringResource(R.string.code_execution),
                        origin = resolved.codeExecutionEnabled.origin,
                        onReset = {
                            onPatchChange(currentPatch.copy(codeExecutionEnabled = null))
                        },
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = resolved.codeExecutionEnabled.value,
                    onCheckedChange = { checked ->
                        onPatchChange(currentPatch.copy(codeExecutionEnabled = checked))
                    },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Native Google Search
        if (capabilities.supportsNativeSearch) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SettingHeaderWithOrigin(
                        title = stringResource(R.string.google_search),
                        origin = resolved.googleSearchEnabled.origin,
                        onReset = {
                            onPatchChange(currentPatch.copy(googleSearchEnabled = null))
                        },
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = resolved.googleSearchEnabled.value,
                    onCheckedChange = { checked ->
                        onPatchChange(currentPatch.copy(googleSearchEnabled = checked))
                    },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Web Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SettingHeaderWithOrigin(
                    title = stringResource(R.string.web_search),
                    origin = resolved.webSearchEnabled.origin,
                    onReset = {
                        onPatchChange(currentPatch.copy(webSearchEnabled = null))
                    },
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = resolved.webSearchEnabled.value,
                onCheckedChange = { checked ->
                    onPatchChange(currentPatch.copy(webSearchEnabled = checked))
                },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Shell
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SettingHeaderWithOrigin(
                    title = stringResource(R.string.shell_title),
                    origin = resolved.shellEnabled.origin,
                    onReset = {
                        onPatchChange(currentPatch.copy(shellEnabled = null))
                    },
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = resolved.shellEnabled.value,
                onCheckedChange = { checked ->
                    onPatchChange(currentPatch.copy(shellEnabled = checked))
                },
            )
        }
    }
}
