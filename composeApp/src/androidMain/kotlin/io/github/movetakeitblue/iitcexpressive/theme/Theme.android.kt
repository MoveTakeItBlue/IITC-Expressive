package io.github.movetakeitblue.iitcexpressive.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun getAppColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // When status bar background is dark (darkTheme == true):
                // isAppearanceLightStatusBars = false -> render LIGHT (white) text and icons
                // When status bar background is light (darkTheme == false):
                // isAppearanceLightStatusBars = true -> render DARK (black) text and icons
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) IITCDarkColorScheme else IITCLightColorScheme
    }
}
