package org.exarhteam.iitcprime.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.theme.SurfaceDark
import org.exarhteam.iitcprime.theme.SurfaceContainer
import org.exarhteam.iitcprime.viewmodel.LocationState
import org.exarhteam.iitcprime.viewmodel.MainViewModel
import org.exarhteam.iitcprime.viewmodel.PanelPosition

@Composable
fun BottomSheetPanel(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelPosition by viewModel.panelPosition.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val currentPane by viewModel.currentPane.collectAsState()
    val panes by viewModel.panes.collectAsState()
    val hasClipboardLink by viewModel.hasClipboardLink.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val isIitcLoaded by viewModel.isIitcLoaded.collectAsState()

    val isMapPane = currentPane == "map"
    val panelTitle = if (isMapPane) {
        L("app.name.release")
    } else {
        panes.find { it.name == currentPane }?.label ?: currentPane
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val closedHeight = 108.dp
        val middleHeight = screenHeight * 0.5f
        val topHeight = (screenHeight - 56.dp).coerceAtLeast(closedHeight)

        val targetHeight = when (panelPosition) {
            PanelPosition.HIDDEN -> 0.dp
            PanelPosition.BOTTOM -> closedHeight
            PanelPosition.MIDDLE -> middleHeight
            PanelPosition.TOP -> topHeight
        }

        val animatedHeight by animateDpAsState(
            targetValue = targetHeight,
            animationSpec = tween(durationMillis = 280),
            label = "bottomSheetHeight"
        )

        val isPanelOpen = panelPosition == PanelPosition.MIDDLE || panelPosition == PanelPosition.TOP

        // Backdrop scrim when open
        AnimatedVisibility(
            visible = isPanelOpen,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.closePanel()
                    }
            )
        }

        if (panelPosition != PanelPosition.HIDDEN || animatedHeight > 0.dp) {
            var dragOffset by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SurfaceDark)
                    .draggable(
                        state = rememberDraggableState { delta ->
                            dragOffset += delta
                        },
                        orientation = Orientation.Vertical,
                        onDragStopped = {
                            if (dragOffset < -40f) {
                                // Dragged up
                                when (panelPosition) {
                                    PanelPosition.HIDDEN -> viewModel.setPanelPosition(PanelPosition.BOTTOM)
                                    PanelPosition.BOTTOM -> viewModel.setPanelPosition(PanelPosition.MIDDLE)
                                    PanelPosition.MIDDLE -> viewModel.setPanelPosition(PanelPosition.TOP)
                                    PanelPosition.TOP -> {}
                                }
                            } else if (dragOffset > 40f) {
                                // Dragged down
                                when (panelPosition) {
                                    PanelPosition.TOP -> viewModel.setPanelPosition(PanelPosition.MIDDLE)
                                    PanelPosition.MIDDLE -> viewModel.setPanelPosition(PanelPosition.BOTTOM)
                                    PanelPosition.BOTTOM -> viewModel.hidePanel()
                                    PanelPosition.HIDDEN -> {}
                                }
                            }
                            dragOffset = 0f
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }

                    // Control buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Access / Back button
                        val isQuickActive = isPanelOpen && activeTab == "quick"
                        ControlButton(
                            icon = if (isMapPane) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = L(if (isMapPane) "panel.menu" else "panel.back_to_map"),
                            active = isQuickActive,
                            onClick = {
                                if (isMapPane) {
                                    if (isPanelOpen && activeTab == "quick") {
                                        viewModel.closePanel()
                                    } else {
                                        viewModel.setActiveTab("quick")
                                    }
                                } else {
                                    viewModel.switchToPane("map")
                                }
                            }
                        )

                        // Title
                        Text(
                            text = panelTitle,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .clickable {
                                    viewModel.togglePanel()
                                },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Paste link button
                        if (hasClipboardLink) {
                            ControlButton(
                                icon = Icons.Default.ContentPaste,
                                contentDescription = L("panel.paste_link"),
                                onClick = {
                                    viewModel.handlePasteClipboard()
                                }
                            )
                        }

                        // Location button
                        if (isIitcLoaded) {
                            val locIcon = when (locationState) {
                                LocationState.ERROR -> Icons.Default.Warning
                                LocationState.FOLLOWING -> Icons.Default.MyLocation
                                else -> Icons.Default.NearMe
                            }
                            ControlButton(
                                icon = locIcon,
                                contentDescription = L("panel.my_location"),
                                loading = locationState == LocationState.LOCATING,
                                onClick = {
                                    viewModel.triggerUserLocate()
                                }
                            )
                        }

                        // Layers button
                        if (isIitcLoaded) {
                            val isLayersActive = isPanelOpen && activeTab == "layers"
                            ControlButton(
                                icon = Icons.Default.Layers,
                                contentDescription = L("panel.layers"),
                                active = isLayersActive,
                                onClick = {
                                    if (isPanelOpen && activeTab == "layers") {
                                        viewModel.closePanel()
                                    } else {
                                        viewModel.setActiveTab("layers")
                                    }
                                }
                            )
                        }
                    }

                    // Content list view when opened
                    if (isPanelOpen || animatedHeight > closedHeight) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(SurfaceContainer.copy(alpha = 0.5f))
                        ) {
                            ControlListView(
                                activeTab = activeTab,
                                viewModel = viewModel,
                                onNavigateToSettings = onNavigateToSettings,
                                onNavigateToPlugins = onNavigateToPlugins,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
