package com.dailybliss.app.core.util

interface Notifier {
    fun showNotification(title: String, message: String)
    fun scheduleDailyNotification(hour: Int, minute: Int)
    fun cancelAllNotifications()
}

expect class DailyBlissNotifier(context: PlatformContext) : Notifier
