package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.Plugin
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.theme.*

/**
 * Material 3 Expressive Plugin Details BottomSheet
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerDark,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AccentContainerTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        color = OnSurfaceWhite,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plugin.category.ifBlank { L("plugin_info.default_category") },
                        color = AccentTeal,
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
                    color = TertiaryAmber.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = L("plugin_info.notice.override"),
                        color = TertiaryAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (plugin.user) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentTeal.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = L("plugin_info.notice.user"),
                        color = AccentTeal,
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
                    color = SurfaceContainerHighDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = plugin.description,
                        color = OnSurfaceWhite,
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
                color = SurfaceContainerHighDark,
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
                        containerColor = AccentTeal,
                        contentColor = Color.Black
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
                        OutlinedButton(
                            onClick = { plugin.downloadURL?.let { PlatformUtils.openUrl(it) } },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(percent = 50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L("plugin_info.website"), color = OnSurfaceWhite, fontSize = 13.sp)
                        }
                    }

                    if (onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(percent = 50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StateError.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = StateError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L("plugin_info.delete"), color = StateError, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = OnSurfaceVariantMuted, fontSize = 13.sp)
        Text(text = value, color = OnSurfaceWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
