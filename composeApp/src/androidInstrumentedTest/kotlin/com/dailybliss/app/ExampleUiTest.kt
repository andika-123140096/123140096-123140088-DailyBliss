package com.dailybliss.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.dailybliss.app.presentation.components.EmptyState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test

class ExampleUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_isDisplayed() {
        composeTestRule.setContent {
            DailyBlissTheme {
                EmptyState(
                    title = "Tidak Ditemukan",
                    message = "Coba kata kunci lain",
                )
            }
        }

        composeTestRule.onNodeWithText("Tidak Ditemukan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coba kata kunci lain").assertIsDisplayed()
    }

    @Test
    fun momentCard_displaysTitleAndPreview() {
        val now = Clock.System.now()
        val testMoment = com.dailybliss.app.domain.model.Moment(
            id = 1,
            title = "Momen Tes",
            content = "Isi momen tes yang cukup panjang untuk preview",
            createdAt = now,
            updatedAt = now,
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                com.dailybliss.app.presentation.components.MomentCard(
                    moment = testMoment,
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Momen Tes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Isi momen tes yang cukup panjang untuk preview").assertIsDisplayed()
    }

    @Test
    fun weatherCard_displaysCityAndTemp() {
        val testWeather = com.dailybliss.app.domain.model.WeatherInfo(
            temperature = "25°C",
            windSpeed = "10 km/h",
            city = "Bandar Lampung",
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                com.dailybliss.app.presentation.screens.home.WeatherCard(
                    weather = testWeather,
                )
            }
        }

        composeTestRule.onNodeWithText("Bandar Lampung").assertIsDisplayed()
        composeTestRule.onNodeWithText("25°C").assertIsDisplayed()
    }
}
