package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils

/**
 * Authentic Material 3 Expressive Settings Item (Tonal container, no harsh borders)
 */
@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bold: Boolean = false,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    val cornerShape = when {
        isFirst && isLast -> RoundedCornerShape(18.dp)
        isFirst -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        isLast -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
        else -> RoundedCornerShape(4.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cornerShape)
            .background(colorScheme.surfaceContainer)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colorScheme.primary)
            ) { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    color = colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = description,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun NavSettingsItem(
    title: String,
    description: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bold: Boolean = false,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    SettingsItem(
        title = title,
        description = description,
        isFirst = isFirst,
        isLast = isLast,
        bold = bold,
        onClick = onClick,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

@Composable
fun SwitchSettingsItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bold: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    SettingsItem(
        title = title,
        description = description,
        isFirst = isFirst,
        isLast = isLast,
        bold = bold,
        onClick = { onCheckedChange(!checked) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.onPrimary,
                    checkedTrackColor = colorScheme.primary,
                    uncheckedThumbColor = colorScheme.outline,
                    uncheckedTrackColor = colorScheme.surfaceContainerHighest
                )
            )
        }
    )
}

@Composable
fun LinkSettingsItem(
    title: String,
    url: String,
    description: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bold: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    SettingsItem(
        title = title,
        description = description ?: url,
        isFirst = isFirst,
        isLast = isLast,
        bold = bold,
        onClick = { PlatformUtils.openUrl(url) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

@Composable
fun ActionSettingsItem(
    title: String,
    description: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bold: Boolean = false,
    onClick: () -> Unit
) {
    SettingsItem(
        title = title,
        description = description,
        isFirst = isFirst,
        isLast = isLast,
        bold = bold,
        onClick = onClick
    )
}
