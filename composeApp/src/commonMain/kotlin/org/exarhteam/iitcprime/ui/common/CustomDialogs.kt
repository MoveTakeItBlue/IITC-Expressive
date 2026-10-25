package org.exarhteam.iitcprime.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.theme.*
import org.exarhteam.iitcprime.viewmodel.AppDialog

/**
 * Material 3 Expressive Dialogs Host
 */
@Composable
fun AppDialogHost(
    dialog: AppDialog?,
    onDismiss: () -> Unit
) {
    if (dialog == null) return

    when (dialog) {
        is AppDialog.Alert -> {
            AlertDialog(
                title = dialog.title,
                message = dialog.message,
                onDismiss = {
                    dialog.onDismiss()
                    onDismiss()
                }
            )
        }
        is AppDialog.Confirm -> {
            ConfirmDialog(
                title = dialog.title,
                message = dialog.message,
                onConfirm = {
                    dialog.onConfirm()
                    onDismiss()
                },
                onCancel = {
                    dialog.onCancel()
                    onDismiss()
                }
            )
        }
        is AppDialog.LocationShare -> {
            LocationShareDialog(
                dialog = dialog,
                onDismiss = onDismiss
            )
        }
        is AppDialog.AddPlugin -> {}
        is AppDialog.PluginInfo -> {}
    }
}

/**
 * Material 3 Expressive Alert Dialog
 */
@Composable
fun AlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = OnSurfaceWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = OnSurfaceVariantMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AccentContainerTeal,
                    contentColor = AccentTeal
                ),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(L("dialog.ok"), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = SurfaceContainerDark
    )
}

/**
 * Material 3 Expressive Confirm Dialog
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                color = OnSurfaceWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = OnSurfaceVariantMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AccentContainerTeal,
                    contentColor = AccentTeal
                ),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(L("dialog.ok"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(L("dialog.cancel"), color = OnSurfaceVariantMuted, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = SurfaceContainerDark
    )
}

/**
 * Material 3 Expressive Location Share Dialog
 */
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentContainerTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPortal) Icons.Default.Place else Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (title.isNotBlank()) title else L("share.title"),
                    color = OnSurfaceWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$lat, $lng",
                    color = OnSurfaceVariantMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val coordStr = "$lat,$lng"
                            PlatformUtils.copyToClipboard(coordStr, L("share.toast.coordinates_copied"))
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SurfaceContainerHighDark,
                            contentColor = OnSurfaceWhite
                        )
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(L("share.copy"), fontSize = 13.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            val geoUrl = "geo:$lat,$lng?q=$lat,$lng($title)"
                            PlatformUtils.openUrl(geoUrl)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentContainerTeal,
                            contentColor = AccentTeal
                        )
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(L("share.open_map"), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Selection Dialog
 */
@Composable
fun SelectFieldDialog(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp).padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp)) {
                Text(
                    text = title,
                    color = OnSurfaceWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        val isSelected = index == selectedIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SurfaceContainerHighestDark else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AccentTeal),
                                    onClick = {
                                        onSelect(index)
                                        onDismiss()
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) AccentTeal else OnSurfaceWhite,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text(L("dialog.cancel"), color = OnSurfaceVariantMuted)
                    }
                }
            }
        }
    }
}
