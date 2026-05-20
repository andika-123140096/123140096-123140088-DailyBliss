package com.dailybliss.app.presentation.screens.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CreateMomentViewModel(
    private val saveMomentUseCase: SaveMomentUseCase,
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
    private val backgroundAIProcessor: BackgroundAIProcessor,
    private val fileStorage: FileStorage,
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
                        createdAt = it.createdAt,
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(
            content = content,
            imageUrl = extractFirstImage(content)
        ) }
    }

    fun addImage(bytesList: List<ByteArray>, insertionIndex: Int = -1) {
        viewModelScope.launch {
            val urls = bytesList.mapNotNull { fileStorage.saveImage(it) }
            if (urls.isEmpty()) return@launch

            val imagesHtml = "<div class=\"image-group\">" +
                urls.joinToString("") { "<img src=\"$it\" />" } +
                "</div>"

            _uiState.update { state ->
                val currentContent = state.content
                
                val newContent = if (insertionIndex == -1 || insertionIndex >= currentContent.length) {
                    currentContent + imagesHtml
                } else {
                    // Find actual HTML index corresponding to text index
                    var htmlIdx = 0
                    var textCount = 0
                    while (htmlIdx < currentContent.length && textCount < insertionIndex) {
                        if (currentContent[htmlIdx] == '<') {
                            val end = currentContent.indexOf('>', htmlIdx)
                            if (end != -1) {
                                htmlIdx = end + 1
                                continue
                            }
                        }
                        htmlIdx++
                        textCount++
                    }
                    currentContent.substring(0, htmlIdx) + imagesHtml + currentContent.substring(htmlIdx)
                }

                state.copy(
                    content = newContent,
                    imageUrl = extractFirstImage(newContent)
                )
            }
        }
    }

    private fun extractFirstImage(html: String): String? {
        val match = Regex("<img src=\"(.*?)\" />").find(html)
        return match?.groupValues?.get(1)
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
                updatedAt = Clock.System.now(),
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
    val createdAt: Instant = Clock.System.now(),
)

sealed interface CreateMomentEvent {
    data object MomentSaved : CreateMomentEvent
    data class Error(val message: String) : CreateMomentEvent
}
