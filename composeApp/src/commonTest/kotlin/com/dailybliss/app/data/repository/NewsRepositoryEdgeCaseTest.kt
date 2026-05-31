package com.dailybliss.app.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.dailybliss.app.core.util.FakeLocationTracker
import com.dailybliss.app.data.local.BlissDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewsRepositoryEdgeCaseTest {
    private lateinit var database: BlissDatabase
    private lateinit var locationTracker: FakeLocationTracker
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BlissDatabase.Schema.create(driver)
        database = BlissDatabase(driver)
        locationTracker = FakeLocationTracker()
    }

    @Test
    fun `getPrabowoNews should clean non-breaking spaces`() = runTest {
        val recentDate = kotlinx.datetime.Clock.System.now().toString()
        val mockEngine = MockEngine { _ ->
            respond(
                // Directly provide the non-breaking space character
                content = """{"data": [{"title": "Prabowo${'\u00A0'}News", "contentSnippet": "Snippet", "link": "url", "isoDate": "$recentDate"}]}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = NewsRepositoryImpl(httpClient, database, locationTracker)
        val news = repository.getPrabowoNews()

        assertEquals(1, news.size)
        // Check if the title is cleaned (should have regular space)
        val title = news[0].title
        assertTrue(title.contains(" "))
        assertFalse(title.contains('\u00A0'))
    }

    private fun assertFalse(actual: Boolean) {
        assertEquals(false, actual)
    }
}
