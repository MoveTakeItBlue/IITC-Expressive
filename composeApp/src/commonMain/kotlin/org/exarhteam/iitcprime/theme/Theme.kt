package org.exarhteam.iitcprime.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Material 3 Expressive ColorScheme
val IITCDarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = Color(0xFF003731),
    primaryContainer = AccentContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    secondary = SecondaryTeal,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceContainerHighDark,
    onSecondaryContainer = OnSurfaceWhite,
    tertiary = TertiaryAmber,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = TertiaryContainerAmber,
    onTertiaryContainer = OnTertiaryContainerAmber,
    surface = SurfaceDark,
    onSurface = OnSurfaceWhite,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = OnSurfaceVariantMuted,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    background = SurfaceDark,
    onBackground = OnSurfaceWhite,
    outline = OutlineVariantMuted,
    outlineVariant = OutlineSubtle,
    error = StateError,
    onError = Color.White
)

// Material 3 Expressive Shapes (rounded containers, pills, fluid elements)
val IITCExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun IITCTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IITCDarkColorScheme,
        shapes = IITCExpressiveShapes,
        content = content
    )
}
