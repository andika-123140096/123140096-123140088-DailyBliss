package com.dailybliss.app.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.domain.usecase.DeleteMomentUseCase
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MomentDetailViewModel(
    private val momentId: Long,
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
    private val deleteMomentUseCase: DeleteMomentUseCase,
    private val saveMomentUseCase: SaveMomentUseCase,
    private val backgroundAIProcessor: BackgroundAIProcessor,
    private val fileStorage: FileStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomentDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMoment()
    }

    private fun loadMoment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMomentByIdUseCase(momentId).collect { moment ->
                if (moment != null) {
                    _uiState.update { it.copy(moment = moment, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Momen tidak ditemukan") }
                }
            }
        }
    }

    fun togglePin() {
        val currentMoment = _uiState.value.moment ?: return
        viewModelScope.launch {
            val updated = currentMoment.copy(isPinned = !currentMoment.isPinned)
            saveMomentUseCase(updated)
            // Local update for immediate feedback
            _uiState.update { it.copy(moment = updated) }
        }
    }

    fun deleteMoment(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteMomentUseCase(momentId)
            onDeleted()
        }
    }

    fun refreshAIAnalysis() {
        backgroundAIProcessor.processMoment(momentId, force = true)
    }
}

data class MomentDetailUiState(
    val moment: com.dailybliss.app.domain.model.Moment? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
