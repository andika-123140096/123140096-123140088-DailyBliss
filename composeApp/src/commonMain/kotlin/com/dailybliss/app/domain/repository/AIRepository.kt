package com.dailybliss.app.domain.repository

import com.dailybliss.app.domain.model.ChatMessage
import com.dailybliss.app.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AIRepository {
    /**
     * Flow of chat messages in the current session.
     */
    val chatMessages: StateFlow<List<ChatMessage>>

    /**
     * Current active session ID. Null if no session or new unsaved session.
     */
    val currentSessionId: StateFlow<Long?>

    /**
     * State of whether the AI is currently processing a message.
     */
    val isChatLoading: StateFlow<Boolean>

    /**
     * Loads all chat sessions from local database.
     */
    fun getAllChatSessions(): Flow<List<ChatSession>>

    /**
     * Loads a specific chat session and its messages.
     */
    fun loadSession(sessionId: Long)

    /**
     * Deletes a chat session and its messages.
     */
    suspend fun deleteSession(sessionId: Long)

    /**
     * Starts a new chat session (clears current messages and ID).
     */
    fun startNewSession()

    /**
     * Sends a message to the AI assistant and updates the chatMessages flow.
     */
    fun sendMessage(text: String, imageBytes: ByteArray? = null)

    /**
     * Clears the current chat session.
     */
    fun clearChat()

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

    /**
     * Generates a global summary of multiple journal entries.
     */
    suspend fun generateGlobalSummary(momentsText: String): String?

    /**
     * Generates a short, punchy insight for the home screen.
     */
    suspend fun generateDailyInsight(momentsText: String): String?
}

data class MoodResult(val mood: String, val emoji: String)
