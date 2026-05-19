package com.dailybliss.app.presentation.screens.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.domain.model.ContentBlock
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.model.MomentContent
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

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
    private val json = Json { ignoreUnknownKeys = true }
    
    fun loadMoment(id: Long) {
        if (currentMomentId == id) return
        currentMomentId = id
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val moment = getMomentByIdUseCase(id).first()
            
            moment?.let {
                val parsedContent = try {
                    if (it.content.startsWith("{\"blocks\":")) {
                        // Legacy support for older JSON format
                        val legacyContent = json.decodeFromString<LegacyMomentContent>(it.content)
                        val convertedBlocks = legacyContent.blocks.map { block ->
                            when (block) {
                                is LegacyContentBlock.Text -> ContentBlock.Html(block.text)
                                is LegacyContentBlock.Image -> ContentBlock.ImageGroup(listOf(block.url))
                            }
                        }
                        MomentContent(convertedBlocks)
                    } else if (it.content.contains("<") || it.content.isNotEmpty()) {
                        // New HTML format
                        MomentContent.fromHtml(it.content)
                    } else {
                        MomentContent(listOf(ContentBlock.Html("")))
                    }
                } catch (e: Exception) {
                    MomentContent(listOf(ContentBlock.Html(it.content)))
                }

                _uiState.update { state ->
                    state.copy(
                        title = it.title,
                        contentBlocks = if (parsedContent.blocks.isEmpty()) listOf(ContentBlock.Html("")) else parsedContent.blocks,
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

    fun onBlockChange(index: Int, block: ContentBlock) {
        _uiState.update { state ->
            val newBlocks = state.contentBlocks.toMutableList()
            if (index in newBlocks.indices) {
                newBlocks[index] = block
                state.copy(contentBlocks = newBlocks)
            } else state
        }
    }

    fun addHtmlBlock(afterIndex: Int? = null) {
        _uiState.update { state ->
            val newBlocks = state.contentBlocks.toMutableList()
            val newBlock = ContentBlock.Html("")
            val newIndex = if (afterIndex != null && afterIndex + 1 <= newBlocks.size) {
                afterIndex + 1
            } else {
                newBlocks.size
            }
            newBlocks.add(newIndex, newBlock)
            state.copy(
                contentBlocks = newBlocks,
                requestedFocusIndex = newIndex
            )
        }
    }

    fun addImageGroupBlock(bytesList: List<ByteArray>, afterIndex: Int? = null) {
        viewModelScope.launch {
            val urls = bytesList.mapNotNull { fileStorage.saveImage(it) }
            if (urls.isNotEmpty()) {
                _uiState.update { state ->
                    val newBlocks = state.contentBlocks.toMutableList()
                    val newBlock = ContentBlock.ImageGroup(urls)
                    val newIndex = if (afterIndex != null && afterIndex + 1 <= newBlocks.size) {
                        afterIndex + 1
                    } else {
                        newBlocks.size
                    }
                    newBlocks.add(newIndex, newBlock)
                    
                    // If the block before is text and empty, remove it
                    if (afterIndex != null && afterIndex < newBlocks.size - 1) {
                        val beforeBlock = newBlocks.getOrNull(afterIndex)
                        if (beforeBlock is ContentBlock.Html && beforeBlock.content.isEmpty() && newBlocks.size > 1) {
                            newBlocks.removeAt(afterIndex)
                        }
                    }

                    state.copy(
                        contentBlocks = newBlocks,
                        requestedFocusIndex = null
                    )
                }
            }
        }
    }

    fun clearFocusRequest() {
        _uiState.update { it.copy(requestedFocusIndex = null) }
    }

    fun removeBlock(index: Int) {
        _uiState.update { state ->
            if (state.contentBlocks.size > 1) {
                val newBlocks = state.contentBlocks.toMutableList()
                newBlocks.removeAt(index)
                
                val focusBackIndex = if (index > 0) index - 1 else 0
                state.copy(
                    contentBlocks = newBlocks,
                    requestedFocusIndex = focusBackIndex
                )
            } else {
                state.copy(
                    contentBlocks = listOf(ContentBlock.Html("")),
                    imageUrl = null
                )
            }
        }
    }

    fun saveMoment() {
        val state = _uiState.value
        val allText = state.contentBlocks.filterIsInstance<ContentBlock.Html>().joinToString("\n") { it.content }
        
        if (state.title.isBlank() && allText.isBlank()) {
            _uiState.update { it.copy(titleError = "Tuliskan sesuatu...") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val momentContent = MomentContent(state.contentBlocks)
            val htmlContent = momentContent.toHtml()
            val mainImageUrl = state.contentBlocks.filterIsInstance<ContentBlock.ImageGroup>().firstOrNull()?.urls?.firstOrNull()
            
            val moment = Moment(
                id = currentMomentId ?: 0,
                title = state.title.trim(),
                content = htmlContent,
                imageUrl = mainImageUrl,
                mood = state.mood,
                tags = state.tags,
                createdAt = if (currentMomentId == null) Clock.System.now() else state.createdAt,
                updatedAt = Clock.System.now()
            )
            
            val newId = saveMomentUseCase(moment)
            if (currentMomentId == null) {
                currentMomentId = newId
            }
            
            // Trigger background AI processing for tagging and mood analysis
            backgroundAIProcessor.processMoment(newId)
            
            _uiState.update { it.copy(isSaving = false) }
            _events.emit(CreateMomentEvent.MomentSaved)
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

data class CreateMomentUiState(
    val title: String = "",
    val contentBlocks: List<ContentBlock> = listOf(ContentBlock.Html("")),
    val imageUrl: String? = null,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val titleError: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val requestedFocusIndex: Int? = null
)

sealed interface CreateMomentEvent {
    data object MomentSaved : CreateMomentEvent
    data class Error(val message: String) : CreateMomentEvent
}
