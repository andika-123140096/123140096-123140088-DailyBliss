package com.dailybliss.app.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSpeechToTextManager : SpeechToTextManager {
    private val _isListening = MutableStateFlow(false)
    override val isListening = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    override val recognizedText = _recognizedText.asStateFlow()

    var startListeningCalled = false
    var stopListeningCalled = false

    override fun startListening() {
        startListeningCalled = true
        _isListening.value = true
    }

    override fun stopListening() {
        stopListeningCalled = true
        _isListening.value = false
    }

    fun emitRecognizedText(text: String) {
        _recognizedText.value = text
    }
}
