package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.MapStatus
import org.exarhteam.iitcprime.model.PortalStatus
import org.exarhteam.iitcprime.theme.*
import org.exarhteam.iitcprime.viewmodel.MainViewModel

/**
 * Material 3 Expressive MapStateBar
 * Docked status bar displaying portal information (left) and map sync state (right).
 * Tapping the portal status opens/toggles the portal details pane ('info').
 * When in a non-map pane (like 'info'), shows an expressive back navigation chip to return to the map.
 * Tapping the map status toggles the panel (e.g. layers).
 */
@Composable
fun MapStateBar(
    portalStatus: PortalStatus,
    mapStatus: MapStatus,
    isWebviewLoaded: Boolean,
    isIitcLoaded: Boolean,
    currentPane: String = "map",
    onPortalTap: () -> Unit,
    onMapTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Portal Status / Back clickable section
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                        onClick = onPortalTap
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (currentPane != "map") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = L("panel.back_to_map"),
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = L("panel.back_to_map"),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (currentPane == "info" && portalStatus.hasSelected) {
                            Text(
                                text = "• ${portalStatus.title}",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    PortalStatusView(
                        portalStatus = portalStatus,
                        isWebviewLoaded = isWebviewLoaded,
                        isIitcLoaded = isIitcLoaded
                    )
                }
            }

            // Map Status clickable section
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                        onClick = onMapTap
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                MapStatusView(
                    mapStatus = mapStatus
                )
            }
        }
    }
}

@Composable
fun MapStateBar(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val portalStatus by viewModel.portalStatus.collectAsState()
    val mapStatus by viewModel.mapStatus.collectAsState()
    val isWebviewLoaded by viewModel.isWebviewLoaded.collectAsState()
    val isIitcLoaded by viewModel.isIitcLoaded.collectAsState()
    val currentPane by viewModel.currentPane.collectAsState()
    val panelPosition by viewModel.panelPosition.collectAsState()
    val isPanelOpen = panelPosition == org.exarhteam.iitcprime.viewmodel.PanelPosition.MIDDLE || panelPosition == org.exarhteam.iitcprime.viewmodel.PanelPosition.TOP

    MapStateBar(
        portalStatus = portalStatus,
        mapStatus = mapStatus,
        isWebviewLoaded = isWebviewLoaded,
        isIitcLoaded = isIitcLoaded,
        currentPane = currentPane,
        onPortalTap = {
            if (isPanelOpen) {
                viewModel.closePanel()
            } else if (currentPane != "map") {
                viewModel.switchToPane("map")
            } else if (portalStatus.hasSelected) {
                viewModel.switchToPane("info")
            } else {
                viewModel.togglePanel()
            }
        },
        onMapTap = {
            viewModel.togglePanel()
        },
        modifier = modifier
    )
}
