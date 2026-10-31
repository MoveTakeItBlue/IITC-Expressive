package io.github.movetakeitblue.iitcexpressive.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getAppColorScheme(
    darkTheme: Boolean,
    isDynamicColor: Boolean,
    presetColor: String
): ColorScheme {
    return buildPresetColorScheme(darkTheme, presetColor)
}
