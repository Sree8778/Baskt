package com.example.supportagent.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleBlue,
    secondary = ApplePurple,
    tertiary = ApplePink,
    background = OledMidnight,
    surface = GlassSurface,
    surfaceVariant = GlassSurfaceElevated,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextWhite,
    onSurface = TextWhite
)

private val AppleLightColorScheme = lightColorScheme(
    primary = AppleBlue,
    secondary = ApplePurple,
    tertiary = ApplePink,
    background = AppleLightBackground,
    surface = AppleLightSurface,
    surfaceVariant = AppleLightSurfaceElevated,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AppleLightTextPrimary,
    onSurface = AppleLightTextPrimary
)

@Composable
fun SupportAgentTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppleDarkColorScheme else AppleLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
