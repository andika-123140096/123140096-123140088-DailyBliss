package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.presentation.screens.addnote.CreateMomentScreen
import com.dailybliss.app.presentation.screens.addnote.CreateMomentUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class CreateMomentScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun createMomentScreen_handlesUserInteractions() {
        var titleChanged = ""
        var contentChanged = ""
        var saved = false

        composeTestRule.setContent {
            DailyBlissTheme {
                CreateMomentScreen(
                    uiState = CreateMomentUiState(title = "Initial Title", content = "Initial Content"),
                    onTitleChange = { titleChanged = it },
                    onContentChange = { contentChanged = it },
                    onAddImage = {},
                    onSaveMoment = { saved = true },
                    onNavigateBack = {},
                )
            }
        }

        // Check initial state
        composeTestRule.onNodeWithText("Initial Title").assertIsDisplayed()

        // Test title change
        composeTestRule.onNodeWithText("Initial Title").performTextReplacement("New Title")
        assertEquals("New Title", titleChanged)

        // Test content change (assuming it has some placeholder or text)
        // Usually content is in a RichTextEditor or TextField. Let's find it.
        // For now, assume it's findable by text.
        composeTestRule.onNodeWithText("Initial Content").performTextReplacement("New Content")
        assertEquals("New Content", contentChanged)

        // Test save button
        // Need to know the text or test tag of the save button.
        // Looking at common patterns, it might be "Simpan" or a check icon.
        // Let's check CreateMomentScreen.kt to be sure about the UI.
    }
}
