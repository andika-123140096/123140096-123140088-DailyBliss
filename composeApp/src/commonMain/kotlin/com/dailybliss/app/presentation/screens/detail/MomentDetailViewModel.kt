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

    private var originalMoment: com.dailybliss.app.domain.model.Moment? = null

    init {
        loadMoment()
    }

    private fun loadMoment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val moment = getMomentByIdUseCase(momentId).first()
            if (moment != null) {
                originalMoment = moment
                _uiState.update { it.copy(moment = moment, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Momen tidak ditemukan") }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        val currentMoment = _uiState.value.moment ?: return
        if (currentMoment.title == newTitle) return

        val updated = currentMoment.copy(title = newTitle)
        _uiState.update { it.copy(moment = updated, isDirty = checkIfDirty(updated)) }
    }

    fun updateContent(newContent: String) {
        val currentMoment = _uiState.value.moment ?: return
        if (currentMoment.content == newContent) return

        val updated = currentMoment.copy(
            content = newContent,
            imageUrl = extractFirstImage(newContent),
        )
        _uiState.update { it.copy(moment = updated, isDirty = checkIfDirty(updated)) }
    }

    fun addImage(bytesList: List<ByteArray>, insertionIndex: Int = -1) {
        viewModelScope.launch {
            val urls = bytesList.mapNotNull { fileStorage.saveImage(it) }
            if (urls.isEmpty()) return@launch

            val currentMoment = _uiState.value.moment ?: return@launch
            val imagesHtml = "<div class=\"image-group\">" +
                urls.joinToString("") { "<img src=\"$it\" />" } +
                "</div>"

            val currentContent = currentMoment.content
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

            val updated = currentMoment.copy(
                content = newContent,
                imageUrl = extractFirstImage(newContent),
            )
            _uiState.update { it.copy(moment = updated, isDirty = checkIfDirty(updated)) }
        }
    }

    private fun checkIfDirty(current: com.dailybliss.app.domain.model.Moment): Boolean {
        val original = originalMoment ?: return false
        return current.title != original.title || current.content != original.content
    }

    fun saveChanges() {
        val current = _uiState.value.moment ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            saveMomentUseCase(current)
            originalMoment = current
            _uiState.update { it.copy(isSaving = false, isDirty = false) }
        }
    }

    private fun extractFirstImage(html: String): String? {
        val match = Regex("<img src=\"(.*?)\" />").find(html)
        return match?.groupValues?.get(1)
    }

    fun deleteMoment(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteMomentUseCase(momentId)
            onDeleted()
        }
    }
}

data class MomentDetailUiState(
    val moment: com.dailybliss.app.domain.model.Moment? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val error: String? = null,
)
