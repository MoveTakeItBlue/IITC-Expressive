package io.github.movetakeitblue.iitcexpressive.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Fallback M3 Expressive Dark ColorScheme
val IITCDarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = Color(0xFF003730),
    primaryContainer = AccentContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    secondary = SecondaryTeal,
    onSecondary = Color(0xFF1B3530),
    secondaryContainer = Color(0xFF324B46),
    onSecondaryContainer = Color(0xFFCCE8E2),
    tertiary = TertiaryAmber,
    onTertiary = Color(0xFF3F3517),
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

// Fallback M3 Expressive Light ColorScheme
val IITCLightColorScheme = lightColorScheme(
    primary = Color(0xFF006A5F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF95F4E3),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF05201C),
    tertiary = Color(0xFF6E5D2E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF9E0A6),
    onTertiaryContainer = Color(0xFF261A00),
    surface = Color(0xFFF4FAF8),
    onSurface = Color(0xFF161D1B),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEEF5F3),
    surfaceContainer = Color(0xFFE8EFED),
    surfaceContainerHigh = Color(0xFFE2EAE7),
    surfaceContainerHighest = Color(0xFFDCE4E1),
    background = Color(0xFFF4FAF8),
    onBackground = Color(0xFF161D1B),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBEC9C5),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

// Material 3 Expressive Shapes (rounded containers, pills, fluid elements)
val IITCExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Returns the platform-appropriate ColorScheme.
 * On Android 12+, dynamically extracts wallpaper/system colors (Material You).
 */
@Composable
expect fun getAppColorScheme(darkTheme: Boolean = true): ColorScheme

@Composable
fun IITCTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = getAppColorScheme(darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = IITCExpressiveShapes,
        content = content
    )
}
