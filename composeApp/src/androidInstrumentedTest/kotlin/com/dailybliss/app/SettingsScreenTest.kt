package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.presentation.screens.settings.SettingsScreenContent
import com.dailybliss.app.presentation.screens.settings.SettingsUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysUserInfo() {
        composeTestRule.setContent {
            DailyBlissTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState(
                        nickname = "Andika",
                        journalSummary = "Summary text",
                        dailyInsight = "Insight text",
                        isDarkMode = false,
                        aiLanguageStyle = "Empathetic"
                    ),
                    nickname = "Andika",
                    onNicknameChange = {},
                    onAiStyleChange = {},
                    onDarkModeToggle = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Andika").assertIsDisplayed()
        composeTestRule.onNodeWithText("Summary text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Insight text").assertIsDisplayed()
    }
}
