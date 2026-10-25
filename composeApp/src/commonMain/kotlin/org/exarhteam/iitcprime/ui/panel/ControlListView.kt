package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.MapLayer
import org.exarhteam.iitcprime.model.Pane
import org.exarhteam.iitcprime.theme.*
import org.exarhteam.iitcprime.ui.common.SelectFieldDialog
import org.exarhteam.iitcprime.viewmodel.MainViewModel

@Composable
fun ControlListView(
    activeTab: String,
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHighlighterDialog by remember { mutableStateOf(false) }
    var showBaseLayerDialog by remember { mutableStateOf(false) }

    val highlighters by viewModel.highlighters.collectAsState()
    val selectedHighlighter by viewModel.selectedHighlighter.collectAsState()
    val baseLayers by viewModel.baseLayers.collectAsState()
    val selectedBaseLayer by viewModel.selectedBaseLayer.collectAsState()
    val overlayLayers by viewModel.overlayLayers.collectAsState()
    val panes by viewModel.panes.collectAsState()
    val currentPane by viewModel.currentPane.collectAsState()

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
        val selectedIdx = baseLayers.indexOfFirst { it.layerId == selectedBaseLayer }.coerceAtLeast(0)
        SelectFieldDialog(
            title = L("layers.select_base_layer"),
            items = baseLayers.map { it.name },
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                if (idx in baseLayers.indices) {
                    viewModel.selectBaseLayer(baseLayers[idx])
                }
            },
            onDismiss = { showBaseLayerDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (activeTab == "quick") {
            // Quick action buttons group
            item {
                ActionButtonsGroup(
                    onSettings = onNavigateToSettings,
                    onPlugins = onNavigateToPlugins,
                    onDebug = { viewModel.toggleDebugMode() },
                    onReload = { viewModel.reloadWebView(clearCache = false) }
                )
            }

            // Panes list
            val filteredPanes = panes.filterIndexed { index, _ -> index < 3 || index >= 5 }
            itemsIndexed(filteredPanes) { index, pane ->
                val isSelected = currentPane == pane.name
                val isFirst = index == 0
                val isLast = index == filteredPanes.size - 1

                val shape = when {
                    isFirst && isLast -> RoundedCornerShape(12.dp)
                    isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    isLast -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(4.dp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(if (isSelected) SurfaceContainerHighDark else SurfaceContainerDark)
                        .clickable { viewModel.switchToPane(pane.name) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getPaneIcon(pane.icon),
                        contentDescription = null,
                        tint = if (isSelected) AccentTeal else OnSurfaceWhite,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = pane.displayLabel,
                        color = if (isSelected) AccentTeal else OnSurfaceWhite,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        } else if (activeTab == "layers") {
            // Select fields group: Highlighter & Base Layer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Highlighter select field
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = L("layers.highlighter"),
                            color = OnSurfaceVariantMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerDark)
                                .clickable { showHighlighterDialog = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedHighlighter,
                                color = OnSurfaceWhite,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OnSurfaceVariantMuted)
                        }
                    }

                    // Base layer select field
                    val currentBaseName = baseLayers.find { it.layerId == selectedBaseLayer }?.name ?: L("select_field.select")
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = L("layers.base_layer"),
                            color = OnSurfaceVariantMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerDark)
                                .clickable { showBaseLayerDialog = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentBaseName,
                                color = OnSurfaceWhite,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OnSurfaceVariantMuted)
                        }
                    }
                }
            }

            // Portal Icons row (first 9 overlay layers: levels 0-8)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerDark)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val portalColors = listOf(
                        Level0Color, Level1Color, Level2Color, Level3Color,
                        Level4Color, Level5Color, Level6Color, Level7Color, Level8Color
                    )

                    for (level in 0..8) {
                        val layer = overlayLayers.getOrNull(level)
                        val isActive = layer?.active ?: true
                        val color = portalColors[level]

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isActive) color else Color.Transparent)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isActive) Color.White else color.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.togglePortalLevelOverlay(level) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$level",
                                color = if (isActive) Color.Black else color,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Remaining overlay layers (index 9 onwards)
            val remainingLayers = overlayLayers.drop(9)
            // Show first 4 remaining as pairs
            val paired = remainingLayers.take(4)
            for (i in paired.indices step 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val layer1 = paired[i]
                        val layer2 = paired.getOrNull(i + 1)

                        LayerSwitchCard(
                            layer = layer1,
                            onToggle = { viewModel.toggleOverlayLayer(layer1) },
                            modifier = Modifier.weight(1f)
                        )
                        if (layer2 != null) {
                            LayerSwitchCard(
                                layer = layer2,
                                onToggle = { viewModel.toggleOverlayLayer(layer2) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Single switches for remainder
            val singleLayers = remainingLayers.drop(4)
            itemsIndexed(singleLayers) { _, layer ->
                LayerSwitchRow(
                    layer = layer,
                    onToggle = { viewModel.toggleOverlayLayer(layer) }
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsGroup(
    onSettings: () -> Unit,
    onPlugins: () -> Unit,
    onDebug: () -> Unit,
    onReload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerDark)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButtonItem(icon = Icons.Default.Settings, text = L("nav.settings"), onClick = onSettings)
        ActionButtonItem(icon = Icons.Default.Extension, text = L("nav.plugins"), onClick = onPlugins)
        ActionButtonItem(icon = Icons.Default.Terminal, text = L("nav.debug"), onClick = onDebug)
        ActionButtonItem(icon = Icons.Default.Refresh, text = L("nav.reload_iitc"), onClick = onReload)
    }
}

@Composable
private fun ActionButtonItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = text, tint = AccentTeal, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, color = OnSurfaceWhite, fontSize = 11.sp)
    }
}

@Composable
private fun LayerSwitchCard(
    layer: MapLayer,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerDark)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = layer.name,
            color = OnSurfaceWhite,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = layer.active,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentTeal,
                uncheckedThumbColor = OnSurfaceVariantMuted,
                uncheckedTrackColor = SurfaceContainerHighDark
            )
        )
    }
}

@Composable
private fun LayerSwitchRow(
    layer: MapLayer,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerDark)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = layer.name,
            color = OnSurfaceWhite,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = layer.active,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentTeal,
                uncheckedThumbColor = OnSurfaceVariantMuted,
                uncheckedTrackColor = SurfaceContainerHighDark
            )
        )
    }
}

private fun getPaneIcon(iconName: String): ImageVector {
    return when (iconName) {
        "ms-campaign" -> Icons.Default.Campaign
        "ms-group" -> Icons.Default.Group
        "ms-notifications" -> Icons.Default.Notifications
        "ms-info" -> Icons.Default.Info
        "ms-map" -> Icons.Default.Map
        else -> Icons.Default.Map
    }
}
