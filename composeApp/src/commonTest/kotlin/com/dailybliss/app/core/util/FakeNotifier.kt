package com.dailybliss.app.core.util

class FakeNotifier : Notifier {
    var lastTitle: String? = null
    var lastMessage: String? = null
    var lastHour: Int? = null
    var lastMinute: Int? = null
    var cancelCalled = false

    override fun showNotification(title: String, message: String) {
        lastTitle = title
        lastMessage = message
    }

    override fun scheduleDailyNotification(hour: Int, minute: Int) {
        lastHour = hour
        lastMinute = minute
    }

    override fun cancelAllNotifications() {
        cancelCalled = true
    }
}
