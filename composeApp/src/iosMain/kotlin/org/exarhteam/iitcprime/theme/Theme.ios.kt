package org.exarhteam.iitcprime.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getAppColorScheme(darkTheme: Boolean): ColorScheme {
    return if (darkTheme) IITCDarkColorScheme else IITCLightColorScheme
}
