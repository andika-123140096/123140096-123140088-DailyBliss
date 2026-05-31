package com.dailybliss.app.data.repository

import com.dailybliss.app.domain.model.CurrencyRates
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo
import com.dailybliss.app.domain.repository.NewsRepository

class FakeNewsRepository : NewsRepository {
    var mockWeather = WeatherInfo("25°C", "10km/h", "Bandar Lampung")
    var mockNews = listOf(NewsArticle("Title", "Summary", "url", "image"))
    var mockCurrency = CurrencyRates(15500.0, 11500.0)

    override suspend fun getWeather(): WeatherInfo = mockWeather
    override suspend fun getPrabowoNews(): List<NewsArticle> = mockNews
    override suspend fun getCurrencyRates(): CurrencyRates? = mockCurrency
}
