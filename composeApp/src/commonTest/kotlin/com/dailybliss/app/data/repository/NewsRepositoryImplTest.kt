package com.dailybliss.app.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.dailybliss.app.core.util.FakeLocationTracker
import com.dailybliss.app.core.util.Location
import com.dailybliss.app.data.local.BlissDatabase
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NewsRepositoryImplTest {
    private lateinit var database: BlissDatabase
    private lateinit var locationTracker: FakeLocationTracker

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BlissDatabase.Schema.create(driver)
        database = BlissDatabase(driver)
        locationTracker = FakeLocationTracker()
    }

    private fun createClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                },
            )
        }
    }

    @Test
    fun `getCurrencyRates should fetch and return rates`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"amount": 1.0, "base": "USD", "date": "2024-05-26", "rates": {"IDR": 15000.0}}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val rates = repository.getCurrencyRates()

        assertNotNull(rates)
        assertEquals(15000.0, rates.usdToIdr)
    }

    @Test
    fun `getCurrencyRates should return null on error`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
            )
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val rates = repository.getCurrencyRates()

        assertTrue(rates == null)
    }

    @Test
    fun `getWeather should use GPS location if available`() = runTest {
        locationTracker.mockLocation = Location(1.23, 4.56)
        val mockEngine = MockEngine { request ->
            if (request.url.toString().contains("latitude=1.23")) {
                respond(
                    content = """{"current": {"temperature_2m": 25.0, "wind_speed_10m": 10.0}}""",
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            } else {
                respond("{}", status = HttpStatusCode.NotFound)
            }
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val weather = repository.getWeather()

        assertEquals("25.0°C", weather.temperature)
        assertEquals("Lokasi Saat Ini", weather.city)
    }

    @Test
    fun `getWeather should fallback to ip location if GPS not available`() = runTest {
        locationTracker.mockLocation = null
        val mockEngine = MockEngine { request ->
            when {
                request.url.toString().contains("ipapi.co") -> {
                    respond(
                        content = """{"latitude": -6.0, "longitude": 106.0, "city": "Ip City"}""",
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
                request.url.toString().contains("latitude=-6.0") -> {
                    respond(
                        content = """{"current": {"temperature_2m": 22.0, "wind_speed_10m": 5.0}}""",
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
                else -> respond("{}")
            }
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val weather = repository.getWeather()

        assertEquals("22.0°C", weather.temperature)
        assertEquals("Ip City", weather.city)
    }

    @Test
    fun `getPrabowoNews should fetch and filter news`() = runTest {
        val recentDate = kotlinx.datetime.Clock.System.now().toString()
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "data": [
                        {"title": "Prabowo Subianto News", "contentSnippet": "Snippet", "link": "url1", "isoDate": "$recentDate"},
                        {"title": "Random News", "contentSnippet": "No mention", "link": "url2", "isoDate": "$recentDate"}
                    ]
                }""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val news = repository.getPrabowoNews()

        assertEquals(1, news.size)
        assertEquals("Prabowo Subianto News", news[0].title)
    }

    @Test
    fun `getCurrencyRates should use cache if not expired`() = runTest {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val cachedData = """{"usdToIdr": 15000.0, "sgdToIdr": 11000.0}"""
        database.momentQueries.insertCache("cache_currency_v2", cachedData, now)

        val mockEngine = MockEngine { respond("{}", HttpStatusCode.InternalServerError) }
        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)

        val rates = repository.getCurrencyRates()
        assertNotNull(rates)
        assertEquals(15000.0, rates.usdToIdr)
    }

    @Test
    fun `getCurrencyRates should fetch if cache is expired`() = runTest {
        val old = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - 13 * 3600000L // 13h old (TTL 12h)
        val cachedData = """{"usdToIdr": 15000.0, "sgdToIdr": 11000.0}"""
        database.momentQueries.insertCache("cache_currency_v2", cachedData, old)

        val mockEngine = MockEngine { request ->
            respond(
                content = """{"amount": 1.0, "base": "USD", "date": "2024-05-26", "rates": {"IDR": 16000.0}}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val rates = repository.getCurrencyRates()

        assertNotNull(rates)
        assertEquals(16000.0, rates.usdToIdr)
    }

    @Test
    fun `getWeather should fallback to cache on network error`() = runTest {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val cachedData = """{"temperature": "30°C", "windSpeed": "10 km/h", "city": "Cached City"}"""
        database.momentQueries.insertCache("cache_weather_v2", cachedData, now - 5000)

        val mockEngine = MockEngine { respond("Error", HttpStatusCode.InternalServerError) }
        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)

        val weather = repository.getWeather()
        assertEquals("30°C", weather.temperature)
        assertEquals("Cached City", weather.city)
    }

    @Test
    fun `getPrabowoNews should cache result on success`() = runTest {
        val recentDate = kotlinx.datetime.Clock.System.now().toString()
        val mockEngine = MockEngine { request ->
            respond(
                content = """{ "data": [{"title": "Prabowo Subianto News", "link": "url1", "isoDate": "$recentDate"}] }""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        repository.getPrabowoNews()

        val cached = database.momentQueries.getCache("cache_news_v2").executeAsOneOrNull()
        assertNotNull(cached)
        assertTrue(cached.data_.contains("Prabowo Subianto News"))
    }

    @Test
    fun `getPrabowoNews should return empty list on error and no cache`() = runTest {
        val mockEngine = MockEngine { respond("Error", HttpStatusCode.BadRequest) }
        val repository = NewsRepositoryImpl(createClient(mockEngine), database, locationTracker)
        val news = repository.getPrabowoNews()
        assertTrue(news.isEmpty())
    }
}
