package com.example.lingoscroll.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM learning_cards")
    fun getAllCardsFlow(): Flow<List<LearningCard>>

    @Query("SELECT * FROM learning_cards WHERE level = :level")
    fun getCardsByLevelFlow(level: String): Flow<List<LearningCard>>

    @Query("SELECT * FROM learning_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Int): LearningCard?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCards(cards: List<LearningCard>)

    @Update
    suspend fun updateCard(card: LearningCard)

    @Query("DELETE FROM learning_cards")
    suspend fun deleteAll()
}
