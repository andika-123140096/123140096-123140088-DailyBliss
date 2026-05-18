package com.dailybliss.app.core.util

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AndroidVoiceToTextParser(
    private val context: PlatformContext
) : VoiceToTextParser, RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    override val state: StateFlow<VoiceToTextParserState> = _state.asStateFlow()

    private val _finalResult = MutableSharedFlow<String>()
    override val finalResult: SharedFlow<String> = _finalResult.asSharedFlow()

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context.androidContext)
    private var isContinuousListening = false
    private var language = "id-ID"
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun startListening(languageCode: String) {
        language = languageCode
        isContinuousListening = true
        _state.update { VoiceToTextParserState(isSpeaking = true) }

        if (!SpeechRecognizer.isRecognitionAvailable(context.androidContext)) {
            _state.update { it.copy(error = "Speech recognition is not available", isSpeaking = false) }
            isContinuousListening = false
            return
        }

        startRecognizer()
    }
    
    private fun startRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
    }

    override fun stopListening() {
        isContinuousListening = false
        _state.update { it.copy(isSpeaking = false, spokenText = "") }
        recognizer.stopListening()
    }

    override fun reset() {
        _state.update { VoiceToTextParserState() }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update { it.copy(error = null) }
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    
    override fun onEndOfSpeech() {
        // Speech ended, wait for onResults
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_CLIENT) return
        
        if (isContinuousListening) {
            // Restart if it's a timeout or non-fatal error during continuous listening
            startRecognizer()
        } else {
            _state.update { it.copy(error = "Error: $error", isSpeaking = false) }
        }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { text ->
                if (text.isNotBlank()) {
                    scope.launch {
                        _finalResult.emit(text)
                    }
                }
            }
            
        _state.update { it.copy(spokenText = "") }
        
        if (isContinuousListening) {
            startRecognizer()
        } else {
            _state.update { it.copy(isSpeaking = false) }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { text ->
                _state.update { it.copy(spokenText = text) }
            }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
