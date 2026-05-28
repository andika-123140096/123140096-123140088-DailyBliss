package com.dailybliss.app.data.repository

import com.dailybliss.app.domain.model.ChatMessage
import com.dailybliss.app.domain.model.ChatSession
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MoodResult
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

class FakeAIRepository : AIRepository {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    override val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    override val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    private val sessionMessages = mutableMapOf<Long, List<ChatMessage>>()

    var mockChatResponse: String = "Mock AI Response"
    var mockMoodResult: MoodResult? = MoodResult("Bahagia", "😊")
    var mockTags: List<String> = listOf("Hobi", "Santai")
    var mockGlobalSummary: String? = "Mock Summary"
    var mockDailyInsight: String? = "Mock Insight"

    var slowMode: Boolean = false
    var analyzeCallCount: Int = 0
    var shouldThrowError = false
    var lastSentText: String? = null

    private suspend fun delayIfSlow() {
        if (slowMode) {
            kotlinx.coroutines.delay(1000)
        }
    }

    override fun getAllChatSessions(): Flow<List<ChatSession>> = _sessions.asStateFlow()

    override fun loadSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        _chatMessages.value = sessionMessages[sessionId] ?: emptyList()
    }

    override suspend fun deleteSession(sessionId: Long) {
        _sessions.update { it.filter { s -> s.id != sessionId } }
        sessionMessages.remove(sessionId)
        if (_currentSessionId.value == sessionId) {
            startNewSession()
        }
    }

    override fun startNewSession() {
        _currentSessionId.value = null
        _chatMessages.value = emptyList()
    }

    override fun sendMessage(text: String, imageBytes: ByteArray?) {
        lastSentText = text
        if (shouldThrowError) return

        if (_currentSessionId.value == null) {
            val id = (_sessions.value.maxOfOrNull { it.id } ?: 0L) + 1
            val now = Clock.System.now().toEpochMilliseconds()
            val newSession = ChatSession(id, text.take(10), now, now)
            _sessions.update { it + newSession }
            _currentSessionId.value = id
        }

        val userMsg = ChatMessage(role = "user", text = text, imageBytes = imageBytes)
        _chatMessages.update { it + userMsg }

        // Instant response for testing
        val modelMsg = ChatMessage(role = "model", text = mockChatResponse)
        _chatMessages.update { it + modelMsg }

        _currentSessionId.value?.let { id ->
            sessionMessages[id] = _chatMessages.value
        }
    }

    override fun clearChat() {
        startNewSession()
    }

    override suspend fun chat(messages: List<ChatMessage>): String {
        delayIfSlow()
        return mockChatResponse
    }

    override suspend fun analyzeMood(content: String, imageBytes: ByteArray?): MoodResult? {
        analyzeCallCount++
        delayIfSlow()
        return mockMoodResult
    }

    override suspend fun generateTags(content: String, imageBytes: ByteArray?): List<String> {
        delayIfSlow()
        return mockTags
    }

    override suspend fun generateGlobalSummary(momentsText: String): String? {
        delayIfSlow()
        return mockGlobalSummary
    }

    override suspend fun generateDailyInsight(momentsText: String): String? {
        delayIfSlow()
        return mockDailyInsight
    }

    // Helper for testing
    fun addFakeSession(session: ChatSession, messages: List<ChatMessage> = emptyList()) {
        _sessions.update { it + session }
        sessionMessages[session.id] = messages
    }
}
