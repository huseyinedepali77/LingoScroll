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
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val cardDao = database.cardDao()

                    // Seviye tespit sınav sorularını ekle
                    val diagCards = LearningContent.diagnosticQuestions.map {
                        WordCard(
                            id = it.id,
                            type = it.type.name,
                            expression = it.phrase,
                            translation = it.translation,
                            example_sentence = it.context,
                            level = it.level.name,
                            optionsRaw = it.options.joinToString("|"),
                            correctAnswer = it.correctAnswer,
                            category = it.category,
                            variationsRaw = it.variations.joinToString("||")
                        )
                    }

                    // Genel pratik kelime/cümle kartlarını ekle
                    val practiceCards = LearningContent.practiceItems.map {
                        WordCard(
                            id = it.id,
                            type = it.type.name,
                            expression = it.phrase,
                            translation = it.translation,
                            example_sentence = it.context,
                            level = it.level.name,
                            optionsRaw = it.options.joinToString("|"),
                            correctAnswer = it.correctAnswer,
                            category = it.category,
                            variationsRaw = it.variations.joinToString("||")
                        )
                    }

                    cardDao.insertCards(diagCards)
                    cardDao.insertCards(practiceCards)
                }
            }
        }
    }
}
