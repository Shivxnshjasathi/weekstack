package com.zincstate.hepta.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.os.Build

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
fun HeptaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StrictDarkColorScheme
        else -> SimpleLightColorScheme
    }
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