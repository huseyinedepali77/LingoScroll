package com.example.lingoscroll.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SurvivalPrimary,
    secondary = SurvivalPrimary,
    background = SurvivalBg,
    surface = SurvivalSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SurvivalText,
    onSurface = SurvivalText,
    error = SurvivalDanger
)

private val LightColorScheme = lightColorScheme(
    primary = SurvivalPrimary,
    secondary = SurvivalPrimary,
    background = SurvivalBg,
    surface = SurvivalSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SurvivalText,
    onSurface = SurvivalText,
    error = SurvivalDanger
)

@Composable
fun LingoScrollTheme(
    darkTheme: Boolean = false, // Dinamik veya sistem koyu teması yerine sabit göz dinlendirici palet
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
