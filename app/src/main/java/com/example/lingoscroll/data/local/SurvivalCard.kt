package com.example.lingoscroll.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survival_cards")
data class SurvivalCard(
    @PrimaryKey val id: Int,
    val category: String,       // CRISIS, NAVIGATION, FINANCE, BASIC_NEEDS
    val mechanicType: String,   // SKELETON, SWIPE, CHUNK, ERROR_FIND
    val scenarioTr: String,     // Türkçe Senaryo açıklaması
    val targetEn: String,       // İngilizce hedef cümle
    val optionsRaw: String,     // Seçenekler (boru işareti '|' ile ayrılmış)
    val difficulty: Int = 3,    // Zorluk (1: Kolay, 2: Orta, 3: Zor)
    val nextReviewDate: Long = 0L, // Leitner Spaced Repetition zaman damgası
    val attempts: Int = 0,
    val correctCount: Int = 0
) {
    val optionsList: List<String>
        get() = if (optionsRaw.isEmpty()) emptyList() else optionsRaw.split("|")
}
