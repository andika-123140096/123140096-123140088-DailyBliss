package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.domain.model.ChatMessage
import com.dailybliss.app.presentation.screens.ai.AIAssistantScreen
import com.dailybliss.app.presentation.screens.ai.AIAssistantUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class AIAssistantScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aiAssistantScreen_displaysMessages() {
        val messages = listOf(
            ChatMessage(role = "user", text = "Halo AI"),
            ChatMessage(role = "model", text = "Halo User!"),
        )

        composeTestRule.setContent {
            DailyBlissTheme {
                AIAssistantScreen(
                    uiState = AIAssistantUiState(messages = messages),
                    onInputChange = {},
                    onSendMessage = {},
                    onImageSelected = {},
                    onRemoveSelectedImage = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Halo AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Halo User!").assertIsDisplayed()
    }

    @Test
    fun aiAssistantScreen_canInputText() {
        var inputText = ""
        composeTestRule.setContent {
            DailyBlissTheme {
                AIAssistantScreen(
                    uiState = AIAssistantUiState(input = "test input"),
                    onInputChange = { inputText = it },
                    onSendMessage = {},
                    onImageSelected = {},
                    onRemoveSelectedImage = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("AI_INPUT_FIELD").assertTextContains("test input")
    }
}
