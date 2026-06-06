package com.dailybliss.app.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

actual class DailyBlissNotifier actual constructor(context: PlatformContext) : Notifier {
    private val androidContext = (context as AndroidPlatformContext).androidContext
    private val notificationManager = androidContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Reminders to write your daily journal"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(androidContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon later
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun scheduleDailyNotification(hour: Int, minute: Int) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(currentDate)) {
                add(Calendar.HOUR_OF_DAY, 24)
            }
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(androidContext).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest,
        )
    }

    override fun cancelAllNotifications() {
        WorkManager.getInstance(androidContext).cancelAllWorkByTag(WORK_TAG)
        notificationManager.cancelAll()
    }

    companion object {
        private const val CHANNEL_ID = "daily_bliss_reminders"
        private const val WORK_TAG = "daily_reminder_work"
        private const val WORK_NAME = "daily_reminder_periodic"
    }
}

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val notifier = DailyBlissNotifier(AndroidPlatformContext(applicationContext))
        notifier.showNotification(
            "Waktunya Jurnal!",
            "Bagaimana harimu? Ceritakan di DailyBliss sekarang.",
        )
        return Result.success()
    }
}
