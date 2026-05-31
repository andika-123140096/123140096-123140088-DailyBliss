package com.dailybliss.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
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
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val backgroundAIProcessor: BackgroundAIProcessor,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.nickname,
        userPreferences.aiLanguageStyle,
        userPreferences.isDarkMode,
        userPreferences.journalSummary,
        userPreferences.dailyInsight,
        userPreferences.ttsVoiceName,
    ) { params ->
        SettingsUiState(
            nickname = params[0] as String,
            aiLanguageStyle = params[1] as String,
            isDarkMode = params[2] as Boolean,
            journalSummary = params[3] as String,
            dailyInsight = params[4] as String,
            ttsVoiceName = params[5] as String,
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
}
