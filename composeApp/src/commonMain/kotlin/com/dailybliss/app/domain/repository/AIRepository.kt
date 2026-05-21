package com.dailybliss.app.domain.repository

import com.dailybliss.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AIRepository {
    /**
     * Flow of chat messages in the current session.
     */
    val chatMessages: StateFlow<List<ChatMessage>>

    /**
     * State of whether the AI is currently processing a message.
     */
    val isChatLoading: StateFlow<Boolean>

    /**
     * Sends a message to the AI assistant and updates the chatMessages flow.
     */
    fun sendMessage(text: String, imageBytes: ByteArray? = null)

    /**
     * Clears the current chat session.
     */
    fun clearChat()

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
    suspend fun analyzeMood(content: String, imageBytes: ByteArray? = null): MoodResult?

    /**
     * Generates relevant tags for a given journal content.
     */
    suspend fun generateTags(content: String, imageBytes: ByteArray? = null): List<String>
}

data class MoodResult(val mood: String, val emoji: String)
