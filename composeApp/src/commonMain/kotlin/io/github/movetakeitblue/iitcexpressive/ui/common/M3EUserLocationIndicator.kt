package io.github.movetakeitblue.iitcexpressive.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive (M3E) User Location Indicator
 *
 * Sized consistently with portal icons (~24-26dp), featuring a continuously shape-changing
 * M3E shape (morphing smoothly between circle, clover, squircle, and diamond star),
 * an outer pulsing radar beacon, and an optional directional heading cone.
 */
@Composable
fun M3EUserLocationIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    orientation: Double? = null
) {
    val transition = rememberInfiniteTransition(label = "m3eLocationIndicator")

    // Continuous shape morph cycle across 4 M3E shapes (3.2 seconds per cycle)
    val morphProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morphProgress"
    )

    // Gentle continuous rotation (7.2 seconds per 360 revolution)
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Outer radar breathing / pulse effect
    val haloPulse by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloPulse"
    )

    val haloAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )

    val containerSize = size * 2f

    Box(
        modifier = modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(containerSize)) {
            val center = this.center
            val baseRadius = size.toPx() / 2f

            // 1. Heading cone / direction indicator (if orientation available)
            if (orientation != null) {
                val rad = (orientation - 90.0) * (PI / 180.0)
                val spread = 24.0 * (PI / 180.0)
                val coneRadius = baseRadius * 2.2f
                val conePath = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(
                        (center.x + coneRadius * cos(rad - spread)).toFloat(),
                        (center.y + coneRadius * sin(rad - spread)).toFloat()
                    )
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            center.x - coneRadius,
                            center.y - coneRadius,
                            center.x + coneRadius,
                            center.y + coneRadius
                        ),
                        startAngleDegrees = (orientation - 24.0).toFloat(),
                        sweepAngleDegrees = 48f,
                        forceMoveTo = false
                    )
                    close()
                }
                drawPath(
                    path = conePath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x7700E5FF),
                            Color(0x3300E5FF),
                            Color(0x0000E5FF)
                        ),
                        center = center,
                        radius = coneRadius
                    )
                )
            }

            // 2. Outer pulsing radar halo
            if (haloAlpha > 0.01f) {
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = haloAlpha),
                    radius = baseRadius * haloPulse,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }

            // 3. Morphing M3E Shape
            val shapePhase = morphProgress % 4f
            val shapeIdx = shapePhase.toInt()
            val frac = shapePhase - shapeIdx
            // Smooth hermite interpolation factor
            val t = frac * frac * (3f - 2f * frac)

            val rotRad = rotationAngle * (PI / 180.0)
            val numSteps = 72
            val morphPath = Path()

            for (i in 0 until numSteps) {
                val theta = (2.0 * PI * i) / numSteps
                // Define 4 M3E shapes as radial functions r(theta):
                // Shape 0: Circle
                val r0 = 1.0f
                // Shape 1: 4-Clover / Petal flower
                val r1 = (0.78 + 0.22 * cos(4.0 * theta)).toFloat()
                // Shape 2: Squircle (Soft superellipse rounded square)
                val r2 = (0.90 - 0.10 * cos(4.0 * theta)).toFloat()
                // Shape 3: Diamond Star
                val r3 = (0.74 + 0.26 * cos(4.0 * theta + PI)).toFloat()

                val fromR = when (shapeIdx) {
                    0 -> r0
                    1 -> r1
                    2 -> r2
                    else -> r3
                }
                val toR = when ((shapeIdx + 1) % 4) {
                    0 -> r0
                    1 -> r1
                    2 -> r2
                    else -> r3
                }

                val currentR = (1f - t) * fromR + t * toR
                val px = (center.x + baseRadius * currentR * cos(theta + rotRad)).toFloat()
                val py = (center.y + baseRadius * currentR * sin(theta + rotRad)).toFloat()

                if (i == 0) {
                    morphPath.moveTo(px, py)
                } else {
                    morphPath.lineTo(px, py)
                }
            }
            morphPath.close()

            // Fill morphing shape with glowing M3 cyan-blue gradient
            drawPath(
                path = morphPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF2979FF)
                    ),
                    start = androidx.compose.ui.geometry.Offset(center.x - baseRadius, center.y - baseRadius),
                    end = androidx.compose.ui.geometry.Offset(center.x + baseRadius, center.y + baseRadius)
                ),
                style = Fill
            )

            // Crisp white boundary stroke
            drawPath(
                path = morphPath,
                color = Color.White,
                style = Stroke(width = 1.8.dp.toPx())
            )

            // 4. Central precise pinpoint dot
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFF0091EA),
                radius = 1.6.dp.toPx(),
                center = center
            )
        }
    }
}
