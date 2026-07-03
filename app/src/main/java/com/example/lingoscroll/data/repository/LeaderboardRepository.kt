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
    suspend fun deleteScore(nickname: String): Boolean
}

class FirebaseLeaderboardRepository : LeaderboardRepository {
    private val database = FirebaseDatabase.getInstance("https://lingoscroll-37efe-default-rtdb.firebaseio.com").getReference("leaderboard")

    override suspend fun submitScore(entry: LeaderboardEntry): Boolean {
        return try {
            database.child(entry.name).setValue(entry)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteScore(nickname: String): Boolean {
        return try {
            database.child(nickname).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getTopScores(limit: Int): List<LeaderboardEntry> {
        return try {
            kotlinx.coroutines.withTimeout(15000L) {
                // Fetch the whole node and sort client-side to bypass index rule constraints
                val snapshot = database.get().await()
                val entries = mutableListOf<LeaderboardEntry>()
                for (child in snapshot.children) {
                    val entry = child.getValue(LeaderboardEntry::class.java)
                    if (entry != null) {
                        entries.add(entry)
                    }
                }
                // Sort descending (highest score first) and take the top N entries
                entries.sortByDescending { it.score }
                entries.take(limit)
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseDatabase", "getTopScores failed", e)
            emptyList()
        }
    }
}
