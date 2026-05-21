package com.dailybliss.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IpResponse(
    val latitude: Double,
    val longitude: Double,
    val city: String,
)

@Serializable
data class WeatherResponse(
    val current: CurrentWeather,
)

@Serializable
data class CurrentWeather(
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
)

@Serializable
data class NewsResponse(
    val data: List<NewsItem> = emptyList(),
)

@Serializable
data class NewsItem(
    val title: String = "",
    val contentSnippet: String? = null,
    val link: String = "",
    val image: NewsImage? = null,
)

@Serializable
data class NewsImage(
    val small: String? = null,
    val large: String? = null,
)
