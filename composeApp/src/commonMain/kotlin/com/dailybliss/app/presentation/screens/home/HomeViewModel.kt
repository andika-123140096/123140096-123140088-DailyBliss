package com.dailybliss.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.CurrencyRates
import com.dailybliss.app.domain.model.NewsArticle
import com.dailybliss.app.domain.model.WeatherInfo
import com.dailybliss.app.domain.repository.HomeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val weather: WeatherInfo? = null,
    val news: List<NewsArticle> = emptyList(),
    val currencyRates: CurrencyRates? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val weatherDeferred = async { homeRepository.getWeather() }
                val newsDeferred = async { homeRepository.getPrabowoNews() }
                val currencyDeferred = async { homeRepository.getCurrencyRates() }

                val weather = weatherDeferred.await()
                val news = newsDeferred.await()
                val currency = currencyDeferred.await()

                _uiState.update {
                    it.copy(
                        weather = weather,
                        news = news,
                        currencyRates = currency,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat data: ${e.message}"
                    )
                }
            }
        }
    }
}
