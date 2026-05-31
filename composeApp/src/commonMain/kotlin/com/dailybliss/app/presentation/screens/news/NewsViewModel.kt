package com.dailybliss.app.presentation.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.CurrencyRates
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo
import com.dailybliss.app.domain.repository.NewsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NewsUiState(
    val weather: WeatherInfo? = null,
    val news: List<NewsArticle> = emptyList(),
    val currencyRates: CurrencyRates? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class NewsViewModel(
    private val newsRepository: NewsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadNewsData() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                supervisorScope {
                    val jobs = listOf(
                        launch {
                            try {
                                val weather = newsRepository.getWeather()
                                _uiState.update { it.copy(weather = weather) }
                            } catch (e: Exception) {
                                println("NewsViewModel: Weather error: ${e.message}")
                            }
                        },
                        launch {
                            try {
                                val news = newsRepository.getPrabowoNews()
                                val finalNews = if (news.isEmpty()) {
                                    listOf(NewsArticle("Info", "Belum ada berita Prabowo terbaru.", "", ""))
                                } else {
                                    news
                                }
                                _uiState.update { it.copy(news = finalNews) }
                            } catch (e: Exception) {
                                _uiState.update {
                                    it.copy(news = listOf(NewsArticle("Info", "Gagal memuat berita saat ini.", "", "")))
                                }
                            }
                        },
                        launch {
                            try {
                                val currency = newsRepository.getCurrencyRates()
                                val finalCurrency = currency ?: CurrencyRates(16000.0, 12000.0)
                                _uiState.update { it.copy(currencyRates = finalCurrency) }
                            } catch (e: Exception) {
                                _uiState.update { it.copy(currencyRates = CurrencyRates(16000.0, 12000.0)) }
                            }
                        },
                    )

                    // Give a standard 500ms delay for visual feedback
                    delay(500)
                    jobs.joinAll()
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
