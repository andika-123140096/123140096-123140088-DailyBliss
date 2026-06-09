package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.screens.home.JournalScreenContent
import com.dailybliss.app.presentation.screens.home.JournalUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class JournalScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun journalScreen_displaysMoments() {
        val moments = listOf(
            Moment(id = 1, title = "Momen Liburan", content = "Senang sekali di Bali"),
            Moment(id = 2, title = "Momen Kerja", content = "Deadline proyek selesai"),
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                JournalScreenContent(
                    uiState = JournalUiState.Success(moments = moments, isLastPage = true),
                    query = "",
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onLoadMore = {},
                    onNavigateToCreateMoment = {},
                    onNavigateToMomentDetail = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Momen Liburan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Momen Kerja").assertIsDisplayed()
    }

    @Test
    fun journalScreen_displaysEmptyState() {
        composeTestRule.setContent {
            DailyBlissTheme {
                JournalScreenContent(
                    uiState = JournalUiState.Empty,
                    query = "",
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onLoadMore = {},
                    onNavigateToCreateMoment = {},
                    onNavigateToMomentDetail = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("EMPTY_STATE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mulai Menulis").assertIsDisplayed()
    }

    @Test
    fun journalScreen_searchBarExists() {
        composeTestRule.setContent {
            DailyBlissTheme {
                JournalScreenContent(
                    uiState = JournalUiState.Empty,
                    query = "Test",
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onLoadMore = {},
                    onNavigateToCreateMoment = {},
                    onNavigateToMomentDetail = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("SEARCH_TEXT_FIELD").assertIsDisplayed()
        composeTestRule.onNodeWithTag("CLEAR_SEARCH_BUTTON").assertIsDisplayed()
    }
}
