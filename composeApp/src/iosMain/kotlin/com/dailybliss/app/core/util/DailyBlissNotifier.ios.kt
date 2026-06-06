package com.dailybliss.app.core.util

import platform.Foundation.*
import platform.UserNotifications.*

actual class DailyBlissNotifier actual constructor(context: PlatformContext) : Notifier {

    override fun showNotification(title: String, message: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            NSUUID().UUIDString,
            content,
            null,
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing notification: ${error.localizedDescription}")
            }
        }
    }

    override fun scheduleDailyNotification(hour: Int, minute: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Waktunya Jurnal!")
            setBody("Bagaimana harimu? Ceritakan di DailyBliss sekarang.")
            setSound(UNNotificationSound.defaultSound)
        }

        val components = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, true)

        val request = UNNotificationRequest.requestWithIdentifier(
            DAILY_REMINDER_ID,
            content,
            trigger,
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling notification: ${error.localizedDescription}")
            }
        }
    }

    override fun cancelAllNotifications() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
        UNUserNotificationCenter.currentNotificationCenter().removeAllDeliveredNotifications()
    }

    companion object {
        private const val DAILY_REMINDER_ID = "daily_reminder"
    }
}
