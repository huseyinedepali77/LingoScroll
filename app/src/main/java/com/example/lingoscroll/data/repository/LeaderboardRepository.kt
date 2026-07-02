package com.example.lingoscroll.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

data class LeaderboardEntry(
    val uid: String = "",
    val name: String = "",
    val score: Int = 0,
    val rank: String = "",
    val timestamp: Long = 0L
)

interface LeaderboardRepository {
    suspend fun submitScore(entry: LeaderboardEntry): Boolean
    suspend fun getTopScores(limit: Int): List<LeaderboardEntry>
}

class FirebaseLeaderboardRepository : LeaderboardRepository {
    private val database = FirebaseDatabase.getInstance().getReference("leaderboard")

    override suspend fun submitScore(entry: LeaderboardEntry): Boolean {
        return try {
            database.child(entry.uid).setValue(entry).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getTopScores(limit: Int): List<LeaderboardEntry> {
        return try {
            val snapshot = database.orderByChild("score").limitToLast(limit).get().await()
            val entries = mutableListOf<LeaderboardEntry>()
            for (child in snapshot.children) {
                val entry = child.getValue(LeaderboardEntry::class.java)
                if (entry != null) {
                    entries.add(entry)
                }
            }
            // Realtime Database outputs ascending order, we reverse it to display descending (highest score first)
            entries.reverse()
            entries
        } catch (e: Exception) {
            emptyList()
        }
    }
}
