package io.github.movetakeitblue.iitcexpressive.ui.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive (M3E) Circular Progress Indicator
 * Features smooth rounded stroke caps and expressive container track color.
 */
@Composable
fun ExpressiveCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeWidth: Dp = 3.dp,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeWidth = strokeWidth,
        strokeCap = strokeCap
    )
}

/**
 * Material 3 Expressive (M3E) Linear Progress Indicator (determinate)
 * Features rounded pill caps and expressive tonal track.
 */
@Composable
fun ExpressiveLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier.clip(RoundedCornerShape(percent = 50)),
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}

/**
 * Material 3 Expressive (M3E) Linear Progress Indicator (indeterminate)
 */
@Composable
fun ExpressiveLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    LinearProgressIndicator(
        modifier = modifier.clip(RoundedCornerShape(percent = 50)),
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}
