package com.dailybliss.app.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.dailybliss.app.core.util.FakeLocationTracker
import com.dailybliss.app.data.local.BlissDatabase
import com.dailybliss.app.domain.model.WeatherInfo
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
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewsRepositoryCachingTest {
    private lateinit var database: BlissDatabase
    private lateinit var locationTracker: FakeLocationTracker
    private val json = Json { ignoreUnknownKeys = true }

    private val keyWeather = "cache_weather_v2"

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BlissDatabase.Schema.create(driver)
        database = BlissDatabase(driver)
        locationTracker = FakeLocationTracker()
    }

    private fun createClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun `getWeather should return cached data if not expired`() = runTest {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedWeather = WeatherInfo("30°C", "2 km/h", "Cached City")
        database.momentQueries.insertCache(keyWeather, json.encodeToString(cachedWeather), now)

        val mockEngine = MockEngine { _ ->
            respond(content = "Should not be called", status = HttpStatusCode.BadRequest)
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val result = repository.getWeather()

        assertEquals("30°C", result.temperature)
        assertEquals("Cached City", result.city)
    }

    @Test
    fun `getWeather should fetch new data if cache expired`() = runTest {
        val longAgo = Clock.System.now().toEpochMilliseconds() - (2 * 3600000L) // 2 hours ago
        val cachedWeather = WeatherInfo("10°C", "1 km/h", "Old City")
        database.momentQueries.insertCache(keyWeather, json.encodeToString(cachedWeather), longAgo)

        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"current": {"temperature_2m": 25.0, "wind_speed_10m": 10.0}}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val result = repository.getWeather()

        assertEquals("25.0°C", result.temperature)
        // Verify cache was updated
        val newCache = database.momentQueries.getCache(keyWeather).executeAsOneOrNull()
        assertTrue(newCache != null)
        assertTrue(newCache.updated_at > longAgo)
    }
}
