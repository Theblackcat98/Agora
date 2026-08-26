package com.newoether.agora.ui.chat.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.settings.ModelSettingsPatch
import com.newoether.agora.ui.components.clearFocusOnTap

@Composable
fun ConversationSettingsDialog(
    initialPatch: ModelSettingsPatch,
    modelPatch: ModelSettingsPatch?,
    providerPatch: ModelSettingsPatch?,
    globalPatch: ModelSettingsPatch,
    capabilities: ModelCapabilities,
    onSave: (ModelSettingsPatch) -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentPatch by remember(initialPatch) { mutableStateOf(initialPatch) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val resolved = remember(currentPatch, modelPatch, providerPatch, globalPatch, capabilities) {
        ConversationSettingsResolver.resolve(
            conversationPatch = currentPatch,
            modelPatch = modelPatch,
            providerPatch = providerPatch,
            globalPatch = globalPatch,
            capabilities = capabilities,
        )
    }

    val tabTitles = listOf(
        stringResource(R.string.settings_tab_parameters),
        stringResource(R.string.settings_tab_thinking),
        stringResource(R.string.settings_tab_tools),
    )

    AlertDialog(
        modifier = modifier.clearFocusOnTap(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.conversation_parameters_title),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                ) {
                    when (selectedTab) {
                        0 -> ConversationNumericParamsSection(
                            resolved = resolved,
                            capabilities = capabilities,
                            currentPatch = currentPatch,
                            onPatchChange = { currentPatch = it },
                        )
                        1 -> ConversationThinkingSection(
                            resolved = resolved,
                            capabilities = capabilities,
                            currentPatch = currentPatch,
                            onPatchChange = { currentPatch = it },
                        )
                        2 -> ConversationToolsSection(
                            resolved = resolved,
                            capabilities = capabilities,
                            currentPatch = currentPatch,
                            onPatchChange = { currentPatch = it },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currentPatch) },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        currentPatch = ModelSettingsPatch()
                        onResetAll()
                    },
                ) {
                    Text(stringResource(R.string.reset_all_overrides))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    )
}
