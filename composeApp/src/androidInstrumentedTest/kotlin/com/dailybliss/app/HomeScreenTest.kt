package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.presentation.screens.home.HomeScreen
import com.dailybliss.app.presentation.screens.home.HomeUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysWelcomeMessage() {
        composeTestRule.setContent {
            DailyBlissTheme {
                HomeScreen(
                    uiState = HomeUiState(nickname = "Andika"),
                    onNavigateToJournal = {},
                    onNavigateToNews = {},
                    onNavigateToCalendar = {},
                    onNavigateToAIAssistant = {},
                    onNavigateToSettings = {},
                    onNavigateToCreateMoment = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Halo, Andika!").assertIsDisplayed()
    }
}
