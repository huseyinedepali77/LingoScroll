package com.example.lingoscroll.data.repository

import com.example.lingoscroll.data.local.SurvivalCardDao
import com.example.lingoscroll.data.local.SurvivalCard
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class CardRepository(private val cardDao: SurvivalCardDao) {

    fun getCardsByCategory(category: String): Flow<List<SurvivalCard>> {
        return cardDao.getCardsByCategoryFlow(category)
    }

    suspend fun getCardById(id: Int): SurvivalCard? {
        return cardDao.getCardById(id)
    }

    suspend fun insertCards(cards: List<SurvivalCard>) {
        cardDao.insertCards(cards)
    }

    suspend fun deleteAll() {
        cardDao.deleteAll()
    }

    // Spaced Repetition (Leitner) güncellemesi
    suspend fun updateCardProgress(cardId: Int, isCorrect: Boolean) {
        val card = cardDao.getCardById(cardId) ?: return
        val currentTime = System.currentTimeMillis()
        
        val newDifficulty: Int
        val intervalDays: Long

        if (isCorrect) {
            newDifficulty = (card.difficulty - 1).coerceAtLeast(1)
            intervalDays = when (newDifficulty) {
                1 -> 7L   // Kolay kartlar 7 gün sonra
                2 -> 4L   // Orta kartlar 4 gün sonra
                else -> 2L // Zor kartlar 2 gün sonra
            }
        } else {
            newDifficulty = 3 // En zor seviyeye yükselt
            intervalDays = 0L // Hemen tekrar gösterilecek
        }

        val updatedCard = card.copy(
            difficulty = newDifficulty,
            nextReviewDate = currentTime + TimeUnit.DAYS.toMillis(intervalDays),
            attempts = card.attempts + 1,
            correctCount = if (isCorrect) card.correctCount + 1 else card.correctCount
        )
        
        cardDao.updateCard(updatedCard)
    }

    // 15 Soruluk Hayatta Kalma Oturumu (9 Yeni + 6 Tekrar)
    suspend fun getStagePackage(category: String, excludeIds: List<Int>, currentTime: Long): List<SurvivalCard> {
        val safeExclude = if (excludeIds.isEmpty()) listOf(-1) else excludeIds
        
        // 1. Tekrar zamanı gelmiş 6 adet soruyu getir
        var reviews = cardDao.getDueReviewQuestions(category, currentTime, 6)
        
        // Yeterli tekrar sorusu yoksa, önceden çözülmüş herhangi sorulardan tamamla
        if (reviews.size < 6) {
            val needed = 6 - reviews.size
            val fallback = cardDao.getAnyReviewQuestions(category, needed)
            reviews = reviews + fallback
        }
        
        // 2. 9 adet yeni soru getir (son çözülenler hariç)
        val newQuestions = cardDao.getNewQuestions(category, safeExclude, 9)
        
        // 3. Birleştir ve karıştır
        val combined = (newQuestions + reviews).distinctBy { it.id }.shuffled()
        
        // Eğer havuz darsa ve 15'e ulaşamadıysa, filtreleri kaldırarak tamamla
        if (combined.size < 15) {
            val needed = 15 - combined.size
            val fallbackAll = cardDao.getNewQuestions(category, listOf(-1), needed)
            return (combined + fallbackAll).distinctBy { it.id }.take(15)
        }
        
        return combined.take(15)
    }

    suspend fun getRedCodeCards(): List<SurvivalCard> {
        return cardDao.getRedCodeCards()
    }
}
