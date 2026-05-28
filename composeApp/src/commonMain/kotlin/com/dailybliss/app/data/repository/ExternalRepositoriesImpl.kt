package com.dailybliss.app.data.repository

import com.dailybliss.app.data.remote.api.CurrencyService
import com.dailybliss.app.data.remote.api.WeatherService
import com.dailybliss.app.domain.repository.CurrencyRepository
import com.dailybliss.app.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherService: WeatherService
) : WeatherRepository {
    override suspend fun getCurrentWeather(location: String): String {
        val (lat, lon) = when (location.lowercase()) {
            "jakarta" -> -6.2088 to 106.8456
            "surabaya" -> -7.2575 to 112.7521
            "bandung" -> -6.9175 to 107.6191
            "tokyo" -> 35.6762 to 139.6503
            "london" -> 51.5074 to -0.1278
            "new york" -> 40.7128 to -74.0060
            else -> -6.2088 to 106.8456 // Default Jakarta
        }

        return try {
            val response = weatherService.getWeather(lat, lon)
            "Cuaca di $location: ${response.current.temperature}°C, Kecepatan angin: ${response.current.windSpeed} km/h."
        } catch (e: Exception) {
            "Gagal ambil data cuaca: ${e.message}"
        }
    }
}

class CurrencyRepositoryImpl(
    private val currencyService: CurrencyService
) : CurrencyRepository {
    override suspend fun getExchangeRate(base: String, target: String): String {
        return try {
            val rates = currencyService.getLatestRates(base.uppercase(), target.uppercase())
            val rate = rates[target.uppercase()]
            if (rate != null) {
                "Kurs 1 $base = $rate $target"
            } else {
                "Kurs tidak ditemukan untuk $target"
            }
        } catch (e: Exception) {
            "Gagal ambil data kurs: ${e.message}"
        }
    }
}
