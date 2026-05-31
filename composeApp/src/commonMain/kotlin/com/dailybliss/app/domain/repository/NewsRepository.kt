package com.dailybliss.app.domain.repository

import com.dailybliss.app.domain.model.CurrencyRates
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo

interface NewsRepository {
    suspend fun getWeather(): WeatherInfo
    suspend fun getPrabowoNews(): List<NewsArticle>
    suspend fun getCurrencyRates(): CurrencyRates?
}
