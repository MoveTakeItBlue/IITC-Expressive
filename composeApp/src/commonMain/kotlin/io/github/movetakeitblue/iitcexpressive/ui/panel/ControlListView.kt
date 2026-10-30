package io.github.movetakeitblue.iitcexpressive.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ripple
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
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.model.MapLayer
import io.github.movetakeitblue.iitcexpressive.model.Pane
import io.github.movetakeitblue.iitcexpressive.theme.*
import io.github.movetakeitblue.iitcexpressive.ui.common.SelectFieldDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel

/**
 * Material 3 Expressive ControlListView
 */
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
    val portalStatus by viewModel.portalStatus.collectAsState()

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

    val displayPanes = remember(panes, portalStatus.hasSelected) {
        panes.filter { pane ->
            if (pane.name == "map") false
            else if (pane.name == "info") portalStatus.hasSelected
            else true
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (activeTab == "quick") {
            // Quick action buttons group
            item {
                ExpressiveActionButtonsGroup(
                    onSettings = onNavigateToSettings,
                    onPlugins = onNavigateToPlugins,
                    onDebug = { viewModel.toggleDebugMode() },
                    onReload = { viewModel.reloadWebView(clearCache = false) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(2.dp))
            }

            itemsIndexed(displayPanes, key = { _, pane -> pane.name }) { _, pane ->
                val isSelected = currentPane == pane.name

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = MaterialTheme.colorScheme.primary),
                            onClick = { viewModel.switchToPane(pane.name) }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading Icon with tonal pill background
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPaneIcon(pane.icon),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = pane.displayLabel,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CORE PLUGINS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            items(io.github.movetakeitblue.iitcexpressive.plugin.core.NativePluginRegistry.getAll(), key = { it.id }) { plugin ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = MaterialTheme.colorScheme.primary),
                            onClick = {
                                viewModel.closePanel()
                                viewModel.openPluginSheet(plugin.id)
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            imageVector = plugin.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plugin.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = plugin.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else if (activeTab == "layers") {
            // Select fields group: Highlighter & Base Layer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Highlighter Card
                    ExpressiveDropdownCard(
                        title = L("layers.highlighter"),
                        value = selectedHighlighter,
                        icon = Icons.Default.Highlight,
                        onClick = { showHighlighterDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    // Base Layer Card
                    val currentBaseName = baseLayers.find { it.layerId == selectedBaseLayer }?.name ?: L("select_field.select")
                    ExpressiveDropdownCard(
                        title = L("layers.base_layer"),
                        value = currentBaseName,
                        icon = Icons.Default.Map,
                        onClick = { showBaseLayerDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Portal Icons row (first 9 overlay layers: levels 0-8)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = L("map_status.portals"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = color),
                                        onClick = { viewModel.togglePortalLevelOverlay(level) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$level",
                                    color = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Remaining overlay layers (index 9 onwards)
            val remainingLayers = overlayLayers.drop(9)
            // First 4 remaining as 2-column cards
            val paired = remainingLayers.take(4)
            for (i in paired.indices step 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val layer1 = paired[i]
                        val layer2 = paired.getOrNull(i + 1)

                        ExpressiveLayerSwitchCard(
                            layer = layer1,
                            onToggle = { viewModel.toggleOverlayLayer(layer1) },
                            modifier = Modifier.weight(1f)
                        )
                        if (layer2 != null) {
                            ExpressiveLayerSwitchCard(
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

            // Single switches for the rest of overlay layers
            val singleLayers = remainingLayers.drop(4)
            itemsIndexed(singleLayers, key = { _, l -> l.layerId }) { _, layer ->
                ExpressiveLayerSwitchRow(
                    layer = layer,
                    onToggle = { viewModel.toggleOverlayLayer(layer) }
                )
            }
        }
    }
}

/**
 * Material 3 Expressive Action Buttons Card Grid
 */
@Composable
private fun ExpressiveActionButtonsGroup(
    onSettings: () -> Unit,
    onPlugins: () -> Unit,
    onDebug: () -> Unit,
    onReload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ExpressiveActionButtonItem(icon = Icons.Default.Settings, text = L("nav.settings"), onClick = onSettings)
        ExpressiveActionButtonItem(icon = Icons.Default.Extension, text = L("nav.plugins"), onClick = onPlugins)
        ExpressiveActionButtonItem(icon = Icons.Default.Terminal, text = L("nav.debug"), onClick = onDebug)
        ExpressiveActionButtonItem(icon = Icons.Default.Refresh, text = L("nav.reload_iitc"), onClick = onReload)
    }
}

@Composable
private fun ExpressiveActionButtonItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExpressiveDropdownCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = MaterialTheme.colorScheme.primary),
                    onClick = onClick
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExpressiveLayerSwitchCard(
    layer: MapLayer,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onToggle
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = layer.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = layer.active,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun ExpressiveLayerSwitchRow(
    layer: MapLayer,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onToggle
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = layer.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = layer.active,
            onCheckedChange = { onToggle() }
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
        "ms-bookmark" -> Icons.Default.Bookmark
        "ms-edit" -> Icons.Default.Edit
        "ms-key" -> Icons.Default.VpnKey
        "ms-extension" -> Icons.Default.Extension
        else -> Icons.Default.Extension
    }
}
