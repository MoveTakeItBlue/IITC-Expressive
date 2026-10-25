package io.github.movetakeitblue.iitcexpressive.ui.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L

/**
 * Material 3 Expressive Speed-Dial FAB Menu
 * Floats on the map screen, expanding to reveal quick actions and second-level destinations.
 * Adheres strictly to M3E tonal containers and dynamic theming without harsh outline borders.
 */
@Composable
fun FabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onOpenLayers: () -> Unit,
    onOpenChat: () -> Unit,
    onLocate: () -> Unit,
    onDebug: () -> Unit,
    onReload: () -> Unit,
    bottomOffset: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabRotation"
    )

    val animatedBottom by animateDpAsState(
        targetValue = bottomOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabBottomOffset"
    )

    Box(modifier = modifier) {
        // Scrim overlay when expanded
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
                        onClick = onToggle
                    )
            )
        }

        // Action items stack & Main FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = animatedBottom),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FabMenuItem(
                        icon = Icons.Default.Settings,
                        label = L("nav.settings"),
                        onClick = onNavigateToSettings
                    )
                    FabMenuItem(
                        icon = Icons.Default.Extension,
                        label = L("nav.plugins"),
                        onClick = onNavigateToPlugins
                    )
                    FabMenuItem(
                        icon = Icons.Default.Layers,
                        label = L("panel.layers"),
                        onClick = onOpenLayers
                    )
                    FabMenuItem(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        label = L("nav.chat"),
                        onClick = onOpenChat
                    )
                    FabMenuItem(
                        icon = Icons.Default.MyLocation,
                        label = L("panel.locate"),
                        onClick = onLocate
                    )
                    FabMenuItem(
                        icon = Icons.Default.Terminal,
                        label = L("nav.debug"),
                        onClick = onDebug
                    )
                    FabMenuItem(
                        icon = Icons.Default.Refresh,
                        label = L("nav.reload_iitc"),
                        onClick = onReload
                    )
                }
            }

            // Main FAB (Authentic M3E container matching Image 1)
            FloatingActionButton(
                onClick = onToggle,
                shape = CircleShape,
                containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = if (isExpanded) "Close menu" else "Open menu",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

/**
 * Compact M3E Pill Menu Item (Icon + Text inside unified Stadium Capsule)
 */
@Composable
private fun FabMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shadowElevation = 3.dp,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
