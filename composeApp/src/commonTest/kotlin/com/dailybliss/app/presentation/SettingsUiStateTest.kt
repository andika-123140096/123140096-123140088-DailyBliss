package com.dailybliss.app.presentation

import com.dailybliss.app.presentation.screens.settings.SettingsUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsUiStateTest {
    @Test
    fun `SettingsUiState should have default values`() {
        val state = SettingsUiState()
        assertEquals("User", state.nickname)
        assertEquals("Santai/Kasual", state.aiLanguageStyle)
        assertEquals(false, state.isDarkMode)
        assertEquals("", state.journalSummary)
        assertEquals("", state.dailyInsight)
    }
}
