package com.dailybliss.app.presentation.screens.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.FileStorage
import com.dailybliss.app.presentation.util.HtmlConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CreateMomentViewModel(
    private val saveMomentUseCase: SaveMomentUseCase,
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
    private val aiRepository: AIRepository,
    private val backgroundAIProcessor: BackgroundAIProcessor,
    private val fileStorage: FileStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMomentUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateMomentEvent>()
    val events = _events.asSharedFlow()

    private var currentMomentId: Long? = null
    
    fun loadMoment(id: Long) {
        if (currentMomentId == id) return
        currentMomentId = id
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val moment = getMomentByIdUseCase(id).first()
            
            moment?.let {
                _uiState.update { state ->
                    state.copy(
                        title = it.title,
                        content = it.content,
                        imageUrl = it.imageUrl,
                        mood = it.mood,
                        tags = it.tags,
                        isLoading = false,
                        isEditMode = true,
                        createdAt = it.createdAt
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun addImage(bytesList: List<ByteArray>, insertionIndex: Int = -1) {
        viewModelScope.launch {
            val urls = bytesList.mapNotNull { fileStorage.saveImage(it) }
            if (urls.isNotEmpty()) {
                val imagesHtml = urls.joinToString("") { "<img src=\"$it\" />" }
                val imageGroupHtml = "<div class=\"image-group\">$imagesHtml</div>"
                
                _uiState.update { state ->
                    val newContent = if (insertionIndex == -1 || insertionIndex >= state.content.length) {
                        state.content + imageGroupHtml
                    } else {
                        var htmlIndex = 0
                        var textCount = 0
                        while (htmlIndex < state.content.length && textCount < insertionIndex) {
                            if (state.content[htmlIndex] == '<') {
                                val end = state.content.indexOf('>', htmlIndex)
                                if (end == -1) break
                                htmlIndex = end + 1
                            } else {
                                htmlIndex++
                                textCount++
                            }
                        }
                        state.content.substring(0, htmlIndex) + imageGroupHtml + state.content.substring(htmlIndex)
                    }
                    
                    state.copy(
                        content = newContent,
                        imageUrl = urls.first()
                    )
                }
            }
        }
    }

    fun saveMoment() {
        val state = _uiState.value
        
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.update { it.copy(titleError = "Tuliskan sesuatu...") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val moment = Moment(
                id = currentMomentId ?: 0,
                title = state.title.trim(),
                content = state.content,
                imageUrl = state.imageUrl,
                mood = state.mood,
                tags = state.tags,
                createdAt = if (currentMomentId == null) Clock.System.now() else state.createdAt,
                updatedAt = Clock.System.now()
            )
            
            val newId = saveMomentUseCase(moment)
            if (currentMomentId == null) {
                currentMomentId = newId
            }
            
            backgroundAIProcessor.processMoment(newId)
            
            _uiState.update { it.copy(isSaving = false) }
            _events.emit(CreateMomentEvent.MomentSaved)
        }
    }
}

data class CreateMomentUiState(
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val titleError: String? = null,
    val createdAt: Instant = Clock.System.now()
)

sealed interface CreateMomentEvent {
    data object MomentSaved : CreateMomentEvent
    data class Error(val message: String) : CreateMomentEvent
}
