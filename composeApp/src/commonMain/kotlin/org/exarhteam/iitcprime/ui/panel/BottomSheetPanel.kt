package org.exarhteam.iitcprime.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.theme.*
import org.exarhteam.iitcprime.viewmodel.LocationState
import org.exarhteam.iitcprime.viewmodel.MainViewModel
import org.exarhteam.iitcprime.viewmodel.PanelPosition

/**
 * Material 3 Expressive BottomSheetPanel
 * Provides draggable multi-step bottom sheet (HIDDEN, BOTTOM, MIDDLE, TOP)
 * with expressive motion, pill drag handle, and pane management.
 */
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
        val found = panes.find { it.name == currentPane }
        if (found != null) {
            L(found.label)
        } else {
            currentPane.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val closedHeight = 110.dp
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
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "bottomSheetHeight"
        )

        val isPanelOpen = panelPosition == PanelPosition.MIDDLE || panelPosition == PanelPosition.TOP

        // Backdrop scrim when open
        AnimatedVisibility(
            visible = isPanelOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
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
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(SurfaceDark)
                    .border(
                        width = 1.dp,
                        color = OutlineSubtle,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
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
                    // M3 Expressive Drag Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(OnSurfaceVariantMuted.copy(alpha = 0.45f))
                        )
                    }

                    // Control buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Menu / Back button
                        val isQuickActive = isPanelOpen && activeTab == "quick"
                        ExpressiveIconButton(
                            icon = if (isMapPane) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = L(if (isMapPane) "panel.menu" else "panel.back_to_map"),
                            active = isQuickActive,
                            onClick = {
                                if (isMapPane) {
                                    if (isPanelOpen && activeTab == "quick") {
                                        viewModel.closePanel()
                                    } else {
                                        viewModel.setActiveTab("quick")
                                        if (!isPanelOpen) viewModel.openPanel()
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.togglePanel()
                                },
                            color = OnSurfaceWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Paste link button
                        if (hasClipboardLink) {
                            ExpressiveIconButton(
                                icon = Icons.Default.ContentPaste,
                                contentDescription = L("panel.paste_link"),
                                onClick = { viewModel.handlePasteClipboard() }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Location button
                        if (isIitcLoaded) {
                            val locIcon = when (locationState) {
                                LocationState.ERROR -> Icons.Default.Warning
                                LocationState.FOLLOWING -> Icons.Default.MyLocation
                                else -> Icons.Default.NearMe
                            }
                            val isLocActive = locationState == LocationState.FOLLOWING
                            ExpressiveIconButton(
                                icon = locIcon,
                                contentDescription = L("panel.my_location"),
                                active = isLocActive,
                                loading = locationState == LocationState.LOCATING,
                                onClick = { viewModel.triggerUserLocate() }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Layers button
                        if (isIitcLoaded) {
                            val isLayersActive = isPanelOpen && activeTab == "layers"
                            ExpressiveIconButton(
                                icon = Icons.Default.Layers,
                                contentDescription = L("panel.layers"),
                                active = isLayersActive,
                                onClick = {
                                    if (isPanelOpen && activeTab == "layers") {
                                        viewModel.closePanel()
                                    } else {
                                        viewModel.setActiveTab("layers")
                                        if (!isPanelOpen) viewModel.openPanel()
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
                                .padding(bottom = 54.dp) // Avoid overlapping bottom MapStateBar
                                .background(SurfaceContainerDark.copy(alpha = 0.5f))
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

/**
 * Material 3 Expressive Icon Button
 */
@Composable
fun ExpressiveIconButton(
    icon: ImageVector,
    contentDescription: String?,
    active: Boolean = false,
    loading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        active -> AccentTeal
        else -> SurfaceContainerHighDark
    }
    val tint = when {
        active -> Color.Black
        else -> OnSurfaceWhite
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AccentTeal),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = AccentTeal,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
