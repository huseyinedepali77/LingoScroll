package com.example.lingoscroll.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {
    @Query("SELECT * FROM word_cards")
    fun getAllCardsFlow(): Flow<List<WordCard>>

    @Query("SELECT * FROM word_cards WHERE level = :level")
    fun getCardsByLevelFlow(level: String): Flow<List<WordCard>>

    @Query("SELECT * FROM word_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Int): WordCard?

    @Query("SELECT * FROM word_cards WHERE level = :level AND (category = :category OR :category = 'MIXED') AND id NOT IN (:excludeIds) AND (id < 100 OR id > 199) AND type != 'CARD' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getNewQuestions(level: String, category: String, excludeIds: List<Int>, limit: Int): List<WordCard>

    @Query("SELECT * FROM word_cards WHERE level = :level AND (category = :category OR :category = 'MIXED') AND (id < 100 OR id > 199) AND type != 'CARD' AND next_review_date <= :currentTime ORDER BY next_review_date ASC LIMIT :limit")
    suspend fun getDueReviewQuestions(level: String, category: String, currentTime: Long, limit: Int): List<WordCard>

    @Query("SELECT * FROM word_cards WHERE level = :level AND (category = :category OR :category = 'MIXED') AND (id < 100 OR id > 199) AND type != 'CARD' AND attempts > 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getAnyReviewQuestions(level: String, category: String, limit: Int): List<WordCard>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCards(cards: List<WordCard>)

    @Update
    suspend fun updateCard(card: WordCard)

    @Query("DELETE FROM word_cards")
    suspend fun deleteAll()
}
