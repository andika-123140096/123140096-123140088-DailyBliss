package com.dailybliss.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun SystemAppearance(isLight: Boolean)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF111111),
    primaryContainer = Color(0xFFEEEEEE),
    onPrimary = Color.White,
    secondary = Color(0xFF555555),
    tertiary = Color(0xFF777777),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFFE0E0E0),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    primaryContainer = Color(0xFF333333),
    onPrimary = Color(0xFF111111),
    secondary = Color(0xFFAAAAAA),
    tertiary = Color(0xFF888888),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFEEEEEE),
    outline = Color(0xFF333333),
)

@Composable
fun DailyBlissTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    SystemAppearance(isLight = !darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
