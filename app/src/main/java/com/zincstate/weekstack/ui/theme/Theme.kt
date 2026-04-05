package com.zincstate.weekstack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StrictDarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    secondary = LightGrey,
    tertiary = SubtleGrey,
    background = DeepCharcoal,
    surface = SurfaceDark,
    onPrimary = DeepCharcoal,
    onSecondary = DeepCharcoal,
    onTertiary = HighContrastWhite,
    onBackground = HighContrastWhite,
    onSurface = HighContrastWhite,
)

private val SimpleLightColorScheme = lightColorScheme(
    primary = LightAccent,
    secondary = LightOnSurface,
    tertiary = LightHeader1,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightOnSurface,
    onTertiary = LightOnSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
)

// Helper to get zebra shades for each mode
@Composable
fun getHeaderShades(isDark: Boolean): List<androidx.compose.ui.graphics.Color> {
    return if (isDark) {
        listOf(HeaderGrey1, HeaderGrey2, HeaderGrey3, HeaderGrey4, HeaderGrey5, HeaderGrey6, HeaderGrey7)
    } else {
        listOf(LightHeader1, LightHeader2, LightHeader3, LightHeader4, LightHeader5, LightHeader6, LightHeader7)
    }
}

@Composable
fun ZenStackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StrictDarkColorScheme else SimpleLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}