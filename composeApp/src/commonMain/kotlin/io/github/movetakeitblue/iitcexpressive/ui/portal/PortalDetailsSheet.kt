package io.github.movetakeitblue.iitcexpressive.ui.portal

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.model.*
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.theme.*
import io.github.movetakeitblue.iitcexpressive.ui.common.ExpressiveCircularProgressIndicator
import io.github.movetakeitblue.iitcexpressive.ui.common.ExpressiveLinearProgressIndicator
import io.github.movetakeitblue.iitcexpressive.viewmodel.AppDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel

/**
 * Material 3 Expressive Portal Details Bottom Sheet
 * Seamlessly transitions between a collapsed peek bar and a full expanded details sheet.
 * Fully compliant with Material 3 Expressive guidelines, dynamic color, and tonal depth.
 */
@Composable
fun PortalDetailsSheet(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val portalStatus by viewModel.portalStatus.collectAsState()
    val isExpanded by viewModel.isPortalDetailsExpanded.collectAsState()

    if (!portalStatus.hasSelected) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val peekHeight = 88.dp + navBottom
        val expandedHeight = (screenHeight * 0.85f).coerceAtLeast(460.dp)

        val animatedHeight by animateDpAsState(
            targetValue = if (isExpanded) expandedHeight else peekHeight,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "portalSheetHeight"
        )

        // Scrim backdrop when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.collapsePortalDetails() }
                    )
            )
        }

        var dragOffset by remember { mutableStateOf(0f) }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .align(Alignment.BottomCenter)
                .draggable(
                    state = rememberDraggableState { delta ->
                        dragOffset += delta
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        if (dragOffset < -30f) {
                            viewModel.expandPortalDetails()
                        } else if (dragOffset > 30f) {
                            if (isExpanded) {
                                viewModel.collapsePortalDetails()
                            } else {
                                viewModel.deselectPortal()
                            }
                        }
                        dragOffset = 0f
                    }
                ),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (isExpanded) viewModel.collapsePortalDetails()
                                else viewModel.expandPortalDetails()
                            }
                        )
                        .padding(top = 10.dp, bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }

                if (!isExpanded) {
                    // Collapsed Peek Bar
                    PortalPeekBar(
                        portalStatus = portalStatus,
                        onExpand = { viewModel.expandPortalDetails() },
                        onClose = { viewModel.deselectPortal() }
                    )
                } else {
                    // Full Expanded Details View
                    PortalExpandedDetails(
                        portalStatus = portalStatus,
                        viewModel = viewModel,
                        onCollapse = { viewModel.collapsePortalDetails() },
                        onClose = { viewModel.deselectPortal() },
                        onShare = {
                            val lat = portalStatus.lat ?: 0.0
                            val lng = portalStatus.lng ?: 0.0
                            viewModel.setDialog(
                                AppDialog.LocationShare(
                                    lat = lat,
                                    lng = lng,
                                    title = portalStatus.title,
                                    isPortal = true,
                                    guid = portalStatus.guid
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Collapsed Peek Bar (Modern M3 Expressive Figure 3 Style)
 */
@Composable
private fun PortalPeekBar(
    portalStatus: PortalStatus,
    onExpand: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = portalStatus.teamColor),
                    onClick = onExpand
                )
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Team & Level Pill
                Surface(
                    color = portalStatus.teamContainerColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "L${portalStatus.level}",
                        color = portalStatus.onTeamContainerColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                // Health %
                Text(
                    text = "${portalStatus.health}%",
                    color = if (portalStatus.health > 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Portal Title
                Text(
                    text = portalStatus.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Resonator 8-slot energy bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val resonators = portalStatus.resonators ?: emptyList()
                for (i in 0..7) {
                    val res = resonators.getOrNull(i)
                    val pct = res?.healthPct ?: 0
                    val col = res?.m3Color ?: portalStatus.teamColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (pct > 0) col.copy(alpha = (pct / 100f).coerceIn(0.35f, 1f))
                                else Color.Transparent
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Expand chevron button
        IconButton(
            onClick = onExpand,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Expand portal details",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Deselect button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Deselect portal",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Full Expanded Portal Details
 */
@Composable
private fun PortalExpandedDetails(
    portalStatus: PortalStatus,
    viewModel: MainViewModel,
    onCollapse: () -> Unit,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    // Dialog state for showing deployed agent name on click
    var selectedModForDetail by remember { mutableStateOf<PortalMod?>(null) }
    var selectedResonatorForDetail by remember { mutableStateOf<Pair<String, Resonator>?>(null) } // (direction, resonator)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = portalStatus.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            // Share action
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Close / Deselect
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Hero Photo & Overview Card (Clean M3 surfaceContainerLow without harsh borders)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Portal Photo
                    PortalAsyncImage(
                        url = portalStatus.image,
                        teamColor = portalStatus.teamColor,
                        httpClient = viewModel.manager.httpClient,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )

                    // Metadata
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badges Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = portalStatus.teamContainerColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = portalStatus.teamType.name,
                                    color = portalStatus.onTeamContainerColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Level ${portalStatus.level}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Owner
                        val ownerText = if (!portalStatus.owner.isNullOrBlank()) {
                            L("portal.captured_by", portalStatus.owner)
                        } else if (portalStatus.isNeutral) {
                            L("portal.unclaimed")
                        } else {
                            portalStatus.teamType.name
                        }
                        Text(
                            text = ownerText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Coordinates chip
                        val lat = portalStatus.lat
                        val lng = portalStatus.lng
                        if (lat != null && lng != null) {
                            val coordStr = "${"%.6f".format(lat)}, ${"%.6f".format(lng)}"
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    PlatformUtils.copyToClipboard(coordStr, L("share.toast.coordinates_copied"))
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy coordinates",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = coordStr,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Key Metrics Grid Card (Clean M3 surfaceContainerLow)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricItem(label = "Range", value = portalStatus.formattedRange, modifier = Modifier.weight(1f))
                    MetricItem(label = "Links", value = "${portalStatus.linksOut} out / ${portalStatus.linksIn} in", modifier = Modifier.weight(1.2f))
                    MetricItem(label = "Fields", value = "${portalStatus.fieldsCount}", modifier = Modifier.weight(0.8f))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val energyStr = if (portalStatus.maxEnergy > 0) {
                        "${portalStatus.energy / 1000}k / ${portalStatus.maxEnergy / 1000}k"
                    } else "${portalStatus.health}%"
                    MetricItem(label = "Energy", value = energyStr, modifier = Modifier.weight(1f))

                    val apStr = if (portalStatus.apGain > 0) "${portalStatus.apGain} AP" else "--"
                    MetricItem(label = "AP Gain", value = apStr, modifier = Modifier.weight(1.2f))

                    MetricItem(label = "Health", value = "${portalStatus.health}%", modifier = Modifier.weight(0.8f))
                }

                // Energy Progress
                if (portalStatus.maxEnergy > 0) {
                    val ratio = (portalStatus.energy.toFloat() / portalStatus.maxEnergy.toFloat()).coerceIn(0f, 1f)
                    ExpressiveLinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = if (ratio > 0.5f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }

        // Mods Section (4 interactive slots - clicking displays deploying agent name)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = L("portal.mods").uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Tap to inspect",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mods = portalStatus.mods ?: listOf(PortalMod(), PortalMod(), PortalMod(), PortalMod())
                for (i in 0..3) {
                    val mod = mods.getOrNull(i)
                    val hasMod = mod != null && !mod.name.isNullOrBlank()

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .clickable(enabled = hasMod) {
                                if (hasMod) selectedModForDetail = mod
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasMod) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasMod) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = mod.name,
                                        color = mod.rarityColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!mod.rarity.isNullOrBlank()) {
                                        Text(
                                            text = mod.rarity,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Empty",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Resonators Section (8 interactive slots - clicking displays deploying agent name)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = L("portal.resonators").uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Tap to inspect agent",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            val resonators = portalStatus.resonators ?: emptyList()
            val compassDirections = listOf("E", "NE", "N", "NW", "W", "SW", "S", "SE")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (chunk in (0..7).chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in chunk) {
                            val res = resonators.getOrNull(i)
                            val hasRes = res != null && res.level > 0
                            val dir = compassDirections.getOrNull(i) ?: "$i"
                            val col = res?.m3Color ?: portalStatus.teamColor

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable(enabled = hasRes) {
                                        if (res != null) {
                                            selectedResonatorForDetail = Pair(dir, res)
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dir,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(22.dp)
                                    )

                                    if (hasRes) {
                                        Surface(
                                            color = col.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "L${res.level}",
                                                color = col,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            val displayText = if (!res.owner.isNullOrBlank()) res.owner else "${res.energy} XM"
                                            Text(
                                                text = displayText,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            ExpressiveLinearProgressIndicator(
                                                progress = { (res.healthPct / 100f).coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                                color = col,
                                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Empty",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // History status badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryChip(label = "Visited", active = portalStatus.historyVisited, modifier = Modifier.weight(1f))
            HistoryChip(label = "Captured", active = portalStatus.historyCaptured, modifier = Modifier.weight(1f))
            HistoryChip(label = "Scout Controlled", active = portalStatus.historyScoutControlled, modifier = Modifier.weight(1.3f))
        }

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val lat = portalStatus.lat
            val lng = portalStatus.lng

            if (lat != null && lng != null) {
                Button(
                    onClick = {
                        val uri = "geo:$lat,$lng?q=$lat,$lng(${portalStatus.title})"
                        PlatformUtils.openUrl(uri)
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Navigate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            FilledTonalButton(
                onClick = onShare,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Share", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    // Interactive Mod Detail Inspection Dialog
    selectedModForDetail?.let { mod ->
        AlertDialog(
            onDismissRequest = { selectedModForDetail = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = mod.rarityColor,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = mod.name ?: "Mod Detail",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!mod.rarity.isNullOrBlank()) {
                        Surface(
                            color = mod.rarityColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = mod.rarity,
                                color = mod.rarityColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Deployed Agent Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (!mod.owner.isNullOrBlank()) "Deployed by: ${mod.owner}" else "Deployed by: Unknown Agent",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Stats if present
                    val stats = mod.stats
                    if (stats != null && stats.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            stats.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = k, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(text = v, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedModForDetail = null }
                ) {
                    Text(text = "Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Interactive Resonator Detail Inspection Dialog
    selectedResonatorForDetail?.let { (direction, res) ->
        val dirFullName = when (direction) {
            "E" -> "East"
            "NE" -> "North-East"
            "N" -> "North"
            "NW" -> "North-West"
            "W" -> "West"
            "SW" -> "South-West"
            "S" -> "South"
            "SE" -> "South-East"
            else -> direction
        }

        AlertDialog(
            onDismissRequest = { selectedResonatorForDetail = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = res.m3Color,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "$dirFullName ($direction) • Level ${res.level}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Deployed Agent Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (!res.owner.isNullOrBlank()) "Deployed by: ${res.owner}" else "Deployed by: Unknown Agent",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Energy details
                    val maxForLvl = listOf(0, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 6000).getOrElse(res.level) { 1000 }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Charge", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(
                                text = "${res.energy} / $maxForLvl XM (${res.healthPct}%)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ExpressiveLinearProgressIndicator(
                            progress = { (res.healthPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = res.m3Color,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedResonatorForDetail = null }
                ) {
                    Text(text = "Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (active) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PortalAsyncImage(
    url: String?,
    teamColor: Color,
    httpClient: io.ktor.client.HttpClient,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            imageBitmap = null
            return@LaunchedEffect
        }
        isLoading = true
        try {
            val bytes = withContext(Dispatchers.Default) {
                val resp = httpClient.get(url)
                resp.readRawBytes()
            }
            imageBitmap = PlatformUtils.decodeImageBitmap(bytes)
        } catch (_: Exception) {
            imageBitmap = null
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        val bmp = imageBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isLoading) {
            ExpressiveCircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = teamColor,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
