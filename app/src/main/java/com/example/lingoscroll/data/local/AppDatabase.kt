package com.example.lingoscroll.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SurvivalCard::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): SurvivalCardDao

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

        fun loadOfflineQuestions(context: Context, cardDao: SurvivalCardDao) {
            try {
                val jsonString = context.assets.open("survival_questions.json").bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                val cards = mutableListOf<SurvivalCard>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    
                    val optionsArray = obj.getJSONArray("options")
                    val optionsList = mutableListOf<String>()
                    for (j in 0 until optionsArray.length()) {
                        optionsList.add(optionsArray.getString(j))
                    }
                    
                    cards.add(
                        SurvivalCard(
                            id = obj.getInt("id"),
                            category = obj.getString("category"),
                            mechanicType = obj.getString("mechanicType"),
                            scenarioTr = obj.getString("scenarioTr"),
                            targetEn = obj.getString("targetEn"),
                            optionsRaw = optionsList.joinToString("|"),
                            difficulty = obj.optInt("difficulty", 3),
                            nextReviewDate = 0L
                        )
                    )
                }
                
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

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    loadOfflineQuestions(context, database.cardDao())
                }
            }
        }
    }
}
