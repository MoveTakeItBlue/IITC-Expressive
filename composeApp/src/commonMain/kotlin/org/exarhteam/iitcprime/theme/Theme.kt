package org.exarhteam.iitcprime.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IITCDarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = Color.Black,
    primaryContainer = AccentContainerTeal,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.Black,
    surface = SurfaceDark,
    onSurface = OnSurfaceWhite,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = OnSurfaceVariantMuted,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    background = SurfaceDark,
    onBackground = OnSurfaceWhite,
    error = StateError,
    onError = Color.White
)

@Composable
fun IITCTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IITCDarkColorScheme,
        content = content
    )
}
