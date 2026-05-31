package com.dailybliss.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.repository.MomentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val nickname: String = "",
    val totalMoments: Int = 0,
    val moodStats: Map<String, Int> = emptyMap(),
    val recentMoments: List<Moment> = emptyList(),
    val dailyInsight: String = "",
    val isLoading: Boolean = false,
    val isAiProcessing: Boolean = false,
)

class HomeViewModel(
    private val momentRepository: MomentRepository,
    private val userPreferences: UserPreferences,
    private val backgroundAIProcessor: BackgroundAIProcessor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combine flows for efficiency
            combine(
                userPreferences.nickname,
                userPreferences.dailyInsight,
                momentRepository.getAllMoments(),
                backgroundAIProcessor.isProcessing,
            ) { nickname, insight, moments, isAiProcessing ->
                val moodCounts = moments.mapNotNull { it.mood?.split(" ")?.lastOrNull() }
                    .groupingBy { it }
                    .eachCount()

                val finalInsight = if (insight.isNotBlank()) {
                    insight
                } else {
                    "Mulai menulis jurnal hari ini untuk mendapatkan insight personal dari AI!"
                }

                HomeUiState(
                    nickname = nickname,
                    totalMoments = moments.size,
                    moodStats = moodCounts,
                    recentMoments = moments.take(3),
                    dailyInsight = finalInsight,
                    isLoading = false,
                    isAiProcessing = isAiProcessing,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
