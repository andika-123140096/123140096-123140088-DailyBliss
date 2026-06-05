package com.dailybliss.app.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.*
import platform.Foundation.*
import platform.Speech.*

actual class DailyBlissSpeechToTextManager actual constructor(context: PlatformContext) : SpeechToTextManager {
    private val speechRecognizer = SFSpeechRecognizer(NSLocale.currentLocale)
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: SFSpeechRecognitionTask? = null
    private val audioEngine = AVAudioEngine()

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    override val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    override fun startListening() {
        if (recognitionTask != null) {
            recognitionTask?.cancel()
            recognitionTask = null
        }

        _recognizedText.update { "" }

        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryRecord, error = null)
        audioSession.setActive(true, error = null)

        recognitionRequest = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
        }

        val inputNode = audioEngine.inputNode

        recognitionTask = speechRecognizer?.recognitionTaskWithRequest(recognitionRequest!!) { result, error ->
            if (result != null) {
                _recognizedText.update { result.bestTranscription.formattedString }
                if (result.isFinal()) {
                    stopListening()
                }
            }
            if (error != null) {
                stopListening()
            }
        }

        val recordingFormat = inputNode.outputFormatForBus(0u)
        inputNode.installTapOnBus(0u, 1024u, recordingFormat) { buffer, _ ->
            recognitionRequest?.appendAudioBuffer(buffer!!)
        }

        audioEngine.prepare()
        audioEngine.startAndReturnError(null)

        _isListening.update { true }
    }

    override fun stopListening() {
        audioEngine.stop()
        audioEngine.inputNode.removeTapOnBus(0u)
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()

        _isListening.update { false }
    }
}
