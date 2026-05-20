package com.dailybliss.app.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.usecase.DeleteMomentUseCase
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.FileStorage
import com.dailybliss.app.presentation.util.HtmlConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.dailybliss.app.core.util.BackgroundAIProcessor

class MomentDetailViewModel(
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
    private val saveMomentUseCase: SaveMomentUseCase,
    private val deleteMomentUseCase: DeleteMomentUseCase,
    private val backgroundAIProcessor: BackgroundAIProcessor,
    private val fileStorage: FileStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<MomentDetailUiState>(MomentDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MomentDetailEvent>()
    val events = _events.asSharedFlow()

    private var currentId: Long = 0
    private var lastAnalyzedContent: String = ""

    fun loadMoment(id: Long) {
        currentId = id
        viewModelScope.launch {
            getMomentByIdUseCase(id).collect { moment ->
                if (moment != null) {
                    _uiState.value = MomentDetailUiState.Success(
                        title = moment.title,
                        content = moment.content,
                        moment = moment
                    )
                    lastAnalyzedContent = moment.content
                } else {
                    _uiState.value = MomentDetailUiState.NotFound
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            _uiState.value = currentState.copy(title = title)
            saveChanges()
        }
    }

    fun onContentChange(content: String) {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            _uiState.value = currentState.copy(content = content)
            saveChanges()
        }
    }

    fun addImage(bytesList: List<ByteArray>, insertionIndex: Int = -1) {
        viewModelScope.launch {
            val urls = bytesList.mapNotNull { fileStorage.saveImage(it) }
            if (urls.isNotEmpty()) {
                val imagesHtml = urls.joinToString("") { "<img src=\"$it\" />" }
                val imageGroupHtml = "<div class=\"image-group\">$imagesHtml</div>"
                
                val currentState = _uiState.value
                if (currentState is MomentDetailUiState.Success) {
                    val currentContent = currentState.content
                    val newContent = if (insertionIndex == -1 || insertionIndex >= currentContent.length) {
                        currentContent + imageGroupHtml
                    } else {
                        var htmlIndex = 0
                        var textCount = 0
                        while (htmlIndex < currentContent.length && textCount < insertionIndex) {
                            if (currentContent[htmlIndex] == '<') {
                                val end = currentContent.indexOf('>', htmlIndex)
                                if (end == -1) break
                                htmlIndex = end + 1
                            } else {
                                htmlIndex++
                                textCount++
                            }
                        }
                        currentContent.substring(0, htmlIndex) + imageGroupHtml + currentContent.substring(htmlIndex)
                    }
                    
                    _uiState.value = currentState.copy(content = newContent)
                    saveChanges()
                }
            }
        }
    }

    private fun saveChanges() {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            viewModelScope.launch {
                val updatedMoment = currentState.moment.copy(
                    title = currentState.title,
                    content = currentState.content
                )
                saveMomentUseCase(updatedMoment)
                
                // Re-analyze if content changed significantly (more than 20 chars or words changed)
                val stripped = currentState.content.replace(Regex("<[^>]*>"), " ").trim()
                val lastStripped = lastAnalyzedContent.replace(Regex("<[^>]*>"), " ").trim()
                
                if (Math.abs(stripped.length - lastStripped.length) > 20 || stripped != lastStripped) {
                    backgroundAIProcessor.processMoment(currentId, force = true)
                    lastAnalyzedContent = currentState.content
                }
            }
        }
    }

    fun deleteMoment() {
        viewModelScope.launch {
            deleteMomentUseCase(currentId)
            _events.emit(MomentDetailEvent.MomentDeleted)
        }
    }
}

sealed interface MomentDetailUiState {
    data object Loading : MomentDetailUiState
    data class Success(
        val title: String,
        val content: String,
        val moment: com.dailybliss.app.domain.model.Moment
    ) : MomentDetailUiState
    data object NotFound : MomentDetailUiState
}

sealed interface MomentDetailEvent {
    data object MomentDeleted : MomentDetailEvent
}
