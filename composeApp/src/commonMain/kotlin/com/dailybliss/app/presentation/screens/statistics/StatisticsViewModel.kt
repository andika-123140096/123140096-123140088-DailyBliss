package com.dailybliss.app.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.MoodStatistics
import com.dailybliss.app.domain.usecase.CalculateStatisticsUseCase
import kotlinx.coroutines.flow.*

data class StatisticsUiState(
    val statistics: MoodStatistics? = null,
    val isLoading: Boolean = true,
)

class StatisticsViewModel(
    private val calculateStatisticsUseCase: CalculateStatisticsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        calculateStatisticsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { stats ->
                _uiState.update { it.copy(statistics = stats, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}
