package com.dailybliss.app.data.remote.api

import app.cash.turbine.test
import com.dailybliss.app.core.network.FakeApiConfig
import com.dailybliss.app.data.remote.dto.GeminiContent
import com.dailybliss.app.data.remote.dto.GeminiPart
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiServiceTest {
    private val apiConfig = FakeApiConfig()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `generateContent should return text on success`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "candidates": [{
                        "content": {
                            "parts": [{"text": "Success Response"}]
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val service = GeminiService(httpClient, apiConfig)

        val result = service.generateContent(listOf(GeminiPart(text = "Hello")))

        assertTrue(result.isSuccess)
        assertEquals("Success Response", result.getOrNull())
    }

    @Test
    fun `generateChat should return text on success`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "candidates": [{
                        "content": {
                            "parts": [{"text": "Chat Response"}]
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GeminiService(httpClient, apiConfig)

        val result = service.generateChat(listOf(GeminiContent(parts = listOf(GeminiPart(text = "Hi")), role = "user")))

        assertTrue(result.isSuccess)
        assertEquals("Chat Response", result.getOrNull())
    }

    @Test
    fun `generateContent should retry on failure and eventually succeed`() = runTest {
        var attempts = 0
        val mockEngine = MockEngine { request ->
            attempts++
            if (attempts < 3) {
                respond(content = "Error", status = HttpStatusCode.InternalServerError)
            } else {
                respond(
                    content = """{"candidates": [{"content": {"parts": [{"text": "Succeed after retry"}]}}]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GeminiService(httpClient, apiConfig)

        val result = service.generateContent(listOf(GeminiPart(text = "Retry me")))

        assertTrue(result.isSuccess)
        assertEquals("Succeed after retry", result.getOrNull())
        assertEquals(3, attempts)
    }

    @Test
    fun `generateContent should fail after max retries`() = runTest {
        var attempts = 0
        val mockEngine = MockEngine { request ->
            attempts++
            respond(content = "Permanent Error", status = HttpStatusCode.ServiceUnavailable)
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        // Using smaller retry count for test speed if possible, but service has it private.
        // Default is 5.
        val service = GeminiService(httpClient, apiConfig)

        val result = service.generateContent(listOf(GeminiPart(text = "Fail me")))

        assertTrue(result.isFailure)
        assertEquals(5, attempts)
    }

    @Test
    fun `streamContent should emit chunks from SSE`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    data: {"candidates": [{"content": {"parts": [{"text": "Part 1"}]}}]}
                    
                    data: {"candidates": [{"content": {"parts": [{"text": "Part 2"}]}}]}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GeminiService(httpClient, apiConfig)

        service.streamContent(listOf(GeminiContent(parts = listOf(GeminiPart(text = "Stream"))))).test {
            assertEquals("Part 1", awaitItem())
            assertEquals("Part 2", awaitItem())
            awaitComplete()
        }
    }
}
