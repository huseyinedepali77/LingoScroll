package com.example.lingoscroll.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PastelPrimary,
    secondary = PastelSecondary,
    tertiary = PastelTertiary,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color(0xFF0F1A24),
    onSecondary = Color(0xFF10281F),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = PastelRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E6B9E),
    secondary = Color(0xFF2C6B50),
    tertiary = Color(0xFF8E4B33),
    background = LightBg,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    error = Color(0xFFC62828)
)

@Composable
fun LingoScrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
