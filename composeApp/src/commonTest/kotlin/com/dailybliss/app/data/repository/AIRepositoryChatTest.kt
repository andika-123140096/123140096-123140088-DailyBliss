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

@OptIn(ExperimentalCoroutinesApi::class)
class AIRepositoryChatTest {
    private lateinit var userPreferences: FakeUserPreferences

    @BeforeTest
    fun setup() {
        userPreferences = FakeUserPreferences()
    }

    @Test
    fun `chat method should return direct response from service`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"candidates": [{"content": {"parts": [{"text": "Direct Chat Response"}]}}]}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val repository = AIRepositoryImpl(GeminiService(httpClient, FakeApiConfig()), userPreferences, this)
        
        val response = repository.chat(listOf(ChatMessage(role = "user", text = "Hi")))
        assertEquals("Direct Chat Response", response)
    }
}
