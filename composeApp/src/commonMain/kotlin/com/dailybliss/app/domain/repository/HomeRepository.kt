package com.dailybliss.app.domain.repository

import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo

interface HomeRepository {
    suspend fun getWeather(): WeatherInfo
    suspend fun getPrabowoNews(): List<NewsArticle>
}
