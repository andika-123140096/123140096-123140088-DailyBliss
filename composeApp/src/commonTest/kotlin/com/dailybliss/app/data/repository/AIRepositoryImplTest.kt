package com.dailybliss.app.data.repository

import app.cash.turbine.test
import com.dailybliss.app.core.network.FakeApiConfig
import com.dailybliss.app.data.local.datastore.FakeUserPreferences
import com.dailybliss.app.data.remote.api.GeminiService
import com.dailybliss.app.domain.model.ChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AIRepositoryImplTest {
    private lateinit var userPreferences: FakeUserPreferences
    private val successJson = """{"candidates": [{"content": {"parts": [{"text": "AI Response"}]}}]}"""

    @BeforeTest
    fun setup() {
        userPreferences = FakeUserPreferences()
    }

    private fun createClient(content: String): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = content,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `sendMessage should update chatMessages flow with response`() = runTest {
        val geminiService = GeminiService(createClient(successJson), FakeApiConfig())
        val repository = AIRepositoryImpl(geminiService, userPreferences, this)
        
        repository.chatMessages.test {
            // Initial empty
            assertEquals(0, awaitItem().size)
            
            repository.sendMessage("User message")
            
            // Initial user message + placeholder
            val midState = awaitItem()
            assertEquals(2, midState.size)
            assertEquals("User message", midState[0].text)
            assertEquals("", midState[1].text)
            
            // Wait for response update
            advanceUntilIdle()
            
            val finalState = awaitItem()
            assertEquals(2, finalState.size)
            assertEquals("AI Response", finalState[1].text)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearChat should empty messages`() = runTest {
        val geminiService = GeminiService(createClient(successJson), FakeApiConfig())
        val repository = AIRepositoryImpl(geminiService, userPreferences, this)
        
        repository.sendMessage("Hi", null)
        advanceUntilIdle() 
        
        repository.clearChat()
        // clearChat should emit a new state
        assertEquals(0, repository.chatMessages.value.size)
    }

    @Test
    fun `analyzeMood should parse JSON correctly`() = runTest {
        val json = """{"candidates": [{"content": {"parts": [{"text": "{\"mood\": \"Senang\", \"emoji\": \"😊\"}"}]}}]}"""
        val geminiService = GeminiService(createClient(json), FakeApiConfig())
        val repository = AIRepositoryImpl(geminiService, userPreferences, this)
        
        val result = repository.analyzeMood("Content", null)
        
        assertEquals("Senang", result?.mood)
        assertEquals("😊", result?.emoji)
    }
}
