package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.presentation.screens.news.NewsScreenContent
import com.dailybliss.app.presentation.screens.news.NewsUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class NewsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun newsScreen_displaysNewsArticles() {
        val news = listOf(
            NewsArticle(title = "Berita 1", summary = "S 1", imageUrl = "", url = "u1"),
            NewsArticle(title = "Berita 2", summary = "S 2", imageUrl = "", url = "u2"),
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                NewsScreenContent(
                    uiState = NewsUiState(news = news),
                    onRefresh = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Berita 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Berita 2").assertIsDisplayed()
    }
}
