package io.github.movetakeitblue.iitcexpressive.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Fallback M3 Expressive Dark ColorScheme (Teal)
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

// Fallback M3 Expressive Light ColorScheme (Teal)
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
 * Builds a preset ColorScheme according to user selection when dynamic color is disabled.
 */
fun buildPresetColorScheme(darkTheme: Boolean, preset: String): ColorScheme {
    return when (preset.lowercase()) {
        "blue" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF8BCEFF),
                onPrimary = Color(0xFF003355),
                primaryContainer = Color(0xFF004B76),
                onPrimaryContainer = Color(0xFFCCE5FF),
                secondary = Color(0xFFB9C8DA),
                onSecondary = Color(0xFF243240),
                secondaryContainer = Color(0xFF3A4857),
                onSecondaryContainer = Color(0xFFD5E4F6),
                tertiary = Color(0xFFD4BEE6),
                onTertiary = Color(0xFF392A4A),
                tertiaryContainer = Color(0xFF504061),
                onTertiaryContainer = Color(0xFFF0DBFF),
                surface = SurfaceDark,
                onSurface = OnSurfaceWhite,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark,
                outline = OutlineVariantMuted,
                outlineVariant = OutlineSubtle
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF006399),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFCCE5FF),
                onPrimaryContainer = Color(0xFF001D32),
                secondary = Color(0xFF51606F),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD5E4F6),
                onSecondaryContainer = Color(0xFF0E1D2A),
                tertiary = Color(0xFF68587A),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFF0DBFF),
                onTertiaryContainer = Color(0xFF231533),
                surface = Color(0xFFF7F9FF),
                onSurface = Color(0xFF181C20),
                surfaceContainer = Color(0xFFECEFF5),
                surfaceContainerHigh = Color(0xFFE6E9EF),
                surfaceContainerHighest = Color(0xFFE0E3E9)
            )
        }
        "green" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF5CDD8B),
                onPrimary = Color(0xFF00391B),
                primaryContainer = Color(0xFF00522B),
                onPrimaryContainer = Color(0xFF7BFAAA),
                secondary = Color(0xFFB5CCBA),
                onSecondary = Color(0xFF213528),
                secondaryContainer = Color(0xFF374B3E),
                onSecondaryContainer = Color(0xFFD1E8D5),
                tertiary = Color(0xFFA3CDDB),
                onTertiary = Color(0xFF033540),
                tertiaryContainer = Color(0xFF224C58),
                onTertiaryContainer = Color(0xFFBEEAF7),
                surface = SurfaceDark,
                onSurface = OnSurfaceWhite,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark,
                outline = OutlineVariantMuted,
                outlineVariant = OutlineSubtle
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF006D3B),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF7BFAAA),
                onPrimaryContainer = Color(0xFF00210E),
                secondary = Color(0xFF4F6354),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD1E8D5),
                onSecondaryContainer = Color(0xFF0C1F14),
                tertiary = Color(0xFF3B6471),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFBEEAF7),
                onTertiaryContainer = Color(0xFF001F26),
                surface = Color(0xFFF6FBF3),
                onSurface = Color(0xFF181D19),
                surfaceContainer = Color(0xFFEBF1E9),
                surfaceContainerHigh = Color(0xFFE5EBE3),
                surfaceContainerHighest = Color(0xFFDFE6DD)
            )
        }
        "red" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFB4AB),
                onPrimary = Color(0xFF690005),
                primaryContainer = Color(0xFF93000A),
                onPrimaryContainer = Color(0xFFFFDAD6),
                secondary = Color(0xFFE7BDB8),
                onSecondary = Color(0xFF442926),
                secondaryContainer = Color(0xFF5D3F3C),
                onSecondaryContainer = Color(0xFFFFDAD6),
                tertiary = Color(0xFFDFC28C),
                onTertiary = Color(0xFF3F2D04),
                tertiaryContainer = Color(0xFF574319),
                onTertiaryContainer = Color(0xFFFCDC9C),
                surface = SurfaceDark,
                onSurface = OnSurfaceWhite,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark,
                outline = OutlineVariantMuted,
                outlineVariant = OutlineSubtle
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFBA1A1A),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFDAD6),
                onPrimaryContainer = Color(0xFF410002),
                secondary = Color(0xFF775652),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFDAD6),
                onSecondaryContainer = Color(0xFF2C1513),
                tertiary = Color(0xFF715B2E),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFCDC9C),
                onTertiaryContainer = Color(0xFF261900),
                surface = Color(0xFFFFF8F7),
                onSurface = Color(0xFF221919),
                surfaceContainer = Color(0xFFF6ECEB),
                surfaceContainerHigh = Color(0xFFF0E6E5),
                surfaceContainerHighest = Color(0xFFEAE0DF)
            )
        }
        "purple" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD6BAFF),
                onPrimary = Color(0xFF421775),
                primaryContainer = Color(0xFF59338C),
                onPrimaryContainer = Color(0xFFECDCFF),
                secondary = Color(0xFFCCC2DC),
                onSecondary = Color(0xFF332D41),
                secondaryContainer = Color(0xFF4A4458),
                onSecondaryContainer = Color(0xFFE8DEF8),
                tertiary = Color(0xFFEFB8C8),
                onTertiary = Color(0xFF492532),
                tertiaryContainer = Color(0xFF633B48),
                onTertiaryContainer = Color(0xFFFFD8E4),
                surface = SurfaceDark,
                onSurface = OnSurfaceWhite,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark,
                outline = OutlineVariantMuted,
                outlineVariant = OutlineSubtle
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF704EA2),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFECDCFF),
                onPrimaryContainer = Color(0xFF280056),
                secondary = Color(0xFF625B71),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE8DEF8),
                onSecondaryContainer = Color(0xFF1E192B),
                tertiary = Color(0xFF7D5260),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFD8E4),
                onTertiaryContainer = Color(0xFF31111D),
                surface = Color(0xFFFFF7FC),
                onSurface = Color(0xFF1E1A20),
                surfaceContainer = Color(0xFFF2ECF4),
                surfaceContainerHigh = Color(0xFFECE6EE),
                surfaceContainerHighest = Color(0xFFE6E0E8)
            )
        }
        "amber" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFB95B),
                onPrimary = Color(0xFF472A00),
                primaryContainer = Color(0xFF653D00),
                onPrimaryContainer = Color(0xFFFFDDB6),
                secondary = Color(0xFFDEC3A2),
                onSecondary = Color(0xFF3E2D16),
                secondaryContainer = Color(0xFF56432B),
                onSecondaryContainer = Color(0xFFFBDEBD),
                tertiary = Color(0xFFBCCB9A),
                onTertiary = Color(0xFF273410),
                tertiaryContainer = Color(0xFF3D4B24),
                onTertiaryContainer = Color(0xFFD8E7B4),
                surface = SurfaceDark,
                onSurface = OnSurfaceWhite,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark,
                outline = OutlineVariantMuted,
                outlineVariant = OutlineSubtle
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF865300),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFDDB6),
                onPrimaryContainer = Color(0xFF2B1700),
                secondary = Color(0xFF6F5B40),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFBDEBD),
                onSecondaryContainer = Color(0xFF271905),
                tertiary = Color(0xFF54643B),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFD8E7B4),
                onTertiaryContainer = Color(0xFF131F01),
                surface = Color(0xFFFFF8F4),
                onSurface = Color(0xFF211A13),
                surfaceContainer = Color(0xFFF5EEE6),
                surfaceContainerHigh = Color(0xFFEFE8E0),
                surfaceContainerHighest = Color(0xFFE9E2DB)
            )
        }
        "slate" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFC3C7CF),
                onPrimary = Color(0xFF2D3137),
                primaryContainer = Color(0xFF43474E),
                onPrimaryContainer = Color(0xFFDFE3EB),
                secondary = Color(0xFFC2C7CE),
                onSecondary = Color(0xFF2C3137),
                secondaryContainer = Color(0xFF43474D),
                onSecondaryContainer = Color(0xFFDEE3EA),
                tertiary = Color(0xFFCBC4CE),
                onTertiary = Color(0xFF322E37),
                tertiaryContainer = Color(0xFF49454E),
                onTertiaryContainer = Color(0xFFE8E0EB),
                surface = SurfaceDark,
                onSurface = OnSurfaceWhite,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark,
                outline = OutlineVariantMuted,
                outlineVariant = OutlineSubtle
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF5B5F66),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDFE3EB),
                onPrimaryContainer = Color(0xFF181C22),
                secondary = Color(0xFF5B6065),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFDEE3EA),
                onSecondaryContainer = Color(0xFF181C21),
                tertiary = Color(0xFF615D66),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFE8E0EB),
                onTertiaryContainer = Color(0xFF1E1A22),
                surface = Color(0xFFF9F9FC),
                onSurface = Color(0xFF1A1C1E),
                surfaceContainer = Color(0xFFEDEFEF),
                surfaceContainerHigh = Color(0xFFE7E9E9),
                surfaceContainerHighest = Color(0xFFE1E3E3)
            )
        }
        else -> { // Default "teal"
            if (darkTheme) IITCDarkColorScheme else IITCLightColorScheme
        }
    }
}

/**
 * Returns the platform-appropriate ColorScheme.
 * On Android 12+, dynamically extracts wallpaper/system colors (Material You) when isDynamicColor is true.
 */
@Composable
expect fun getAppColorScheme(
    darkTheme: Boolean = true,
    isDynamicColor: Boolean = true,
    presetColor: String = "teal"
): ColorScheme

@Composable
fun IITCTheme(
    themeMode: String = "system",
    isDynamicColor: Boolean = true,
    presetColor: String = "teal",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = getAppColorScheme(
        darkTheme = darkTheme,
        isDynamicColor = isDynamicColor,
        presetColor = presetColor
    )
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = IITCExpressiveShapes,
        content = content
    )
}
