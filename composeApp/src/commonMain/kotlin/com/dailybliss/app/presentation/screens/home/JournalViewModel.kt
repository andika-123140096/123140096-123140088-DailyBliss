package com.dailybliss.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetAllMomentsUseCase
import com.dailybliss.app.domain.usecase.MomentSortBy
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
class JournalViewModel(private val getAllMomentsUseCase: GetAllMomentsUseCase) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _sortBy = MutableStateFlow(MomentSortBy.UPDATED_DESC)
    val sortBy = _sortBy.asStateFlow()

    private val pageSize = MutableStateFlow(10)

    val uiState: StateFlow<JournalUiState> =
        combine(
            getAllMomentsUseCase(),
            _query.debounce(300L),
            _sortBy,
            pageSize,
        ) { moments, query, sort, size ->
            var filtered = moments

            if (query.isNotBlank()) {
                filtered =
                    filtered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                    }
            }

            filtered =
                when (sort) {
                    MomentSortBy.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
                    MomentSortBy.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
                    MomentSortBy.CREATED_ASC -> filtered.sortedBy { it.createdAt }
                    MomentSortBy.CREATED_DESC -> filtered.sortedByDescending { it.createdAt }
                    MomentSortBy.UPDATED_ASC -> filtered.sortedBy { it.updatedAt }
                    MomentSortBy.UPDATED_DESC -> filtered.sortedByDescending { it.updatedAt }
                }

            val isLastPage = filtered.size <= pageSize
            val paged = filtered.take(pageSize)

            if (filtered.isEmpty()) {
                JournalUiState.Empty(query)
            } else {
                JournalUiState.Success(paged, query, isLastPage)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = JournalUiState.Loading,
        )

    fun onSearchQueryChange(newQuery: String) {
        _query.value = newQuery
        _pageSize.value = 10
    }

    fun clearSearch() {
        _query.value = ""
        _pageSize.value = 10
    }

    fun loadMore() {
        _pageSize.value += 10
    }
}

sealed interface JournalUiState {
    data object Loading : JournalUiState

    data class Success(val moments: List<Moment>, val query: String = "", val isLastPage: Boolean = true) : JournalUiState

    data class Empty(val query: String = "") : JournalUiState

    data class Error(val message: String) : JournalUiState
}
