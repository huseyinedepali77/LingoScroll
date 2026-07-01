package com.example.lingoscroll.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lingoscroll.data.local.AppDatabase
import com.example.lingoscroll.data.local.SurvivalCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Arka plan senkronizasyonu başlatıldı (Wi-Fi)")
        
        return try {
            val urlString = "https://raw.githubusercontent.com/mock-repo/lingoscroll-data/main/survival_cards.json"
            val newCards = fetchWordsFromServer(urlString)
            
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
            val dao = db.cardDao()
            
            if (newCards.isNotEmpty()) {
                dao.insertCards(newCards)
                Log.d("SyncWorker", "${newCards.size} adet yeni acil durum kartı Room DB'ye eklendi.")
            } else {
                val fallbackCards = getTrendingWordsFallback()
                dao.insertCards(fallbackCards)
                Log.d("SyncWorker", "Sunucu çevrimdışı. ${fallbackCards.size} adet yedek acil durum kartı eklendi.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Senkronizasyon hatası", e)
            Result.retry()
        }
    }

    private fun fetchWordsFromServer(urlString: String): List<SurvivalCard> {
        val cardsList = mutableListOf<SurvivalCard>()
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                val jsonArray = JSONArray(response.toString())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    cardsList.add(
                        SurvivalCard(
                            id = obj.getInt("id"),
                            category = obj.getString("category"),
                            mechanicType = obj.getString("mechanicType"),
                            scenarioTr = obj.getString("scenarioTr"),
                            targetEn = obj.getString("targetEn"),
                            optionsRaw = obj.optString("optionsRaw", ""),
                            difficulty = obj.optInt("difficulty", 3),
                            nextReviewDate = 0L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "HTTP veri çekme başarısız", e)
        } finally {
            connection?.disconnect()
        }
        return cardsList
    }

    private fun getTrendingWordsFallback(): List<SurvivalCard> {
        return listOf(
            SurvivalCard(
                id = 9001,
                category = "FINANCE",
                mechanicType = "SKELETON",
                scenarioTr = "Kartım ATM'de sıkıştı de.",
                targetEn = "My card is stuck in the ATM.",
                optionsRaw = "",
                difficulty = 3
            ),
            SurvivalCard(
                id = 9002,
                category = "CRISIS",
                mechanicType = "SWIPE",
                scenarioTr = "Taksici taksimetreyi açmayı reddediyor.",
                targetEn = "Turn on the meter, please.",
                optionsRaw = "Turn on the meter, please.|I will pay whatever.",
                difficulty = 2
            ),
            SurvivalCard(
                id = 9003,
                category = "BASIC_NEEDS",
                mechanicType = "CHUNK",
                scenarioTr = "Tuvaletin nerede olduğunu sor.",
                targetEn = "Where is the restroom?",
                optionsRaw = "Where is|the|restroom?",
                difficulty = 1
            )
        )
    }
}
