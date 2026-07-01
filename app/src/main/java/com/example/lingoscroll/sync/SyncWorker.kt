package com.example.lingoscroll.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lingoscroll.data.local.AppDatabase
import com.example.lingoscroll.data.local.WordCard
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
            // Eşitleme yapılacak sunucu URL'si (örnek mock api)
            val urlString = "https://raw.githubusercontent.com/mock-repo/lingoscroll-data/main/words.json"
            val newCards = fetchWordsFromServer(urlString)
            
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
            val dao = db.cardDao()
            
            if (newCards.isNotEmpty()) {
                dao.insertCards(newCards)
                Log.d("SyncWorker", "${newCards.size} adet yeni kelime/deyim Room DB'ye eklendi.")
            } else {
                // Sunucu çevrimdışı olduğunda simüle edilen fallback kelimeleri ekle
                val fallbackCards = getTrendingWordsFallback()
                dao.insertCards(fallbackCards)
                Log.d("SyncWorker", "Sunucu çevrimdışı. ${fallbackCards.size} adet güncel yedek kelime/deyim eklendi.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Senkronizasyon hatası", e)
            Result.retry()
        }
    }

    private fun fetchWordsFromServer(urlString: String): List<WordCard> {
        val cardsList = mutableListOf<WordCard>()
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
                        WordCard(
                            id = obj.getInt("id"),
                            source_lang = obj.optString("source_lang", "TR"),
                            target_lang = obj.optString("target_lang", "EN"),
                            type = obj.getString("type"),
                            expression = obj.getString("expression"),
                            translation = obj.getString("translation"),
                            example_sentence = obj.getString("example_sentence"),
                            level = obj.getString("level"),
                            optionsRaw = obj.optString("optionsRaw", ""),
                            correctAnswer = obj.optString("correctAnswer", "")
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

    private fun getTrendingWordsFallback(): List<WordCard> {
        return listOf(
            WordCard(
                id = 301,
                type = "CARD",
                expression = "Hit the sack",
                translation = "Kafayı vurup yatmak / Uyumaya gitmek",
                example_sentence = "Yorgun olduğumuzda uyumak anlamında sokakta sıkça kullanılan bir deyim.",
                level = "INTERMEDIATE"
            ),
            WordCard(
                id = 302,
                type = "QUIZ_MULTIPLE_CHOICE",
                expression = "Hangi sokak kalıbı 'Zor duruma katlanıp dişini sıkmak' anlamına gelir?",
                translation = "Bite the bullet",
                example_sentence = "Kaçınılmaz ve zor durumlara göğüs germeyi ifade eden popüler Amerikan deyimidir.",
                level = "ADVANCED",
                optionsRaw = "Bite the bullet|Break the ice|Hit the road|Spill the beans",
                correctAnswer = "Bite the bullet"
            ),
            WordCard(
                id = 303,
                type = "CARD",
                expression = "No sweat!",
                translation = "Lafı bile olmaz! / Terlemedim bile! / Çok kolay!",
                example_sentence = "Biri teşekkür ettiğinde 'rica ederim, benim için çok kolaydı' anlamında rahat bir sokak dilidir.",
                level = "BEGINNER"
            )
        )
    }
}
