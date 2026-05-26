package com.dailybliss.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)

class HomeViewModel(
    private val momentRepository: MomentRepository,
    private val userPreferences: UserPreferences,
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
                userPreferences.journalSummary,
                momentRepository.getAllMoments(),
            ) { nickname, summary, moments ->
                val moodCounts = moments.mapNotNull { it.mood?.split(" ")?.lastOrNull() }
                    .groupingBy { it }
                    .eachCount()

                val insight = if (summary.isNotBlank()) {
                    // We just use the summary or a part of it as insight for now
                    // In a real app, we might call Gemini to generate a short affirmation from this summary
                    summary.take(150) + if (summary.length > 150) "..." else ""
                } else {
                    "Mulai menulis jurnal hari ini untuk mendapatkan insight personal dari AI!"
                }

                HomeUiState(
                    nickname = nickname,
                    totalMoments = moments.size,
                    moodStats = moodCounts,
                    recentMoments = moments.take(3),
                    dailyInsight = insight,
                    isLoading = false,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
