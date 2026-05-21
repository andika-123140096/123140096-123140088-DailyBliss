package com.dailybliss.app.data.repository

import com.dailybliss.app.data.local.BlissDatabase
import com.dailybliss.app.data.remote.dto.FrankfurterResponse
import com.dailybliss.app.data.remote.dto.IpResponse
import com.dailybliss.app.data.remote.dto.NewsResponse
import com.dailybliss.app.data.remote.dto.WeatherResponse
import com.dailybliss.app.domain.model.CurrencyRates
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo
import com.dailybliss.app.domain.repository.HomeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HomeRepositoryImpl(
    private val httpClient: HttpClient,
    private val database: BlissDatabase,
) : HomeRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }
    private val momentQueries = database.momentQueries

    override suspend fun getCurrencyRates(): CurrencyRates? = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = try { momentQueries.getCache(KEY_CURRENCY).executeAsOneOrNull() } catch (e: Exception) { null }

        try {
            val usdDeferred = async { 
                httpClient.get("https://api.frankfurter.dev/v1/latest?base=USD&symbols=IDR").body<FrankfurterResponse>()
            }
            val sgdDeferred = async { 
                httpClient.get("https://api.frankfurter.dev/v1/latest?base=SGD&symbols=IDR").body<FrankfurterResponse>()
            }

            val usdResponse = usdDeferred.await()
            val sgdResponse = sgdDeferred.await()

            val rates = CurrencyRates(
                usdToIdr = usdResponse.rates["IDR"] ?: 0.0,
                sgdToIdr = sgdResponse.rates["IDR"] ?: 0.0
            )

            momentQueries.insertCache(KEY_CURRENCY, json.encodeToString(rates), now)
            rates
        } catch (e: Exception) {
            cached?.let {
                try {
                    json.decodeFromString<CurrencyRates>(it.data_)
                } catch (inner: Exception) { null }
            }
        }
    }

    override suspend fun getWeather(): WeatherInfo = withContext(Dispatchers.IO) {
        val cached = try { momentQueries.getCache(KEY_WEATHER).executeAsOneOrNull() } catch (e: Exception) { null }
        val now = Clock.System.now().toEpochMilliseconds()

        try {
            fetchAndCacheWeather(now)
        } catch (e: Exception) {
            cached?.let {
                try {
                    return@withContext json.decodeFromString<WeatherInfo>(it.data_)
                } catch (inner: Exception) { /* ignore */ }
            }
            WeatherInfo("28°C", "5 km/h", "Jakarta")
        }
    }

    private suspend fun fetchAndCacheWeather(timestamp: Long): WeatherInfo {
        var lat = -6.2088
        var lon = 106.8456
        var city = "Jakarta"

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
        } catch (e: Exception) { /* use default */ }

        val weatherResponse: WeatherResponse = httpClient.get(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,wind_speed_10m"
        ).body()

        val weatherInfo = WeatherInfo(
            temperature = "${weatherResponse.current.temperature}°C",
            windSpeed = "${weatherResponse.current.windSpeed} km/h",
            city = city
        )

        momentQueries.insertCache(KEY_WEATHER, json.encodeToString(weatherInfo), timestamp)
        return weatherInfo
    }

    override suspend fun getPrabowoNews(): List<NewsArticle> = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        
        try {
            val response: HttpResponse = httpClient.get("https://berita-indo-api-next.vercel.app/api/cnn-news") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            }
            
            if (!response.status.isSuccess()) throw Exception("API Error")
            
            val rawBody = response.bodyAsText()
            val cleanedBody = rawBody.replace("\u00A0", " ")
            val newsData = json.decodeFromString<NewsResponse>(cleanedBody)

            val filteredNews = newsData.data
                .filter { item ->
                    val title = item.title.lowercase()
                    val snippet = item.contentSnippet?.lowercase() ?: ""
                    title.contains("prabowo") || snippet.contains("prabowo")
                }
                .map {
                    NewsArticle(
                        title = it.title,
                        summary = it.contentSnippet ?: "",
                        imageUrl = it.image?.large ?: it.image?.small ?: "",
                        url = it.link
                    )
                }
            
            if (filteredNews.isNotEmpty()) {
                momentQueries.insertCache(KEY_NEWS_FINAL, json.encodeToString(filteredNews), now)
            }
            
            return@withContext filteredNews
        } catch (e: Exception) {
            momentQueries.getCache(KEY_NEWS_FINAL).executeAsOneOrNull()?.let {
                try {
                    return@withContext json.decodeFromString<List<NewsArticle>>(it.data_)
                } catch (inner: Exception) { /* ignore */ }
            }
            emptyList()
        }
    }

    companion object {
        private const val KEY_WEATHER = "cache_weather_prod"
        private const val KEY_NEWS_FINAL = "cache_news_prod"
        private const val KEY_CURRENCY = "cache_currency_prod"
        private const val CACHE_EXPIRY = 60 * 60 * 1000L // 1 hour
    }
}
