package com.example.lingoscroll.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lingoscroll_prefs", Context.MODE_PRIVATE)

    fun getUserLevel(): Level? {
        val levelStr = prefs.getString("user_level", null) ?: return null
        return try {
            Level.valueOf(levelStr)
        } catch (e: Exception) {
            null
        }
    }

    fun setUserLevel(level: Level) {
        prefs.edit().putString("user_level", level.name).apply()
    }

    fun getUserStyle(): String {
        return prefs.getString("user_style", "MIXED") ?: "MIXED"
    }

    fun setUserStyle(style: String) {
        prefs.edit().putString("user_style", style).apply()
    }

    fun clearUserProgress() {
        val buildTime = getSavedBuildTime()
        prefs.edit().clear().apply()
        if (buildTime != null) {
            setSavedBuildTime(buildTime)
        }
    }

    fun getSavedBuildTime(): String? = prefs.getString("saved_build_time", null)

    fun setSavedBuildTime(buildTime: String) {
        prefs.edit().putString("saved_build_time", buildTime).apply()
    }

    fun getStreak(): Int = prefs.getInt("streak_count", 0)

    fun setStreak(streak: Int) {
        prefs.edit().putInt("streak_count", streak).apply()
    }

    fun getLastActiveDate(): String? = prefs.getString("last_active_date", null)

    fun setLastActiveDate(date: String) {
        prefs.edit().putString("last_active_date", date).apply()
    }

    fun getTotalSecondsSaved(): Long = prefs.getLong("total_seconds_saved", 0L)

    fun addSecondsSaved(seconds: Long) {
        val current = getTotalSecondsSaved()
        prefs.edit().putLong("total_seconds_saved", current + seconds).apply()
    }

    // Leitner Box yönetimi (1, 2 veya 3. kutu)
    fun getItemBox(itemId: Int): Int = prefs.getInt("item_box_$itemId", 1)

    fun setItemBox(itemId: Int, box: Int) {
        prefs.edit().putInt("item_box_$itemId", box.coerceIn(1, 3)).apply()
    }

    fun incrementAttempts(itemId: Int, correct: Boolean) {
        val attempts = prefs.getInt("item_attempts_$itemId", 0) + 1
        prefs.edit().putInt("item_attempts_$itemId", attempts).apply()
        if (correct) {
            val correctCount = prefs.getInt("item_correct_$itemId", 0) + 1
            prefs.edit().putInt("item_correct_$itemId", correctCount).apply()
        }
    }

    // Günlük seri (streak) takibi ve güncelleme mantığı
    fun updateStreak() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val lastActive = getLastActiveDate()

        if (lastActive == null) {
            setStreak(1)
            setLastActiveDate(todayStr)
        } else if (lastActive != todayStr) {
            // Dün mü girmiş yoksa daha mı eski kontrolü
            try {
                val todayDate = sdf.parse(todayStr)
                val lastDate = sdf.parse(lastActive)
                if (todayDate != null && lastDate != null) {
                    val diff = todayDate.time - lastDate.time
                    val diffDays = diff / (24 * 60 * 60 * 1000)

                    if (diffDays == 1L) {
                        // Arka arkaya girilmiş, seriyi artır
                        setStreak(getStreak() + 1)
                    } else if (diffDays > 1L) {
                        // Seri bozulmuş, sıfırla
                        setStreak(1)
                    }
                }
            } catch (e: Exception) {
                setStreak(1)
            }
            setLastActiveDate(todayStr)
        }
    }

    fun getSecondsSpentToday(): Long {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("time_spent_date", null)
        if (savedDate == todayStr) {
            return prefs.getLong("time_spent_seconds", 0L)
        }
        return 0L
    }

    fun addSecondsSpentToday(seconds: Long) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("time_spent_date", null)
        if (savedDate == todayStr) {
            val current = prefs.getLong("time_spent_seconds", 0L)
            prefs.edit().putLong("time_spent_seconds", current + seconds).apply()
        } else {
            prefs.edit()
                .putString("time_spent_date", todayStr)
                .putLong("time_spent_seconds", seconds)
                .apply()
        }
    }

    // TTS Telaffuz Hızı (Speech Rate) Ayarı (Örn: 1.0f, 0.75f, 0.5f)
    fun getTtsSpeechRate(): Float {
        return prefs.getFloat("tts_speech_rate", 1.0f)
    }

    fun setTtsSpeechRate(rate: Float) {
        prefs.edit().putFloat("tts_speech_rate", rate).apply()
    }

    // Son Akıllı Bildirim Gönderilme Zamanı (Timestamp)
    fun getLastNotificationSentTime(): Long {
        return prefs.getLong("last_notification_sent_time", 0L)
    }

    fun setLastNotificationSentTime(time: Long) {
        prefs.edit().putLong("last_notification_sent_time", time).apply()
    }

    // İstatistikler: Toplam cevaplanan soru sayısı ve doğru cevaplanan soru sayısı
    fun getTotalQuestionsAnswered(): Int {
        return prefs.getInt("total_questions_answered", 0)
    }

    fun getCorrectQuestionsAnswered(): Int {
        return prefs.getInt("correct_questions_answered", 0)
    }

    fun incrementQuestionsStats(isCorrect: Boolean) {
        val total = getTotalQuestionsAnswered() + 1
        val correct = if (isCorrect) getCorrectQuestionsAnswered() + 1 else getCorrectQuestionsAnswered()
        prefs.edit()
            .putInt("total_questions_answered", total)
            .putInt("correct_questions_answered", correct)
            .apply()
    }

    // Özel Akış URL'si (Senkronizasyon Bağlantısı)
    fun getCustomFeedUrl(): String {
        return prefs.getString("custom_feed_url", "https://raw.githubusercontent.com/huseyinedepali77/LingoScroll/main/feed.json") ?: "https://raw.githubusercontent.com/huseyinedepali77/LingoScroll/main/feed.json"
    }

    fun setCustomFeedUrl(url: String) {
        prefs.edit().putString("custom_feed_url", url).apply()
    }

    // Son çözülen 30 sorunun ID'sini saklayan kalıcı hafıza listesi (Sıfır Tekrar Motoru)
    fun getRecentSeenIds(): List<Int> {
        val raw = prefs.getString("recent_seen_ids", "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun addRecentSeenId(id: Int) {
        val current = getRecentSeenIds().toMutableList()
        if (id in current) {
            current.remove(id)
        }
        current.add(id)
        // Son 30 soruyu koru
        while (current.size > 30) {
            current.removeAt(0)
        }
        prefs.edit().putString("recent_seen_ids", current.joinToString(",")).apply()
    }

    fun clearRecentSeenIds() {
        prefs.edit().remove("recent_seen_ids").apply()
    }

    // Oyunlaştırılmış Aşama (Stage) Yönetimi
    fun getCurrentStage(): Int {
        return prefs.getInt("current_stage", 1)
    }

    fun setCurrentStage(stage: Int) {
        prefs.edit().putInt("current_stage", stage).apply()
    }

    fun getStageProgress(): Int {
        return prefs.getInt("stage_progress", 0)
    }

    fun setStageProgress(progress: Int) {
        prefs.edit().putInt("stage_progress", progress.coerceIn(0, 15)).apply()
    }

    fun incrementStageProgress() {
        val current = getStageProgress()
        setStageProgress(current + 1)
    }
}
