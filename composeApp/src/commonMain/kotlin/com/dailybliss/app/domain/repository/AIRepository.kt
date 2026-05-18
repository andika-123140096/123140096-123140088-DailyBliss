package com.dailybliss.app.domain.repository

import com.dailybliss.app.presentation.screens.ai.ChatMessage
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    /**
     * Streams a chat response from the AI assistant.
     */
    suspend fun streamChat(messages: List<ChatMessage>): Flow<String>

    /**
     * Gets a full chat response from the AI assistant.
     */
    suspend fun chat(messages: List<ChatMessage>): String

    /**
     * Analyzes the mood of a given journal content.
     */
    suspend fun analyzeMood(content: String): MoodResult?

    /**
     * Generates relevant tags for a given journal content.
     */
    suspend fun generateTags(content: String): List<String>

    /**
     * Generates a daily reflective prompt.
     */
    suspend fun generateDailyPrompt(): String?
}

data class MoodResult(
    val mood: String,
    val emoji: String
)
