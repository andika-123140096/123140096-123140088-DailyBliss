package com.dailybliss.app.presentation

import com.dailybliss.app.presentation.screens.ai.AIAssistantUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AIAssistantUiStateTest {
    @Test
    fun `AIAssistantUiState should have default values`() {
        val state = AIAssistantUiState()
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.input)
        assertNull(state.selectedImageBytes)
        assertEquals(false, state.isLoading)
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }
}
