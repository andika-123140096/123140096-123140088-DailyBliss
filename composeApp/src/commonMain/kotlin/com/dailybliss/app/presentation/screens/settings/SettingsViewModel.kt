package com.dailybliss.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.core.util.Notifier
import com.dailybliss.app.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val nickname: String = "User",
    val aiLanguageStyle: String = "Santai/Kasual",
    val isDarkMode: Boolean = false,
    val journalSummary: String = "",
    val dailyInsight: String = "",
    val ttsVoiceName: String = "Kore",
    val isReminderEnabled: Boolean = false,
    val reminderTime: String = "20:00",
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val backgroundAIProcessor: BackgroundAIProcessor,
    private val notifier: Notifier,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.nickname,
        userPreferences.aiLanguageStyle,
        userPreferences.isDarkMode,
        userPreferences.journalSummary,
        userPreferences.dailyInsight,
        userPreferences.ttsVoiceName,
        userPreferences.isReminderEnabled,
        userPreferences.reminderTime,
    ) { params ->
        SettingsUiState(
            nickname = params[0] as String,
            aiLanguageStyle = params[1] as String,
            isDarkMode = params[2] as Boolean,
            journalSummary = params[3] as String,
            dailyInsight = params[4] as String,
            ttsVoiceName = params[5] as String,
            isReminderEnabled = params[6] as Boolean,
            reminderTime = params[7] as String,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateNickname(name: String) {
        viewModelScope.launch {
            userPreferences.setNickname(name)
            backgroundAIProcessor.updateGlobalSummary()
        }
    }

    fun updateAiStyle(style: String) {
        viewModelScope.launch {
            userPreferences.setAiLanguageStyle(style)
            backgroundAIProcessor.updateGlobalSummary()
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(isDark)
        }
    }

    fun updateTtsVoiceName(voiceName: String) {
        viewModelScope.launch {
            userPreferences.setTtsVoiceName(voiceName)
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setReminderEnabled(enabled)
            if (enabled) {
                val time = uiState.value.reminderTime
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 20
                    val minute = parts[1].toIntOrNull() ?: 0
                    notifier.scheduleDailyNotification(hour, minute)
                }
            } else {
                notifier.cancelAllNotifications()
            }
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            userPreferences.setReminderTime(time)
            if (uiState.value.isReminderEnabled) {
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 20
                    val minute = parts[1].toIntOrNull() ?: 0
                    notifier.scheduleDailyNotification(hour, minute)
                }
            }
        }
    }
}
