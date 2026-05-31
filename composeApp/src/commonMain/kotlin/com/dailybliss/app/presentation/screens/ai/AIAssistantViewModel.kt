package com.dailybliss.app.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.ChatMessage
import com.dailybliss.app.domain.repository.AIRepository
import kotlinx.coroutines.flow.*

class AIAssistantViewModel(
    private val aiRepository: AIRepository,
) : ViewModel() {

    private val inputFlow = MutableStateFlow("")
    private val selectedImageBytesFlow = MutableStateFlow<ByteArray?>(null)

    val uiState: StateFlow<AIAssistantUiState> = combine(
        aiRepository.chatMessages,
        aiRepository.isChatLoading,
        inputFlow,
        selectedImageBytesFlow,
    ) { messages, isLoading, input, imageBytes ->
        AIAssistantUiState(
            messages = messages,
            input = input,
            selectedImageBytes = imageBytes,
            isLoading = isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AIAssistantUiState(),
    )

    fun onInputChange(input: String) {
        inputFlow.value = input
    }

    fun onImageSelected(bytes: ByteArray?) {
        selectedImageBytesFlow.value = bytes
    }

    fun sendMessage() {
        val currentInput = inputFlow.value.trim()
        val currentImage = selectedImageBytesFlow.value

        if (currentInput.isBlank() && currentImage == null) return

        aiRepository.sendMessage(currentInput, currentImage)

        inputFlow.value = ""
        selectedImageBytesFlow.value = null
    }
}

data class AIAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val selectedImageBytes: ByteArray? = null,
    val isLoading: Boolean = false,
)
