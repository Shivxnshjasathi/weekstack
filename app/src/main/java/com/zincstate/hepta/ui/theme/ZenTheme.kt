package com.zincstate.hepta.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

enum class ZenTheme(val displayName: String) {
    OBSIDIAN("Obsidian"),
    ARCTIC("Arctic"),
    SEPIA("Sepia"),
    NORD("Nord"),
    GRAPHITE("Graphite"),
    SLATE("Slate"),
    FOREST("Forest"),
    WINE("Wine"),
    SAND("Sand"),
    MIDNIGHT("Midnight"),
    EVERFOREST("Everforest"),
    ROSE_PINE("Rose Pine"),
    CYBER("Cyber"),
    SOLARIZED("Solarized"),
    COBALT("Cobalt")
}

data class ZenColors(
    val colorScheme: ColorScheme,
    val headerShades: List<Color>
)

fun getZenColors(theme: ZenTheme): ZenColors {
    return when (theme) {
        ZenTheme.OBSIDIAN -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFFE0E0E0),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onBackground = Color(0xFFFFFFFF),
                onSurface = Color(0xFFFFFFFF)
            ),
            headerShades = listOf(
                Color(0xFF2D2D2D), Color(0xFF282828), Color(0xFF232323),
                Color(0xFF1E1E1E), Color(0xFF1A1A1A), Color(0xFF161616), Color(0xFF121212)
            )
        )
        ZenTheme.ARCTIC -> ZenColors(
            colorScheme = lightColorScheme(
                primary = Color(0xFF333333),
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFF7F7F7),
                onBackground = Color(0xFF121212),
                onSurface = Color(0xFF121212)
            ),
            headerShades = listOf(
                Color(0xFFEEEEEE), Color(0xFFE5E5E5), Color(0xFFDDDDDD),
                Color(0xFFD4D4D4), Color(0xFFCCCCCC), Color(0xFFC3C3C3), Color(0xFFBBBBBB)
            )
        )
        ZenTheme.SEPIA -> ZenColors(
            colorScheme = lightColorScheme(
                primary = Color(0xFF5D4037),
                background = Color(0xFFF4ECD8),
                surface = Color(0xFFE8DFC8),
                onBackground = Color(0xFF3E2723),
                onSurface = Color(0xFF3E2723)
            ),
            headerShades = listOf(
                Color(0xFFEFE6D2), Color(0xFFE8DFC8), Color(0xFFE1D8BF),
                Color(0xFFDAD1B6), Color(0xFFD3CBAD), Color(0xFFCDC4A4), Color(0xFFC6BD9B)
            )
        )
        ZenTheme.NORD -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF88C0D0),
                background = Color(0xFF2E3440),
                surface = Color(0xFF3B4252),
                onBackground = Color(0xFFECEFF4),
                onSurface = Color(0xFFECEFF4)
            ),
            headerShades = listOf(
                Color(0xFF4C566A), Color(0xFF434C5E), Color(0xFF3B4252),
                Color(0xFF343B49), Color(0xFF2E3440), Color(0xFF272D37), Color(0xFF21252E)
            )
        )
        ZenTheme.GRAPHITE -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFFAAAAAA),
                background = Color(0xFF242424),
                surface = Color(0xFF2D2D2D),
                onBackground = Color(0xFFE0E0E0),
                onSurface = Color(0xFFE0E0E0)
            ),
            headerShades = listOf(
                Color(0xFF3A3A3A), Color(0xFF353535), Color(0xFF303030),
                Color(0xFF2D2D2D), Color(0xFF282828), Color(0xFF242424), Color(0xFF1F1F1F)
            )
        )
        ZenTheme.SLATE -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF94A3B8),
                background = Color(0xFF1E293B),
                surface = Color(0xFF334155),
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF1F5F9)
            ),
            headerShades = listOf(
                Color(0xFF475569), Color(0xFF3F4E60), Color(0xFF334155),
                Color(0xFF2A3749), Color(0xFF1E293B), Color(0xFF162132), Color(0xFF0F172A)
            )
        )
        ZenTheme.FOREST -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF8BA88B),
                background = Color(0xFF141E14),
                surface = Color(0xFF1B261B),
                onBackground = Color(0xFFE0E8E0),
                onSurface = Color(0xFFE0E8E0)
            ),
            headerShades = listOf(
                Color(0xFF263326), Color(0xFF202B20), Color(0xFF1B261B),
                Color(0xFF161E16), Color(0xFF111711), Color(0xFF0C100C), Color(0xFF070907)
            )
        )
         ZenTheme.WINE -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFFA88B8B),
                background = Color(0xFF1E1414),
                surface = Color(0xFF261B1B),
                onBackground = Color(0xFFE8E0E0),
                onSurface = Color(0xFFE8E0E0)
            ),
            headerShades = listOf(
                Color(0xFF332626), Color(0xFF2B2020), Color(0xFF261B1B),
                Color(0xFF1E1616), Color(0xFF171111), Color(0xFF100C0C), Color(0xFF090707)
            )
        )
        ZenTheme.SAND -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFFA8A38B),
                background = Color(0xFF1E1C14),
                surface = Color(0xFF26231B),
                onBackground = Color(0xFFE8E5E0),
                onSurface = Color(0xFFE8E5E0)
            ),
            headerShades = listOf(
                Color(0xFF333126), Color(0xFF2B2920), Color(0xFF26231B),
                Color(0xFF1E1C16), Color(0xFF171611), Color(0xFF100F0C), Color(0xFF090807)
            )
        )
        ZenTheme.MIDNIGHT -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF6366F1),
                background = Color(0xFF0F172A),
                surface = Color(0xFF1E293B),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF8FAFC)
            ),
            headerShades = listOf(
                Color(0xFF334155), Color(0xFF2A3749), Color(0xFF1E293B),
                Color(0xFF162132), Color(0xFF0F172A), Color(0xFF0B1221), Color(0xFF080D18)
            )
        )
        ZenTheme.EVERFOREST -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFFA7C080),
                background = Color(0xFF2D353B),
                surface = Color(0xFF343F44),
                onBackground = Color(0xFFD3C6AA),
                onSurface = Color(0xFFD3C6AA)
            ),
            headerShades = listOf(
                Color(0xFF475258), Color(0xFF3D484D), Color(0xFF343F44),
                Color(0xFF2D353B), Color(0xFF232A2E), Color(0xFF1E2326), Color(0xFF1A1D21)
            )
        )
        ZenTheme.ROSE_PINE -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFFEBBCBA),
                background = Color(0xFF191724),
                surface = Color(0xFF1F1D2E),
                onBackground = Color(0xFFE0DEF4),
                onSurface = Color(0xFFE0DEF4)
            ),
            headerShades = listOf(
                Color(0xFF26233A), Color(0xFF232136), Color(0xFF1F1D2E),
                Color(0xFF191724), Color(0xFF16141F), Color(0xFF12101A), Color(0xFF0F0D15)
            )
        )
        ZenTheme.CYBER -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00FFFF),
                background = Color(0xFF050505),
                surface = Color(0xFF111111),
                onBackground = Color(0xFFFFFFFF),
                onSurface = Color(0xFFFFFFFF)
            ),
            headerShades = listOf(
                Color(0xFF222222), Color(0xFF1A1A1A), Color(0xFF111111),
                Color(0xFF0A0A0A), Color(0xFF050505), Color(0xFF000000), Color(0xFF000000)
            )
        )
        ZenTheme.SOLARIZED -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF268BD2),
                background = Color(0xFF002B36),
                surface = Color(0xFF073642),
                onBackground = Color(0xFF839496),
                onSurface = Color(0xFF839496)
            ),
            headerShades = listOf(
                Color(0xFF586E75), Color(0xFF073642), Color(0xFF002B36),
                Color(0xFF00212B), Color(0xFF001920), Color(0xFF001116), Color(0xFF000A0D)
            )
        )
        ZenTheme.COBALT -> ZenColors(
            colorScheme = darkColorScheme(
                primary = Color(0xFF82AAFF),
                background = Color(0xFF002240),
                surface = Color(0xFF003366),
                onBackground = Color(0xFFFFFFFF),
                onSurface = Color(0xFFFFFFFF)
            ),
            headerShades = listOf(
                Color(0xFF004488), Color(0xFF003366), Color(0xFF002240),
                Color(0xFF001A33), Color(0xFF001121), Color(0xFF000B16), Color(0xFF00060B)
            )
        )
    }
}
