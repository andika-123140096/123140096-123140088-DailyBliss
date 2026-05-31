package com.dailybliss.app.presentation

import com.dailybliss.app.presentation.screens.news.NewsUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NewsUiStateTest {
    @Test
    fun `NewsUiState should have default values`() {
        val state = NewsUiState()
        assertNull(state.weather)
        assertTrue(state.news.isEmpty())
        assertNull(state.currencyRates)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }
}
