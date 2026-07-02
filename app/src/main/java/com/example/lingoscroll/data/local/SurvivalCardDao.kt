package com.example.lingoscroll.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SurvivalCardDao {
    @Query("SELECT * FROM survival_cards")
    fun getAllCardsFlow(): Flow<List<SurvivalCard>>

    @Query("SELECT * FROM survival_cards WHERE category = :category")
    fun getCardsByCategoryFlow(category: String): Flow<List<SurvivalCard>>

    @Query("SELECT * FROM survival_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Int): SurvivalCard?

    @Query("SELECT * FROM survival_cards WHERE (category = :category OR :category = 'MIXED') AND id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getNewQuestions(category: String, excludeIds: List<Int>, limit: Int): List<SurvivalCard>

    @Query("SELECT * FROM survival_cards WHERE (category = :category OR :category = 'MIXED') AND nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC LIMIT :limit")
    suspend fun getDueReviewQuestions(category: String, currentTime: Long, limit: Int): List<SurvivalCard>

    @Query("SELECT * FROM survival_cards WHERE (category = :category OR :category = 'MIXED') AND attempts > 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getAnyReviewQuestions(category: String, limit: Int): List<SurvivalCard>

    @Query("SELECT * FROM survival_cards WHERE mechanicType IN ('SKELETON', 'SWIPE')")
    suspend fun getRedCodeCards(): List<SurvivalCard>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCards(cards: List<SurvivalCard>)

    @Update
    suspend fun updateCard(card: SurvivalCard)

    @Query("DELETE FROM survival_cards")
    suspend fun deleteAll()
}
