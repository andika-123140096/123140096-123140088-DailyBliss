package com.dailybliss.app.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.ContentBlock
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.model.MomentContent
import com.dailybliss.app.domain.usecase.DeleteMomentUseCase
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class MomentDetailViewModel(
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
    private val saveMomentUseCase: SaveMomentUseCase,
    private val deleteMomentUseCase: DeleteMomentUseCase,
    private val fileStorage: FileStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<MomentDetailUiState>(MomentDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MomentDetailEvent>()
    val events = _events.asSharedFlow()

    private var currentMomentId: Long? = null
    private val json = Json { ignoreUnknownKeys = true }
    
    fun loadMoment(id: Long) {
        if (currentMomentId == id) return
        currentMomentId = id
        
        viewModelScope.launch {
            val moment = getMomentByIdUseCase(id).first()
            if (moment != null) {
                val parsedContent = try {
                    if (moment.content.startsWith("{\"blocks\":")) {
                        // Legacy support for older JSON format
                        val legacyContent = json.decodeFromString<LegacyMomentContent>(moment.content)
                        val convertedBlocks = legacyContent.blocks.map { block ->
                            when (block) {
                                is LegacyContentBlock.Text -> ContentBlock.Html(block.text)
                                is LegacyContentBlock.Image -> ContentBlock.ImageGroup(listOf(block.url))
                            }
                        }
                        MomentContent(convertedBlocks)
                    } else if (moment.content.contains("<") || moment.content.isNotEmpty()) {
                        // New HTML format
                        MomentContent.fromHtml(moment.content)
                    } else {
                        MomentContent(listOf(ContentBlock.Html("")))
                    }
                } catch (e: Exception) {
                    MomentContent(listOf(ContentBlock.Html(moment.content)))
                }

                _uiState.update { 
                    MomentDetailUiState.Success(
                        moment = moment,
                        title = moment.title,
                        contentBlocks = if (parsedContent.blocks.isEmpty()) listOf(ContentBlock.Html("")) else parsedContent.blocks,
                        requestedFocusIndex = null
                    )
                }
            } else {
                _uiState.value = MomentDetailUiState.NotFound
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            _uiState.value = currentState.copy(title = newTitle)
            saveChangesInternal()
        }
    }

    fun onBlockChange(index: Int, block: ContentBlock) {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            val newBlocks = currentState.contentBlocks.toMutableList()
            if (index in newBlocks.indices) {
                newBlocks[index] = block
                _uiState.value = currentState.copy(contentBlocks = newBlocks)
                saveChangesInternal()
            }
        }
    }

    fun addHtmlBlock(afterIndex: Int) {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            val newBlocks = currentState.contentBlocks.toMutableList()
            val newIndex = afterIndex + 1
            newBlocks.add(newIndex, ContentBlock.Html(""))
            _uiState.value = currentState.copy(
                contentBlocks = newBlocks,
                requestedFocusIndex = newIndex
            )
            saveChangesInternal()
        }
    }

    fun addImageGroupBlock(bytesList: List<ByteArray>, afterIndex: Int) {
        viewModelScope.launch {
            val urls = bytesList.mapNotNull { fileStorage.saveImage(it) }
            if (urls.isNotEmpty()) {
                val currentState = _uiState.value
                if (currentState is MomentDetailUiState.Success) {
                    val newBlocks = currentState.contentBlocks.toMutableList()
                    val newIndex = afterIndex + 1
                    newBlocks.add(newIndex, ContentBlock.ImageGroup(urls))
                    
                    // If the block before is text and empty, remove it
                    val beforeBlock = newBlocks.getOrNull(afterIndex)
                    if (beforeBlock is ContentBlock.Html && beforeBlock.content.isEmpty() && newBlocks.size > 1) {
                        newBlocks.removeAt(afterIndex)
                    }

                    _uiState.value = currentState.copy(
                        contentBlocks = newBlocks,
                        requestedFocusIndex = null
                    )
                    saveChangesInternal()
                }
            }
        }
    }

    fun clearFocusRequest() {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            _uiState.value = currentState.copy(requestedFocusIndex = null)
        }
    }

    fun removeBlock(index: Int) {
        val currentState = _uiState.value
        if (currentState is MomentDetailUiState.Success) {
            if (currentState.contentBlocks.size > 1) {
                val newBlocks = currentState.contentBlocks.toMutableList()
                newBlocks.removeAt(index)
                val focusBackIndex = if (index > 0) index - 1 else 0
                _uiState.value = currentState.copy(
                    contentBlocks = newBlocks,
                    requestedFocusIndex = focusBackIndex
                )
                saveChangesInternal()
            } else {
                _uiState.value = currentState.copy(
                    contentBlocks = listOf(ContentBlock.Html("")),
                )
                saveChangesInternal()
            }
        }
    }

    private fun saveChangesInternal() {
        val state = _uiState.value
        if (state is MomentDetailUiState.Success) {
            viewModelScope.launch {
                val momentContent = MomentContent(state.contentBlocks)
                val htmlContent = momentContent.toHtml()
                val mainImageUrl = state.contentBlocks.filterIsInstance<ContentBlock.ImageGroup>().firstOrNull()?.urls?.firstOrNull()
                
                val updatedMoment = state.moment.copy(
                    title = state.title.trim(),
                    content = htmlContent,
                    imageUrl = mainImageUrl ?: state.moment.imageUrl,
                    updatedAt = Clock.System.now()
                )
                saveMomentUseCase(updatedMoment)
            }
        }
    }

    fun saveChanges() {
        saveChangesInternal()
        viewModelScope.launch {
            _events.emit(MomentDetailEvent.MomentSaved)
        }
    }

    fun deleteMoment() {
        val id = currentMomentId ?: return
        viewModelScope.launch {
            deleteMomentUseCase(id)
            _events.emit(MomentDetailEvent.MomentDeleted)
        }
    }
}

// Helper classes for legacy JSON format support
@kotlinx.serialization.Serializable
private sealed class LegacyContentBlock {
    @kotlinx.serialization.Serializable
    @kotlinx.serialization.SerialName("com.dailybliss.app.domain.model.ContentBlock.Text")
    data class Text(val text: String) : LegacyContentBlock()
    
    @kotlinx.serialization.Serializable
    @kotlinx.serialization.SerialName("com.dailybliss.app.domain.model.ContentBlock.Image")
    data class Image(val url: String, val caption: String? = null) : LegacyContentBlock()
}

@kotlinx.serialization.Serializable
private data class LegacyMomentContent(
    val blocks: List<LegacyContentBlock>
)

sealed interface MomentDetailUiState {
    data object Loading : MomentDetailUiState
    data class Success(
        val moment: Moment,
        val title: String,
        val contentBlocks: List<ContentBlock>,
        val requestedFocusIndex: Int? = null
    ) : MomentDetailUiState
    data object NotFound : MomentDetailUiState
}

sealed interface MomentDetailEvent {
    data object MomentDeleted : MomentDetailEvent
    data object MomentSaved : MomentDetailEvent
    data class Error(val message: String) : MomentDetailEvent
}
