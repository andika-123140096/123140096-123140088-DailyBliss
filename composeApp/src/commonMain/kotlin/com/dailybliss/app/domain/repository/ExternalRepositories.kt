package com.dailybliss.app.domain.repository

interface WeatherRepository {
    suspend fun getCurrentWeather(location: String): String
}

interface CurrencyRepository {
    suspend fun getExchangeRate(base: String, target: String): String
}
