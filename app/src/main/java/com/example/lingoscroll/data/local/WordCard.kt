package com.example.lingoscroll.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_cards")
data class WordCard(
    @PrimaryKey val id: Int,
    val source_lang: String = "TR",
    val target_lang: String = "EN",
    val expression: String,       // İngilizce deyim/kelime
    val translation: String,      // Türkçe karşılığı
    val example_sentence: String, // Sokak dilinden örnek kullanım
    val difficulty_score: Int = 3, // 1-5 arası, Spaced Repetition için
    val next_review_date: Long = 0L, // Long/Timestamp
    val level: String,            // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    val type: String = "CARD",     // "CARD", "QUIZ_MULTIPLE_CHOICE", "QUIZ_COMPLETION"
    val optionsRaw: String = "",   // Seçenekler (örn: "Opt1|Opt2|Opt3|Opt4")
    val correctAnswer: String = "",
    val attempts: Int = 0,
    val correct_count: Int = 0,
    val category: String = "CASUAL", // "TRAVEL", "BUSINESS", "CASUAL", "MIXED"
    val variationsRaw: String = ""  // Alternatif cümleler (örn: "Sentence1||Sentence2")
) {
    val optionsList: List<String>
        get() = if (optionsRaw.isEmpty()) emptyList() else optionsRaw.split("|")

    val variationsList: List<String>
        get() = if (variationsRaw.isEmpty()) emptyList() else variationsRaw.split("||")
}
