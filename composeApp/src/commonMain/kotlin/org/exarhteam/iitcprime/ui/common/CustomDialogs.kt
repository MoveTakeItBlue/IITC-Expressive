package org.exarhteam.iitcprime.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.theme.*
import org.exarhteam.iitcprime.viewmodel.AppDialog

@Composable
fun AppDialogHost(
    dialog: AppDialog?,
    onDismiss: () -> Unit
) {
    if (dialog == null) return

    when (dialog) {
        is AppDialog.Alert -> {
            AlertDialog(
                onDismissRequest = {
                    dialog.onDismiss()
                    onDismiss()
                },
                title = { Text(dialog.title, color = OnSurfaceWhite) },
                text = { Text(dialog.message, color = OnSurfaceVariantMuted) },
                confirmButton = {
                    TextButton(onClick = {
                        dialog.onDismiss()
                        onDismiss()
                    }) {
                        Text(L("dialog.ok"), color = AccentTeal)
                    }
                },
                containerColor = SurfaceContainerDark
            )
        }
        is AppDialog.Confirm -> {
            AlertDialog(
                onDismissRequest = {
                    dialog.onCancel()
                    onDismiss()
                },
                title = { Text(dialog.title, color = OnSurfaceWhite) },
                text = { Text(dialog.message, color = OnSurfaceVariantMuted) },
                confirmButton = {
                    TextButton(onClick = {
                        dialog.onConfirm()
                        onDismiss()
                    }) {
                        Text(L("dialog.ok"), color = AccentTeal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        dialog.onCancel()
                        onDismiss()
                    }) {
                        Text(L("dialog.cancel"), color = OnSurfaceVariantMuted)
                    }
                },
                containerColor = SurfaceContainerDark
            )
        }
        is AppDialog.LocationShare -> {
            LocationShareDialog(
                dialog = dialog,
                onDismiss = onDismiss
            )
        }
        is AppDialog.AddPlugin -> {
            // Handled in Settings/Plugins
        }
        is AppDialog.PluginInfo -> {
            // Handled in Settings/Plugins
        }
    }
}

@Composable
fun LocationShareDialog(
    dialog: AppDialog.LocationShare,
    onDismiss: () -> Unit
) {
    val lat = dialog.lat
    val lng = dialog.lng
    val title = dialog.title
    val isPortal = dialog.isPortal
    val guid = dialog.guid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L("share.title"), color = OnSurfaceWhite) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShareOptionRow(
                    icon = Icons.Default.Share,
                    label = L("share.action.text"),
                    onClick = {
                        val text = "${if (title.isNotBlank()) "$title\n" else ""}Location: $lat,$lng"
                        PlatformUtils.shareText(text, title.ifBlank { "Location" })
                        onDismiss()
                    }
                )
                ShareOptionRow(
                    icon = Icons.Default.Map,
                    label = L("share.action.maps"),
                    onClick = {
                        PlatformUtils.shareGeo(lat, lng, title)
                        onDismiss()
                    }
                )
                ShareOptionRow(
                    icon = Icons.Default.ContentCopy,
                    label = L("share.action.coordinates"),
                    onClick = {
                        PlatformUtils.copyToClipboard("$lat,$lng", L("share.toast.coordinates_copied"))
                        onDismiss()
                    }
                )
                ShareOptionRow(
                    icon = Icons.Default.Link,
                    label = L("share.action.link"),
                    onClick = {
                        val url = "https://intel.ingress.com/?ll=$lat,$lng&z=17${if (isPortal) "&pll=$lat,$lng" else ""}"
                        PlatformUtils.shareUrl(url, title.ifBlank { "Intel Map" })
                        onDismiss()
                    }
                )
                if (isPortal || !guid.isNullOrBlank()) {
                    ShareOptionRow(
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        label = L("share.action.ingress_prime"),
                        onClick = {
                            val primeUrl = "https://link.ingress.com/?link=${"https://intel.ingress.com/portal/${guid ?: ""}"}&apn=com.nianticproject.ingress&ofl=${"https://intel.ingress.com/intel?pll=$lat,$lng"}"
                            PlatformUtils.openInBrowser(primeUrl)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(L("dialog.cancel"), color = OnSurfaceVariantMuted)
            }
        },
        containerColor = SurfaceContainerDark
    )
}

@Composable
private fun ShareOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = OnSurfaceWhite, fontSize = 15.sp)
    }
}

@Composable
fun SelectFieldDialog(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = OnSurfaceWhite) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(index)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSelect(index)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentTeal,
                                unselectedColor = OnSurfaceVariantMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item,
                            color = if (isSelected) AccentTeal else OnSurfaceWhite,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(L("dialog.cancel"), color = OnSurfaceVariantMuted)
            }
        },
        containerColor = SurfaceContainerDark
    )
}

@Composable
fun AlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AppDialogHost(AppDialog.Alert(title, message, onDismiss), onDismiss)
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AppDialogHost(AppDialog.Confirm(title, message, onConfirm, onCancel), onCancel)
}

