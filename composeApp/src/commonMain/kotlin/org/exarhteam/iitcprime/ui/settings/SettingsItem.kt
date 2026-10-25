package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.theme.SurfaceContainer

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
    val cornerShape = when {
        isFirst && isLast -> RoundedCornerShape(12.dp)
        isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cornerShape)
            .background(SurfaceContainer)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }

        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 16.dp)
            )
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
    isLast: Boolean = false
) {
    SettingsItem(
        title = title,
        description = description,
        isFirst = isFirst,
        isLast = isLast,
        onClick = { onCheckedChange(!checked) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun LinkSettingsItem(
    title: String,
    url: String,
    description: String? = url,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    SettingsItem(
        title = title,
        description = description,
        isFirst = isFirst,
        isLast = isLast,
        onClick = { PlatformUtils.openUrl(url) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
