package io.github.movetakeitblue.iitcexpressive.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive (M3E) User Location Indicator
 *
 * Renders the user's personal location using the official M3E Arrow shape
 * from the Material 3 Expressive library (`MaterialShapes.Arrow`), rotated so that
 * the arrow points towards the device's heading / orientation.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3EUserLocationIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    orientation: Double? = null,
    mapBearing: Double = 0.0
) {
    // Calculate effective screen rotation: device azimuth minus map rotation
    val targetHeading = remember(orientation, mapBearing) {
        if (orientation != null) {
            ((orientation - mapBearing + 360.0) % 360.0).toFloat()
        } else {
            0f
        }
    }

    // Smooth shortest-path rotation animation across 0°/360° boundary
    var cumulativeAngle by remember { mutableStateOf(targetHeading) }
    LaunchedEffect(targetHeading) {
        val currentMod = cumulativeAngle % 360f
        val normalizedCurrent = if (currentMod < 0) currentMod + 360f else currentMod
        var diff = (targetHeading - normalizedCurrent) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        cumulativeAngle += diff
    }

    val animatedRotation by animateFloatAsState(
        targetValue = cumulativeAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "userLocationHeading"
    )

    val arrowShape = MaterialShapes.Arrow.toShape()
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // M3E Arrow indicator oriented towards device heading
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    rotationZ = animatedRotation
                }
                .shadow(elevation = 4.dp, shape = arrowShape, clip = false)
                .background(color = primaryColor, shape = arrowShape)
                .border(width = 2.dp, color = Color.White, shape = arrowShape)
        )
    }
}
