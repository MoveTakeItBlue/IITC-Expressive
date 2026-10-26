package io.github.movetakeitblue.iitcexpressive.ui.layers

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.model.MapLayer
import io.github.movetakeitblue.iitcexpressive.model.NativeBaseMap
import io.github.movetakeitblue.iitcexpressive.theme.*
import io.github.movetakeitblue.iitcexpressive.ui.common.SelectFieldDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel

/**
 * Material 3 Expressive Layers Bottom Sheet
 * Opened from the FAB menu.
 * Contains MapLibre Native Base Map selection (4 keyless styles: Dark, Light, Satellite, Color),
 * M3E dynamic Highlighters, Portal level filter, Player Activity Tracker, and Overlay switches.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayersSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val nativeBaseMap by viewModel.nativeBaseMap.collectAsState()
    val overlayLayers by viewModel.overlayLayers.collectAsState()
    val highlighters by viewModel.highlighters.collectAsState()
    val selectedHighlighter by viewModel.selectedHighlighter.collectAsState()
    val isPlayerTrackerEnabled by viewModel.isPlayerTrackerEnabled.collectAsState()
    val playerTrails by viewModel.playerTrails.collectAsState()

    var showHighlighterDialog by remember { mutableStateOf(false) }
    var showBaseLayerDialog by remember { mutableStateOf(false) }

    if (showHighlighterDialog) {
        val selectedIdx = highlighters.indexOf(selectedHighlighter).coerceAtLeast(0)
        SelectFieldDialog(
            title = L("layers.select_highlighter"),
            items = highlighters,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                if (idx in highlighters.indices) {
                    viewModel.selectHighlighter(highlighters[idx])
                }
            },
            onDismiss = { showHighlighterDialog = false }
        )
    }

    if (showBaseLayerDialog) {
        val baseMapOptions = NativeBaseMap.entries
        val selectedIdx = baseMapOptions.indexOf(nativeBaseMap).coerceAtLeast(0)
        SelectFieldDialog(
            title = L("layers.select_base_layer"),
            items = baseMapOptions.map { "${it.title} (${it.subtitle})" },
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                if (idx in baseMapOptions.indices) {
                    viewModel.selectNativeBaseMap(baseMapOptions[idx])
                }
            },
            onDismiss = { showBaseLayerDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
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
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = L("panel.layers"),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Base Map & Highlighter Selector Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LayersDropdownCard(
                            title = L("layers.base_layer"),
                            value = nativeBaseMap.title,
                            icon = Icons.Default.Map,
                            onClick = { showBaseLayerDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        LayersDropdownCard(
                            title = "Highlighter",
                            value = selectedHighlighter.ifBlank { "No Highlights" },
                            icon = Icons.Default.Highlight,
                            onClick = { showHighlighterDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Portal Level Fast-Filter (L0 to L8)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PORTAL LEVEL FILTERS",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        val levelColors = listOf(
                            Level0Color, Level1Color, Level2Color, Level3Color,
                            Level4Color, Level5Color, Level6Color, Level7Color, Level8Color
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (lvl in 0..8) {
                                val isLayerActive = overlayLayers.getOrNull(lvl)?.active ?: false
                                val lvlCol = levelColors.getOrElse(lvl) { MaterialTheme.colorScheme.primary }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isLayerActive) lvlCol.copy(alpha = 0.25f)
                                             else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                        .clickable {
                                            viewModel.togglePortalLevelOverlay(lvl)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L$lvl",
                                        color = if (isLayerActive) lvlCol else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontWeight = if (isLayerActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Title: Tactical Extensions & Overlays
                item {
                    Text(
                        text = "TACTICAL OVERLAYS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Player Activity Tracker Overlay Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Player Activity Tracker",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (isPlayerTrackerEnabled && playerTrails.isNotEmpty()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "${playerTrails.size} active",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Native GPU tactical trails & agent positions from COMM",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Switch(
                                checked = isPlayerTrackerEnabled,
                                onCheckedChange = { viewModel.setPlayerTrackerEnabled(it) }
                            )
                        }
                    }
                }

                // Overlay layers list
                val remainingLayers = overlayLayers.drop(9)
                items(remainingLayers, key = { it.layerId }) { layer ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleOverlayLayer(layer) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = layer.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = layer.active,
                                onCheckedChange = { viewModel.toggleOverlayLayer(layer) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayersDropdownCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
