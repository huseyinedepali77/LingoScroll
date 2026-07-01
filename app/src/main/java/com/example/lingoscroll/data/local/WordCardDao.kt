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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCards(cards: List<WordCard>)

    @Update
    suspend fun updateCard(card: WordCard)

    @Query("DELETE FROM word_cards")
    suspend fun deleteAll()
}
