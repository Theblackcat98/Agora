package com.newoether.agora.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import com.newoether.agora.ui.theme.ChatType

enum class SettingsInheritanceLevel {
    GLOBAL_DEFAULT,
    PROVIDER_DEFAULT,
    MODEL_OVERRIDE,
}

@Composable
fun SettingsInheritanceBadge(
    level: SettingsInheritanceLevel,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor, textRes) = when (level) {
        SettingsInheritanceLevel.GLOBAL_DEFAULT -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            R.string.setting_origin_inherited_global,
        )
        SettingsInheritanceLevel.PROVIDER_DEFAULT -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            R.string.setting_origin_inherited_provider,
        )
        SettingsInheritanceLevel.MODEL_OVERRIDE -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            R.string.setting_origin_overridden,
        )
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(textRes),
            style = ChatType.micro,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun SettingsParamHeader(
    title: String,
    isOverridden: Boolean,
    inheritedLabel: String,
    onReset: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (isOverridden) {
            SettingsInheritanceBadge(level = SettingsInheritanceLevel.MODEL_OVERRIDE)
            if (onReset != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = stringResource(R.string.gen_reset),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = inheritedLabel,
                    style = ChatType.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}
