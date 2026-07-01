package com.example.lingoscroll.data.repository

import com.example.lingoscroll.data.local.WordCardDao
import com.example.lingoscroll.data.local.WordCard
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class CardRepository(private val cardDao: WordCardDao) {

    // Seviyeye uygun kartları gerçek zamanlı akış (Flow) olarak dinler
    fun getCardsByLevel(level: String): Flow<List<WordCard>> {
        return cardDao.getCardsByLevelFlow(level)
    }

    // Tek bir kartı ID'si ile veritabanından çeker
    suspend fun getCardById(id: Int): WordCard? {
        return cardDao.getCardById(id)
    }

    // Toplu kart ekler
    suspend fun insertCards(cards: List<WordCard>) {
        cardDao.insertCards(cards)
    }

    // Tüm kartları siler
    suspend fun deleteAll() {
        cardDao.deleteAll()
    }

    // Spaced Repetition (Aralıklı Tekrar) Güncellemesi
    // Kullanıcının "Kolay / Doğru" veya "Zor / Yanlış" cevabına göre kartın durumunu günceller.
    suspend fun updateCardProgress(cardId: Int, isCorrect: Boolean) {
        val card = cardDao.getCardById(cardId) ?: return
        val currentTime = System.currentTimeMillis()
        
        val newDifficulty: Int
        val intervalDays: Long

        if (isCorrect) {
            // Başarılı: Zorluk puanını azalt (min 1), tekrar süresini uzat
            newDifficulty = (card.difficulty_score - 1).coerceAtLeast(1)
            intervalDays = when (newDifficulty) {
                1 -> 7L  // Kolay kartlar 7 gün sonra
                2 -> 4L  // Orta-kolay 4 gün sonra
                3 -> 2L  // Normal 2 gün sonra
                else -> 1L // Diğerleri 1 gün sonra
            }
        } else {
            // Başarısız: Zorluğu en yüksek (5) yap, hemen (0 gün) tekrar göster
            newDifficulty = 5
            intervalDays = 0L // Hemen tekrar göster
        }

        val updatedCard = card.copy(
            difficulty_score = newDifficulty,
            next_review_date = currentTime + TimeUnit.DAYS.toMillis(intervalDays),
            attempts = card.attempts + 1,
            correct_count = if (isCorrect) card.correct_count + 1 else card.correct_count
        )
        
        cardDao.updateCard(updatedCard)
    }
}
