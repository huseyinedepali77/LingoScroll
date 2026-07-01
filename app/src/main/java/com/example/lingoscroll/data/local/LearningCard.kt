package com.example.lingoscroll.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_cards")
data class LearningCard(
    @PrimaryKey val id: Int,
    val type: String,          // "CARD", "QUIZ_MULTIPLE_CHOICE", "QUIZ_COMPLETION"
    val phrase: String,        // İngilizce kelime/ifade
    val translation: String,   // Türkçe anlamı
    val context: String,       // Günlük sokak kullanımı açıklaması
    val level: String,         // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    val difficultyScore: Int = 3, // 1-5 arası zorluk derecesi
    val nextReviewDate: Long = 0L, // Milisaniye cinsinden bir sonraki tekrar tarihi
    val attempts: Int = 0,
    val correctCount: Int = 0,
    val box: Int = 1,          // Leitner Kutu numarası: 1-3
    val optionsRaw: String = "", // Seçenekler (örn: "Option A|Option B|Option C|Option D")
    val correctAnswer: String = "",
    val isCustom: Boolean = false // Eşitleme ile gelen harici kelime ayrımı
) {
    val optionsList: List<String>
        get() = if (optionsRaw.isEmpty()) emptyList() else optionsRaw.split("|")
}
