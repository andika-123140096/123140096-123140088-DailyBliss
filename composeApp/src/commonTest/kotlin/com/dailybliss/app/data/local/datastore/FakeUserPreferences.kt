package com.dailybliss.app.data.local.datastore

import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferences : UserPreferences {
    override val nickname = MutableStateFlow("User")
    override suspend fun setNickname(name: String) {
        nickname.value = name
    }

    override val aiLanguageStyle = MutableStateFlow("Santai/Kasual")
    override suspend fun setAiLanguageStyle(style: String) {
        aiLanguageStyle.value = style
    }

    override val journalSummary = MutableStateFlow("")
    override suspend fun setJournalSummary(summary: String) {
        journalSummary.value = summary
    }

    override val dailyInsight = MutableStateFlow("")
    override suspend fun setDailyInsight(insight: String) {
        dailyInsight.value = insight
    }

    override val isDarkMode = MutableStateFlow(false)
    override suspend fun setDarkMode(isDark: Boolean) {
        isDarkMode.value = isDark
    }

    override val ttsVoiceName = MutableStateFlow("Aoede")
    override suspend fun setTtsVoiceName(voiceName: String) {
        ttsVoiceName.value = voiceName
    }

    override val isReminderEnabled = MutableStateFlow(false)
    override suspend fun setReminderEnabled(enabled: Boolean) {
        isReminderEnabled.value = enabled
    }

    override val reminderTime = MutableStateFlow("20:00")
    override suspend fun setReminderTime(time: String) {
        reminderTime.value = time
    }
}
