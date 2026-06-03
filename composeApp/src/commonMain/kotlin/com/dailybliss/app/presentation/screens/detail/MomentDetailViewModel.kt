package com.dailybliss.app.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.usecase.DeleteMomentUseCase
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.util.AudioPlayer
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
    private val aiRepository: AIRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomentDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MomentDetailEvent>()
    val events = _events.asSharedFlow()

    private var originalMoment: com.dailybliss.app.domain.model.Moment? = null
    private val audioPlayer = AudioPlayer()

    private var cachedTTSContent: String? = null
    private var cachedTTSVoice: String? = null
    private var cachedTTSBytes: ByteArray? = null

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
            val newContent = run {
                var htmlIdx = 0
                var textCount = 0
                val targetCount = if (insertionIndex == -1) {
                    Int.MAX_VALUE
                } else if (insertionIndex < 0) {
                    0
                } else {
                    insertionIndex
                }

                while (htmlIdx < currentContent.length && textCount < targetCount) {
                    if (currentContent[htmlIdx] == '<') {
                        val end = currentContent.indexOf('>', htmlIdx)
                        if (end != -1) {
                            val tag = currentContent.substring(htmlIdx, end + 1)
                            if (tag == "<br/>" || tag == "<br>" || tag == "<br />") {
                                textCount++
                            }
                            htmlIdx = end + 1
                            continue
                        }
                    }
                    htmlIdx++
                    textCount++
                }

                val before = currentContent.substring(0, htmlIdx)
                val after = currentContent.substring(htmlIdx)

                var trimmedBefore = before
                // Strip trailing newlines from 'before'
                while (true) {
                    if (trimmedBefore.endsWith("<br/>")) {
                        trimmedBefore = trimmedBefore.substring(0, trimmedBefore.length - 5)
                    } else if (trimmedBefore.endsWith("<br>")) {
                        trimmedBefore = trimmedBefore.substring(0, trimmedBefore.length - 4)
                    } else if (trimmedBefore.endsWith("<br />")) {
                        trimmedBefore = trimmedBefore.substring(0, trimmedBefore.length - 6)
                    } else {
                        break
                    }
                }

                var trimmedAfter = after
                // Strip leading newlines from 'after'
                while (true) {
                    if (trimmedAfter.startsWith("<br/>")) {
                        trimmedAfter = trimmedAfter.substring(5)
                    } else if (trimmedAfter.startsWith("<br>")) {
                        trimmedAfter = trimmedAfter.substring(4)
                    } else if (trimmedAfter.startsWith("<br />")) {
                        trimmedAfter = trimmedAfter.substring(6)
                    } else {
                        break
                    }
                }

                trimmedBefore + imagesHtml + trimmedAfter
            }

            val updated = currentMoment.copy(
                content = newContent,
                imageUrl = extractFirstImage(newContent),
            )
            _uiState.update { it.copy(moment = updated, isDirty = checkIfDirty(updated)) }
            _events.emit(MomentDetailEvent.ImageInserted(insertionIndex))
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

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    fun playTTS() {
        if (audioPlayer.isPlaying()) {
            audioPlayer.stop()
            _uiState.update { it.copy(isTTSPlaying = false) }
            return
        }

        viewModelScope.launch {
            val content = _uiState.value.moment?.content ?: return@launch
            val mId = _uiState.value.moment?.id ?: return@launch
            val voiceName = userPreferences.ttsVoiceName.first()
            val fileName = "tts_${mId}_${voiceName}.mp3"
            val textFileName = "tts_${mId}_${voiceName}.txt"

            // Local Memory Cache Fallback (for unsaved changes if needed)
            if (content == cachedTTSContent && voiceName == cachedTTSVoice && cachedTTSBytes != null) {
                cachedTTSBytes?.let { bytes ->
                    playAudioBytes(bytes)
                }
                return@launch
            }

            // Check Persistent Storage Cache first
            val cachedFileBytes = fileStorage.loadFile(fileName)
            val cachedTextBytes = fileStorage.loadFile(textFileName)
            if (cachedFileBytes != null && cachedTextBytes != null && cachedTextBytes.decodeToString() == content) {
                cachedTTSBytes = cachedFileBytes
                cachedTTSContent = content
                cachedTTSVoice = voiceName
                playAudioBytes(cachedFileBytes)
                return@launch
            }

            _uiState.update { it.copy(isTTSLoading = true) }

            // Extract plain text and image URLs from HTML content
            val textRegex = Regex("<[^>]*>")
            val plainText = content.replace(textRegex, "").replace("&nbsp;", " ").trim()
            val imageUrls = Regex("<img src=\"(.*?)\" />").findAll(content).map { it.groupValues[1] }.toList()

            // Load images from file storage if any
            val imageBytes = mutableListOf<ByteArray>()
            for (url in imageUrls) {
                fileStorage.loadImage(url)?.let { imageBytes.add(it) }
            }

            val audioRes = aiRepository.generateAudioForMoment(plainText, imageBytes, voiceName)
            _uiState.update { it.copy(isTTSLoading = false) }

            if (audioRes.isSuccess) {
                audioRes.getOrNull()?.let { bytes ->
                    cachedTTSContent = content
                    cachedTTSVoice = voiceName
                    cachedTTSBytes = bytes

                    fileStorage.saveFile(bytes, fileName)
                    fileStorage.saveFile(content.encodeToByteArray(), textFileName)

                    playAudioBytes(bytes)
                }
            } else {
                val e = audioRes.exceptionOrNull()
                println("TTS Error: ${e?.message}")
                e?.printStackTrace()
                val error = e?.message ?: "Gagal memutar audio"
                _events.emit(MomentDetailEvent.Error(error))
            }
        }
    }

    private fun playAudioBytes(bytes: ByteArray) {
        audioPlayer.play(bytes)
        _uiState.update { it.copy(isTTSPlaying = true) }
        viewModelScope.launch {
            while (audioPlayer.isPlaying()) {
                kotlinx.coroutines.delay(500)
            }
            _uiState.update { it.copy(isTTSPlaying = false) }
        }
    }
}

data class MomentDetailUiState(
    val moment: com.dailybliss.app.domain.model.Moment? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val error: String? = null,
    val isTTSLoading: Boolean = false,
    val isTTSPlaying: Boolean = false,
)

sealed interface MomentDetailEvent {
    data class ImageInserted(val index: Int) : MomentDetailEvent
    data class Error(val message: String) : MomentDetailEvent
}
