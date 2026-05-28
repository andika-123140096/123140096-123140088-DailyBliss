package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.screens.home.JournalScreen
import com.dailybliss.app.presentation.screens.home.JournalUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class JournalScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun journalScreen_displaysList() {
        val moments = listOf(
            Moment(id = 1, title = "Momen 1", content = "Konten 1"),
            Moment(id = 2, title = "Momen 2", content = "Konten 2"),
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                JournalScreen(
                    uiState = JournalUiState.Success(moments),
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onNavigateToCreateMoment = {},
                    onNavigateToMomentDetail = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Momen 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Momen 2").assertIsDisplayed()
    }

    @Test
    fun journalScreen_displaysEmptyState() {
        composeTestRule.setContent {
            DailyBlissTheme {
                JournalScreen(
                    uiState = JournalUiState.Empty("cari apa"),
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onNavigateToCreateMoment = {},
                    onNavigateToMomentDetail = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Tidak Ada Momen").assertIsDisplayed()
    }
}
