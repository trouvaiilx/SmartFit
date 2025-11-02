// FILE: app/src/main/java/com/smartfit/ui/theme/Theme.kt

package com.smartfit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.smartfit.data.local.datastore.PreferencesManager

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE)
)

@Composable
fun SmartFitTheme(
    themeMode: String = PreferencesManager.THEME_SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        PreferencesManager.THEME_LIGHT -> false
        PreferencesManager.THEME_DARK -> true
        PreferencesManager.THEME_SYSTEM -> systemInDarkTheme
        else -> systemInDarkTheme
    }

    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Determine the background color based on the current app theme
            val backgroundColor = if (darkTheme) {
                DarkColorScheme.background.toArgb() // Use the color from theme
            } else {
                LightColorScheme.background.toArgb() // Use the color from theme
            }

            // Apply the theme background color to BOTH the status and navigation bars
            window.statusBarColor = backgroundColor
            window.navigationBarColor = backgroundColor

            // This handles the color of the ICONS in the status and navigation bars
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}