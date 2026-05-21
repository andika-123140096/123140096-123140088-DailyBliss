package com.dailybliss.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun SystemAppearance(isLight: Boolean)

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

private val DarkSageGreenColorScheme = darkColorScheme(
    primary = Color(0xFF9CCC65),
    primaryContainer = Color(0xFF33691E),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFAED581),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF999999),
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

private val DarkRosePinkColorScheme = darkColorScheme(
    primary = Color(0xFFF06292),
    primaryContainer = Color(0xFF880E4F),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFF48FB1),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF999999),
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

private val DarkOceanBlueColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    primaryContainer = Color(0xFF01579B),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF81D4FA),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF999999),
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

private val DarkLavenderColorScheme = darkColorScheme(
    primary = Color(0xFFB39DDB),
    primaryContainer = Color(0xFF4527A0),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFD1C4E9),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF999999),
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

private val DarkMonochromeColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    primaryContainer = Color(0xFF424242),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFBDBDBD),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF999999),
)

@Composable
fun DailyBlissTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "Sage Green",
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        when (themeName) {
            "Ocean Blue" -> DarkOceanBlueColorScheme
            "Rose Pink" -> DarkRosePinkColorScheme
            "Lavender" -> DarkLavenderColorScheme
            "Monochrome" -> DarkMonochromeColorScheme
            else -> DarkSageGreenColorScheme
        }
    } else {
        when (themeName) {
            "Ocean Blue" -> OceanBlueColorScheme
            "Rose Pink" -> RosePinkColorScheme
            "Lavender" -> LavenderColorScheme
            "Monochrome" -> MonochromeColorScheme
            else -> SageGreenColorScheme
        }
    }

    SystemAppearance(!darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
