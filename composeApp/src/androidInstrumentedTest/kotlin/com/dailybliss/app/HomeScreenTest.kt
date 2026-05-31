package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.screens.home.HomeScreenContent
import com.dailybliss.app.presentation.screens.home.HomeUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysInfo() {
        val uiState = HomeUiState(
            nickname = "Andika",
            recentMoments = listOf(Moment(id = 1, title = "Momen Home", content = "Konten Home")),
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                HomeScreenContent(
                    uiState = uiState,
                    onNavigateToSettings = {},
                    onNavigateToMomentDetail = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Halo, Andika!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Momen Home").assertIsDisplayed()
    }
}
