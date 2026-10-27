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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
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
 * Portal 璇︽儏 sheet 鐨勫嚑浣曞父閲忋€? * FAB 绛夋偓娴厓绱犲鐢ㄨ繖閲岀殑鍊兼潵璁＄畻閬胯璺濈锛岄伩鍏嶇‖缂栫爜鍋忕Щ瀵艰嚧涓?sheet 閲嶅彔銆? */
object PortalSheetDefaults {
    /** 鏀惰捣鎬侊紙peek锛夊唴瀹归珮搴︼紝涓嶅惈瀵艰埅鏍?inset */
    val PeekHeight = 88.dp

    /** 鎮诞鎸夐挳搴曡竟涓?sheet 涓婃部涔嬮棿淇濈暀鐨勯棿璺?*/
    val FabGap = 16.dp
}

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
        val peekHeight = PortalSheetDefaults.PeekHeight + navBottom
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

                val bookmarkFolders by viewModel.bookmarkFolders.collectAsState()
                val pGuid = portalStatus.guid ?: ""
                val isBookmarked = pGuid.isNotBlank() && bookmarkFolders.any { folder -> folder.bookmarks.any { it.guid == pGuid } }
                val onBookmarkToggle = {
                    val portal = NativePortal(
                        guid = pGuid,
                        lat = portalStatus.lat ?: 0.0,
                        lng = portalStatus.lng ?: 0.0,
                        team = when (portalStatus.team.uppercase()) {
                            "RES", "RESISTANCE" -> 1
                            "ENL", "ENLIGHTENED" -> 2
                            "MAC", "MACHINA" -> 3
                            else -> 0
                        },
                        level = portalStatus.level,
                        health = portalStatus.health,
                        title = portalStatus.title
                    )
                    val added = viewModel.togglePortalBookmark(portal)
                    viewModel.pluginHost.showToast(if (added) "Bookmarked ${portalStatus.title}" else "Removed bookmark")
                }

                if (!isExpanded) {
                    // Collapsed Peek Bar
                    PortalPeekBar(
                        portalStatus = portalStatus,
                        isBookmarked = isBookmarked,
                        onBookmarkToggle = onBookmarkToggle,
                        onExpand = { viewModel.expandPortalDetails() },
                        onClose = { viewModel.deselectPortal() }
                    )
                } else {
                    // Full Expanded Details View
                    PortalExpandedDetails(
                        portalStatus = portalStatus,
                        isBookmarked = isBookmarked,
                        onBookmarkToggle = onBookmarkToggle,
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
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
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

        Spacer(modifier = Modifier.width(8.dp))

        // Bookmark star button
        IconButton(
            onClick = onBookmarkToggle,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Bookmark portal",
                tint = if (isBookmarked) AmberStar else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

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
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    viewModel: MainViewModel,
    onCollapse: () -> Unit,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    // Dialog state for showing deployed agent name on click
    var selectedModForDetail by remember { mutableStateOf<PortalMod?>(null) }
    var selectedResonatorForDetail by remember { mutableStateOf<Pair<String, Resonator>?>(null) } // (direction, resonator)
    var isFullscreenImageOpen by remember { mutableStateOf(false) }

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

            // Bookmark star action
            IconButton(
                onClick = onBookmarkToggle,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Bookmark portal",
                    tint = if (isBookmarked) AmberStar else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

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
                    // Portal Photo (Clickable to view full size)
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = !portalStatus.image.isNullOrBlank()) {
                                isFullscreenImageOpen = true
                            }
                    ) {
                        PortalAsyncImage(
                            url = portalStatus.image,
                            teamColor = portalStatus.teamColor,
                            httpClient = viewModel.manager.httpClient,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (!portalStatus.image.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(5.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Expand photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

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
            PortalModsHeader()

            PortalModsRow(
                mods = portalStatus.mods,
                onModClick = { selectedModForDetail = it }
            )
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

        // Linked Portals Section (M3E Expressive Linked Portals Card)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "LINKED PORTALS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (portalStatus.linkedPortals.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${portalStatus.linkedPortals.size}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (portalStatus.linkedPortals.isNotEmpty()) {
                    val outCount = portalStatus.linkedPortals.count { it.isOutgoing }
                    val inCount = portalStatus.linkedPortals.size - outCount
                    Text(
                        text = "$outCount Out / $inCount In",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            if (portalStatus.linkedPortals.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (portalStatus.linksIn == 0 && portalStatus.linksOut == 0) "No active links connected" else "Links detected; zoom or pan to load linked portals",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    portalStatus.linkedPortals.forEach { linked ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectLinkedPortal(linked)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Direction Icon & Tag
                                Surface(
                                    color = if (linked.isOutgoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (linked.isOutgoing) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = if (linked.isOutgoing) "Outgoing" else "Incoming",
                                            tint = if (linked.isOutgoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (linked.isOutgoing) "Out" else "In",
                                            color = if (linked.isOutgoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Level Tag
                                Surface(
                                    color = linked.teamColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "L${linked.level}",
                                        color = linked.teamColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // Title and distance
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = linked.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = linked.formattedDistance,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Jump to linked portal",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
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

    // Interactive Mod Detail Inspection Dialog锛堟棤鐘舵€佺粍浠讹紝鍙湪 Compose Preview 涓崟鐙瑙堬級
    selectedModForDetail?.let { mod ->
        PortalModDetailDialog(
            mod = mod,
            onDismiss = { selectedModForDetail = null }
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
                    text = "$dirFullName ($direction) 鈥?Level ${res.level}",
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

    // Fullscreen Portal Image Viewer Dialog
    val imageUrl = portalStatus.image
    if (isFullscreenImageOpen && !imageUrl.isNullOrBlank()) {
        PortalFullscreenImageDialog(
            imageUrl = imageUrl,
            title = portalStatus.title,
            teamColor = portalStatus.teamColor,
            httpClient = viewModel.manager.httpClient,
            onDismiss = { isFullscreenImageOpen = false }
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

/**
 * Fullscreen Interactive Portal Image Viewer
 * Features pinch-to-zoom, double-tap zoom toggle, panning, and header controls.
 */
@Composable
fun PortalFullscreenImageDialog(
    imageUrl: String,
    title: String,
    teamColor: Color,
    httpClient: io.ktor.client.HttpClient,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageBitmap by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(imageUrl) { mutableStateOf(true) }

    LaunchedEffect(imageUrl) {
        isLoading = true
        try {
            val bytes = withContext(Dispatchers.Default) {
                val resp = httpClient.get(imageUrl)
                resp.readRawBytes()
            }
            imageBitmap = PlatformUtils.decodeImageBitmap(bytes)
        } catch (_: Exception) {
            imageBitmap = null
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF5000000))
        ) {
            // Interactive Zoomable / Pannable Image Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            },
                            onTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    onDismiss()
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val bmp = imageBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                } else if (isLoading) {
                    ExpressiveCircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.5.dp
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Failed to load photo", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            // Top Floating Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(teamColor)
                        )
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Floating Hint & Browser Open Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Double-tap to zoom 鈥?Drag to pan",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                FilledTonalButton(
                    onClick = {
                        PlatformUtils.openUrl(imageUrl)
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Browser", fontSize = 12.sp)
                }
            }
        }
    }
}
