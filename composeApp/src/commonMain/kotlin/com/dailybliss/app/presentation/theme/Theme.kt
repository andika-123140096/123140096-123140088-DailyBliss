package com.dailybliss.app.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Sage Green Theme (Default)
private val SageGreenColorScheme = lightColorScheme(
    primary = Color(0xFF6B8E23), // Olive drab
    primaryContainer = Color(0xFFF1F8E9),
    onPrimary = Color.White,
    secondary = Color(0xFF556B2F), // Dark olive green
    background = Color(0xFFFBFCF8),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF767676),
)

// Rose Pink Theme
private val RosePinkColorScheme = lightColorScheme(
    primary = Color(0xFFD81B60),
    primaryContainer = Color(0xFFFCE4EC),
    onPrimary = Color.White,
    secondary = Color(0xFFAD1457),
    background = Color(0xFFFFF9FB),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF767676),
)

// Ocean Blue Theme
private val OceanBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0277BD),
    primaryContainer = Color(0xFFE1F5FE),
    onPrimary = Color.White,
    secondary = Color(0xFF01579B),
    background = Color(0xFFF9FDFF),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF767676),
)

// Lavender Theme
private val LavenderColorScheme = lightColorScheme(
    primary = Color(0xFF7E57C2),
    primaryContainer = Color(0xFFF3E5F5),
    onPrimary = Color.White,
    secondary = Color(0xFF5E35B1),
    background = Color(0xFFFAF9FF),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF767676),
)

// Monochrome Theme
private val MonochromeColorScheme = lightColorScheme(
    primary = Color(0xFF333333),
    primaryContainer = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    secondary = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF767676),
)

@Composable
fun DailyBlissTheme(darkTheme: Boolean = false, themeName: String = "Sage Green", content: @Composable () -> Unit) {
    // darkTheme is ignored for now as requested
    val colorScheme = when (themeName) {
        "Ocean Blue" -> OceanBlueColorScheme
        "Rose Pink" -> RosePinkColorScheme
        "Lavender" -> LavenderColorScheme
        "Monochrome" -> MonochromeColorScheme
        else -> SageGreenColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
