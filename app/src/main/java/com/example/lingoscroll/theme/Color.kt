package com.example.lingoscroll.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Survival V2 Light Palette (Varsayılan Aydınlık Tema)
val LightBg = Color(0xFFFAF9F6)
val LightPrimary = Color(0xFF2A9D8F)
val LightDanger = Color(0xFFE76F51)
val LightSurface = Color(0xFFFFFFFF)
val LightText = Color(0xFF2B2D42)
val LightTextSecondary = Color(0xFF6B7280)
val LightBorder = Color(0xFFE5E7EB)

// Survival V2 Dark Palette (Göz yormayan, gece konforlu kadife siyah tonlar)
val DarkBg = Color(0xFF0F0F12)          // Kadife Mat Gece Siyahı
val DarkPrimary = Color(0xFF38B2A6)     // Kontrastı optimize edilmiş nane yeşili
val DarkDanger = Color(0xFFF4A261)      // Parlamayan kiremit/turuncu
val DarkSurface = Color(0xFF1E1E24)     // Koyu kömür kart arka planı
val DarkText = Color(0xFFF1F1F4)        // Göz kamaştırmayan yumuşak beyaz
val DarkTextSecondary = Color(0xFF9CA3AF) // Muted gri
val DarkBorder = Color(0xFF2F3037)      // Çok hafif kenarlık rengi

// Aktif Renkler (State backed - Değiştiğinde tüm Composable'lar otomatik recompose olur)
var SurvivalBg by mutableStateOf(LightBg)
var SurvivalPrimary by mutableStateOf(LightPrimary)
var SurvivalDanger by mutableStateOf(LightDanger)
var SurvivalSurface by mutableStateOf(LightSurface)
var SurvivalText by mutableStateOf(LightText)
var SurvivalTextSecondary by mutableStateOf(LightTextSecondary)
var SurvivalBorder by mutableStateOf(LightBorder)
