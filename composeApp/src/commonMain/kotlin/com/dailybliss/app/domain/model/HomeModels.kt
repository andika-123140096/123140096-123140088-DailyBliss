package com.dailybliss.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherInfo(
    val temperature: String,
    val windSpeed: String,
    val city: String,
)

@Serializable
data class NewsArticle(
    val title: String,
    val summary: String,
    val imageUrl: String,
    val url: String,
)

@Serializable
data class CurrencyRates(
    val usdToIdr: Double,
    val sgdToIdr: Double,
)
