package com.example.lingoscroll.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lingoscroll.data.LearningContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WordCard::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): WordCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lingoscroll_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(context.applicationContext, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        // assets/offline_questions.json dosyasını okuyup veritabanına yükler
        fun loadOfflineQuestions(context: Context, cardDao: WordCardDao) {
            try {
                val jsonString = context.assets.open("offline_questions.json").bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                val cards = mutableListOf<WordCard>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    
                    val optionsArray = obj.getJSONArray("options")
                    val optionsList = mutableListOf<String>()
                    for (j in 0 until optionsArray.length()) {
                        optionsList.add(optionsArray.getString(j))
                    }
                    
                    val variationsArray = obj.getJSONArray("variations")
                    val variationsList = mutableListOf<String>()
                    for (j in 0 until variationsArray.length()) {
                        variationsList.add(variationsArray.getString(j))
                    }
                    
                    cards.add(
                        WordCard(
                            id = obj.getInt("id"),
                            type = obj.getString("type"),
                            level = obj.getString("level"),
                            expression = obj.getString("phrase"),
                            translation = obj.getString("translation"),
                            example_sentence = obj.getString("context"),
                            optionsRaw = optionsList.joinToString("|"),
                            correctAnswer = obj.getString("correctAnswer"),
                            category = obj.getString("category"),
                            variationsRaw = variationsList.joinToString("||")
                        )
                    )
                }
                
                // Toplu veritabanı kaydı
                kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                    cardDao.insertCards(cards)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    loadOfflineQuestions(context, database.cardDao())
                }
            }
        }
    }
}
