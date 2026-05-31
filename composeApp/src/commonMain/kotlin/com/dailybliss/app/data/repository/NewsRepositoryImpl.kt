package com.dailybliss.app.data.repository

import com.dailybliss.app.core.util.LocationTracker
import com.dailybliss.app.data.local.BlissDatabase
import com.dailybliss.app.data.remote.dto.FrankfurterResponse
import com.dailybliss.app.data.remote.dto.IpResponse
import com.dailybliss.app.data.remote.dto.NewsResponse
import com.dailybliss.app.data.remote.dto.WeatherResponse
import com.dailybliss.app.domain.model.CurrencyRates
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo
import com.dailybliss.app.domain.repository.NewsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NewsRepositoryImpl(
    private val httpClient: HttpClient,
    private val database: BlissDatabase,
    private val locationTracker: LocationTracker,
) : NewsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val momentQueries = database.momentQueries

    override suspend fun getCurrencyRates(): CurrencyRates? = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = try {
            momentQueries.getCache(KEY_CURRENCY).executeAsOneOrNull()
        } catch (e: Exception) {
            null
        }

        if (cached != null && (now - cached.updated_at) < TTL_CURRENCY) {
            try {
                return@withContext json.decodeFromString<CurrencyRates>(cached.data_)
            } catch (e: Exception) {
            }
        }

        try {
            val usdText = httpClient.get("https://api.frankfurter.dev/v1/latest?base=USD&symbols=IDR").bodyAsText()
            val usdResponse = json.decodeFromString<FrankfurterResponse>(usdText)
            val sgdText = httpClient.get("https://api.frankfurter.dev/v1/latest?base=SGD&symbols=IDR").bodyAsText()
            val sgdResponse = json.decodeFromString<FrankfurterResponse>(sgdText)

            val rates = CurrencyRates(
                usdToIdr = usdResponse.rates["IDR"] ?: 0.0,
                sgdToIdr = sgdResponse.rates["IDR"] ?: 0.0,
            )
            momentQueries.insertCache(KEY_CURRENCY, json.encodeToString(rates), now)
            rates
        } catch (e: Exception) {
            e.printStackTrace()
            cached?.let {
                try {
                    json.decodeFromString<CurrencyRates>(it.data_)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun getWeather(): WeatherInfo = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = try {
            momentQueries.getCache(KEY_WEATHER).executeAsOneOrNull()
        } catch (_: Exception) {
            null
        }

        if (cached != null && (now - cached.updated_at) < TTL_WEATHER) {
            try {
                return@withContext json.decodeFromString<WeatherInfo>(cached.data_)
            } catch (_: Exception) { /* ignore and fetch */ }
        }

        try {
            fetchAndCacheWeather(now)
        } catch (e: Exception) {
            cached?.let {
                try {
                    return@withContext json.decodeFromString<WeatherInfo>(it.data_)
                } catch (_: Exception) { /* ignore */ }
            }
            WeatherInfo("28°C", "5 km/h", "Jakarta")
        }
    }

    private suspend fun fetchAndCacheWeather(timestamp: Long): WeatherInfo {
        var lat = -6.2088
        var lon = 106.8456
        var city = "Jakarta"

        // Coba ambil lokasi GPS dulu
        val actualLocation = try {
            locationTracker.getCurrentLocation()
        } catch (_: Exception) {
            null
        }

        if (actualLocation != null) {
            lat = actualLocation.latitude
            lon = actualLocation.longitude
            city = "Lokasi Saat Ini"
        } else {
            // Fallback ke IP jika GPS tidak tersedia/diizinkan
            try {
                val response: HttpResponse = httpClient.get("https://ipapi.co/json/") {
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                }
                if (response.status.isSuccess()) {
                    val data = json.decodeFromString<IpResponse>(response.bodyAsText())
                    lat = data.latitude
                    lon = data.longitude
                    city = data.city
                }
            } catch (_: Exception) { /* use default */ }
        }

        val weatherResponse: WeatherResponse = httpClient.get(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,wind_speed_10m",
        ).body()

        val weatherInfo = WeatherInfo(
            temperature = "${weatherResponse.current.temperature}°C",
            windSpeed = "${weatherResponse.current.windSpeed} km/h",
            city = city,
        )

        momentQueries.insertCache(KEY_WEATHER, json.encodeToString(weatherInfo), timestamp)
        return weatherInfo
    }

    override suspend fun getPrabowoNews(): List<NewsArticle> = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = try {
            momentQueries.getCache(KEY_NEWS).executeAsOneOrNull()
        } catch (e: Exception) {
            null
        }

        if (cached != null && (now - cached.updated_at) < TTL_NEWS) {
            try {
                return@withContext json.decodeFromString<List<NewsArticle>>(cached.data_)
            } catch (e: Exception) {
                println("NewsRepository: Failed to decode cached news: ${e.message}")
            }
        }

        try {
            val response: HttpResponse = httpClient.get("https://berita-indo-api-next.vercel.app/api/cnn-news") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            }

            if (!response.status.isSuccess()) {
                throw Exception("API Error: ${response.status}")
            }

            val rawBody = response.bodyAsText()
            val cleanedBody = rawBody.replace("\u00A0", " ")
            val newsData = json.decodeFromString<NewsResponse>(cleanedBody)

            val newFetchedNews = newsData.data
                .mapNotNull { item ->
                    val title = item.title.lowercase()
                    val snippet = item.contentSnippet?.lowercase() ?: ""
                    val isPrabowo = title.contains("prabowo") || snippet.contains("prabowo")

                    val publishedAt = item.isoDate?.let {
                        try {
                            Instant.parse(it).toEpochMilliseconds()
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (isPrabowo && publishedAt != null && (now - publishedAt) <= 7 * 24 * 60 * 60 * 1000L) {
                        NewsArticle(
                            title = item.title,
                            summary = item.contentSnippet ?: "",
                            imageUrl = item.image?.large ?: item.image?.small ?: "",
                            url = item.link,
                            publishedAt = publishedAt,
                        )
                    } else {
                        null
                    }
                }

            val oldNews = cached?.data_?.let { oldData ->
                try {
                    json.decodeFromString<List<NewsArticle>>(oldData)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()

            // Merge and keep only news from the last 7 days
            val mergedNews = (newFetchedNews + oldNews)
                .distinctBy { it.url }
                .filter { it.publishedAt > 0L && (now - it.publishedAt) <= 7 * 24 * 60 * 60 * 1000L }
                .sortedByDescending { it.publishedAt }

            momentQueries.insertCache(KEY_NEWS, json.encodeToString(mergedNews), now)
            return@withContext mergedNews
        } catch (e: Exception) {
            e.printStackTrace()
            cached?.let {
                try {
                    return@withContext json.decodeFromString<List<NewsArticle>>(it.data_)
                } catch (_: Exception) { /* ignore */ }
            }
            emptyList()
        }
    }

    companion object {
        private const val KEY_WEATHER = "cache_weather_v2"
        private const val KEY_NEWS = "cache_news_v2"
        private const val KEY_CURRENCY = "cache_currency_v2"

        private const val TTL_WEATHER = 3600000L // 1 Jam
        private const val TTL_NEWS = 14400000L // 4 Jam
        private const val TTL_CURRENCY = 43200000L // 12 Jam
    }
}
