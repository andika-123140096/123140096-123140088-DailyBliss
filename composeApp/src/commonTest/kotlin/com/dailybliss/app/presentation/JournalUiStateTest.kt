package com.dailybliss.app.presentation

import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.screens.home.JournalUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class JournalUiStateTest {
    @Test
    fun `JournalUiState Success should hold moments and query`() {
        val moments = listOf(Moment(title = "T", content = "C"))
        val state = JournalUiState.Success(moments, "query")

        assertEquals(moments, state.moments)
        assertEquals("query", state.query)
    }

    @Test
    fun `JournalUiState Empty should hold query`() {
        val state = JournalUiState.Empty("none")
        assertEquals("none", state.query)
    }

    @Test
    fun `JournalUiState Error should hold message`() {
        val state = JournalUiState.Error("fail")
        assertEquals("fail", state.message)
    }
}
