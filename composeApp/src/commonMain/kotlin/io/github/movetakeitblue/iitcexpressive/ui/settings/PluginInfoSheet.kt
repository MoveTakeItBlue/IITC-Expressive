package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.model.Plugin
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils

/**
 * Authentic Material 3 Expressive Plugin Details Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginInfoSheet(
    plugin: Plugin,
    installMode: Boolean = false,
    onInstall: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colorScheme.surfaceContainerLow,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row: Icon + Name + Category
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        color = colorScheme.onSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plugin.category.ifBlank { L("plugin_info.default_category") },
                        color = colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // User / Override badge notice
            if (plugin.override) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = L("plugin_info.notice.override"),
                        color = colorScheme.onTertiaryContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (plugin.user) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = L("plugin_info.notice.user"),
                        color = colorScheme.onSecondaryContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Description Card
            if (plugin.description.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = plugin.description,
                        color = colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Metadata Chips / Table
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (plugin.version.isNotBlank()) {
                        InfoRow(label = L("plugin_info.version"), value = "v${plugin.version}")
                    }
                    if (plugin.author.isNotBlank()) {
                        InfoRow(label = L("plugin_info.author"), value = plugin.author)
                    }
                    if (plugin.id.isNotBlank()) {
                        InfoRow(label = "ID", value = plugin.id)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            if (installMode) {
                FilledTonalButton(
                    onClick = {
                        onInstall?.invoke()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(L("plugin_info.install"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!plugin.downloadURL.isNullOrBlank()) {
                        FilledTonalButton(
                            onClick = { PlatformUtils.openUrl(plugin.downloadURL) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colorScheme.surfaceContainerHigh,
                                contentColor = colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L("plugin_info.website"), color = colorScheme.onSurface, fontSize = 13.sp)
                        }
                    }

                    if (onDelete != null) {
                        FilledTonalButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colorScheme.errorContainer,
                                contentColor = colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L("plugin_info.delete"), color = colorScheme.error, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(text = value, color = colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
