package com.dailybliss.app.data.remote.api

import com.dailybliss.app.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class WeatherService(private val httpClient: HttpClient) {
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return httpClient.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", lat)
            parameter("longitude", lon)
            parameter("current", "temperature_2m,wind_speed_10m")
        }.body()
    }
}

class CurrencyService(private val httpClient: HttpClient) {
    suspend fun getLatestRates(base: String, target: String): Map<String, Double> {
        val response: FrankfurterResponse = httpClient.get("https://api.frankfurter.app/latest") {
            parameter("from", base)
            parameter("to", target)
        }.body()
        return response.rates
    }
}
