package com.dailybliss.app.data.remote.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiDtoTest {

    @Test
    fun `getTextContent should extract text from candidates`() {
        val response = GeminiResponse(
            candidates = listOf(
                GeminiCandidate(
                    content = GeminiContent(
                        parts = listOf(GeminiPart(text = "Hello World")),
                        role = "model",
                    ),
                ),
            ),
        )
        assertEquals("Hello World", response.getTextContent())
    }

    @Test
    fun `isBlocked should return true if blockReason exists`() {
        val response = GeminiResponse(
            promptFeedback = PromptFeedback(blockReason = "SAFETY"),
        )
        assertTrue(response.isBlocked())
    }

    @Test
    fun `getErrorMessage should return error message or block reason`() {
        val errResponse = GeminiResponse(error = GeminiError(400, "Bad Request", "INVALID"))
        assertEquals("Bad Request", errResponse.getErrorMessage())

        val blockedResponse = GeminiResponse(promptFeedback = PromptFeedback(blockReason = "SAFETY"))
        assertTrue(blockedResponse.getErrorMessage()?.contains("SAFETY") == true)
    }
}
