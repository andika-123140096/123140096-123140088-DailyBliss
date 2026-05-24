package com.dailybliss.app.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetMomentsForDateUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class DailyMomentsUiState(
    val date: LocalDate,
    val moments: List<Moment> = emptyList(),
    val isLoading: Boolean = true,
)

class DailyMomentsViewModel(
    private val dateStr: String,
    private val getMomentsForDateUseCase: GetMomentsForDateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DailyMomentsUiState(date = LocalDate.parse(dateStr)),
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadMoments()
    }

    private fun loadMoments() {
        val selected = _uiState.value.date
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMomentsForDateUseCase(selected).collect { moments ->
                _uiState.update { it.copy(moments = moments, isLoading = false) }
            }
        }
    }
}
