package com.dailybliss.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val nickname: String = "User",
    val aiLanguageStyle: String = "Santai/Kasual",
    val themeName: String = "Sage Green",
    val journalSummary: String = "",
)

class SettingsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.nickname,
        userPreferences.aiLanguageStyle,
        userPreferences.colorTheme,
        userPreferences.journalSummary,
    ) { nickname, aiStyle, theme, summary ->
        SettingsUiState(nickname, aiStyle, theme, summary)
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

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            userPreferences.setColorTheme(theme)
        }
    }

    val languageStyles = listOf("Santai/Kasual", "Formal/Baku", "Puitis/Puitik")

    val themes = listOf("Sage Green", "Ocean Blue", "Rose Pink", "Lavender", "Monochrome")
}
