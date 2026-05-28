package com.dailybliss.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val nickname: String = "User",
    val aiLanguageStyle: String = "Santai/Kasual",
    val isDarkMode: Boolean = false,
    val journalSummary: String = "",
    val dailyInsight: String = "",
)

class SettingsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.nickname,
        userPreferences.aiLanguageStyle,
        userPreferences.isDarkMode,
        userPreferences.journalSummary,
        userPreferences.dailyInsight,
    ) { nickname, aiStyle, isDark, summary, insight ->
        SettingsUiState(nickname, aiStyle, isDark, summary, insight)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateNickname(name: String) {
        viewModelScope.launch {
            userPreferences.setNickname(name)
        }
    }

    fun updateAiStyle(style: String) {
        viewModelScope.launch {
            userPreferences.setAiLanguageStyle(style)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(isDark)
        }
    }
}
