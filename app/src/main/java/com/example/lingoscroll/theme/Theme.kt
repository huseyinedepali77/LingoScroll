package com.example.lingoscroll.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkPrimary,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
    error = DarkDanger
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightPrimary,
    background = LightBg,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    error = LightDanger
)

@Composable
fun LingoScrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Aktif renkleri sistem/seçilen temaya göre güncelle
    if (darkTheme) {
        SurvivalBg = DarkBg
        SurvivalPrimary = DarkPrimary
        SurvivalDanger = DarkDanger
        SurvivalSurface = DarkSurface
        SurvivalText = DarkText
        SurvivalTextSecondary = DarkTextSecondary
        SurvivalBorder = DarkBorder
    } else {
        SurvivalBg = LightBg
        SurvivalPrimary = LightPrimary
        SurvivalDanger = LightDanger
        SurvivalSurface = LightSurface
        SurvivalText = LightText
        SurvivalTextSecondary = LightTextSecondary
        SurvivalBorder = LightBorder
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
