package io.github.movetakeitblue.iitcexpressive.ui.common

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.model.PlayerTrail
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.theme.*
import io.github.movetakeitblue.iitcexpressive.viewmodel.AppDialog
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        is AppDialog.PlayerTrailInfo -> {
            PlayerTrailDialog(
                trail = dialog.trail,
                onPortalClick = { _, _ -> onDismiss() },
                onDismiss = onDismiss
            )
        }
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(L("dialog.ok"), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                Text(L("dialog.cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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

    // Niantic Ingress Prime Deep Link (makePrimeLink standard)
    val primeLink = if (!guid.isNullOrBlank()) {
        "https://link.ingress.com/?link=https%3A%2F%2Fintel.ingress.com%2Fportal%2F$guid&apn=com.nianticproject.ingress&isi=576505181&ibi=com.google.ingress&ifl=https%3A%2F%2Fapps.apple.com%2Fapp%2Fingress%2Fid576505181&ofl=https%3A%2F%2Fintel.ingress.com%2Fintel%3Fpll%3D$lat%2C$lng"
    } else {
        "https://link.ingress.com/?link=https%3A%2F%2Fintel.ingress.com%2Fintel%3Fpll%3D$lat%2C$lng&apn=com.nianticproject.ingress&isi=576505181&ibi=com.google.ingress&ifl=https%3A%2F%2Fapps.apple.com%2Fapp%2Fingress%2Fid576505181&ofl=https%3A%2F%2Fintel.ingress.com%2Fintel%3Fpll%3D$lat%2C$lng"
    }
    // Standard Intel Map Link
    val intelLink = "https://intel.ingress.com/intel?ll=$lat,$lng&z=17&pll=$lat,$lng"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPortal) Icons.Default.Place else Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (title.isNotBlank()) title else L("share.title"),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$lat, $lng",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isPortal) {
                    // Portal Specific Share Options (M3E Options List)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShareItemCard(
                            title = "Ingress Prime",
                            subtitle = L("share.item.prime_desc"),
                            onCopy = {
                                PlatformUtils.copyToClipboard(primeLink, L("share.toast.prime_copied"))
                                onDismiss()
                            },
                            onOpen = {
                                PlatformUtils.openUrl(primeLink)
                                onDismiss()
                            }
                        )

                        ShareItemCard(
                            title = "Intel Map",
                            subtitle = L("share.item.intel_desc"),
                            onCopy = {
                                PlatformUtils.copyToClipboard(intelLink, L("share.toast.intel_copied"))
                                onDismiss()
                            },
                            onOpen = {
                                PlatformUtils.openUrl(intelLink)
                                onDismiss()
                            }
                        )

                        ShareItemCard(
                            title = L("share.item.coordinates"),
                            subtitle = L("share.item.coordinates_desc"),
                            onCopy = {
                                PlatformUtils.copyToClipboard("$lat,$lng", L("share.toast.coordinates_copied"))
                                onDismiss()
                            },
                            onOpen = {
                                val geoUrl = "geo:$lat,$lng?q=$lat,$lng($title)"
                                PlatformUtils.openUrl(geoUrl)
                                onDismiss()
                            }
                        )
                    }
                } else {
                    // Standard Location Share Actions
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
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L("share.copy"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                val geoUrl = "geo:$lat,$lng?q=$lat,$lng($title)"
                                PlatformUtils.openUrl(geoUrl)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(percent = 50)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L("share.open"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareItemCard(
    title: String,
    subtitle: String,
    onCopy: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(L("share.copy"), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(L("share.open"), fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = MaterialTheme.colorScheme.primary),
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
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
                        Text(L("dialog.cancel"), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

private fun formatEpochTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val h = local.hour.toString().padStart(2, '0')
        val m = local.minute.toString().padStart(2, '0')
        val s = local.second.toString().padStart(2, '0')
        "$h:$m:$s"
    } catch (_: Exception) {
        ""
    }
}

/**
 * Material 3 Expressive Player Activity Tracker Trail Dialog
 */
@Composable
fun PlayerTrailDialog(
    trail: PlayerTrail,
    onPortalClick: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Avatar with Faction Color
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(trail.teamContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = trail.onTeamContainerColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Agent Codename
                Text(
                    text = trail.sender.ifBlank { "Agent" },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Faction Badge & Stops count
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = trail.teamContainerColor
                    ) {
                        Text(
                            text = trail.team.uppercase(),
                            color = trail.onTeamContainerColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = L("player_tracker.locations_count", trail.points.size),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                // Timeline List of Portals
                val displayPoints = remember(trail.points) { trail.points.reversed() }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(displayPoints) { index, point ->
                        val isLatest = index == 0
                        val timeStr = if (point.formattedTime.isNotBlank()) {
                            point.formattedTime
                        } else {
                            formatEpochTime(point.time)
                        }

                        Surface(
                            onClick = { onPortalClick(point.lat, point.lng) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLatest) {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            } else {
                                Color.Transparent
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Timeline Node Indicator
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isLatest) 12.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isLatest) trail.teamColor else MaterialTheme.colorScheme.outline
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Portal Name & Coordinates
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = point.portalName.ifBlank { "Portal" },
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )

                                        if (isLatest) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = trail.teamColor.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = L("player_tracker.latest"),
                                                    color = trail.teamColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    val latShort = (kotlin.math.round(point.lat * 10000) / 10000.0).toString()
                                    val lngShort = (kotlin.math.round(point.lng * 10000) / 10000.0).toString()
                                    Text(
                                        text = "$latShort, $lngShort",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Formatted Time Chip
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = timeStr,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Locate Latest / Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            onPortalClick(trail.latestLat, trail.latestLng)
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(L("player_tracker.focus"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text(L("dialog.close"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
