package com.dailybliss.app.data.repository

import com.dailybliss.app.domain.model.ChatMessage
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MoodResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeAIRepository : AIRepository {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    override val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

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

    override fun sendMessage(text: String, imageBytes: ByteArray?) {
        lastSentText = text
        if (shouldThrowError) return

        val userMsg = ChatMessage(role = "user", text = text, imageBytes = imageBytes)
        _chatMessages.update { it + userMsg }
        
        // Instant response for testing
        val modelMsg = ChatMessage(role = "model", text = mockChatResponse)
        _chatMessages.update { it + modelMsg }
    }

    override fun clearChat() {
        _chatMessages.value = emptyList()
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
}
