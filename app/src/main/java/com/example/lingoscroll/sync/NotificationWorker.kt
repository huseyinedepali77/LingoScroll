package com.example.lingoscroll.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lingoscroll.MainActivity
import com.example.lingoscroll.data.PreferencesManager
import java.util.Calendar

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)

        // 1. Sessiz Saatler Kontrolü (Quiet Hours): 22:00 - 07:00
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 22 || currentHour < 7) {
            Log.d("NotificationWorker", "Sessiz saatler (22.00 - 07.00) aktif. Bildirim iptal edildi.")
            return Result.success()
        }

        // 2. Bugün Zaten Pratik Yaptı mı Kontrolü?
        // Eğer günlük aktif çalışma süresi 30 saniyeyi geçtiyse rahatsız etmiyoruz.
        if (prefs.getSecondsSpentToday() > 30L) {
            Log.d("NotificationWorker", "Kullanıcı bugün zaten pratik yaptı. Bildirim iptal edildi.")
            return Result.success()
        }

        // 3. En son bildirim gönderilme zamanı kontrolü (Sıklık Kontrolü)
        // Günde 1 kezden fazla rahatsız etmemek için en az 12 saat geçmiş olması gerekir.
        val currentTime = System.currentTimeMillis()
        val lastSent = prefs.getLastNotificationSentTime()
        if (currentTime - lastSent < 12 * 60 * 60 * 1000) {
            Log.d("NotificationWorker", "Son bildirimden bu yana 12 saat geçmedi. Bildirim iptal edildi.")
            return Result.success()
        }

        // Şartlar sağlandı, bildirimi oluştur ve gönder
        sendStudyReminder()
        prefs.setLastNotificationSentTime(currentTime)

        return Result.success()
    }

    private fun sendStudyReminder() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lingoscroll_study_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Günlük Pratik Hatırlatıcı",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Serinizi kaybetmemeniz için günlük motive edici hatırlatmalar gönderir."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Varsayılan bilgi ikonu
            .setContentTitle("🔥 Günlük Serini Koru!")
            .setContentText("Bugün serini devam ettirmek için yeni kelime kalıplarını keşfet ⏱️")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2026, notification)
        Log.d("NotificationWorker", "Günlük hatırlatıcı bildirimi başarıyla gönderildi.")
    }
}
