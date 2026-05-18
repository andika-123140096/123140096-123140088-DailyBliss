package com.dailybliss.app.core.util

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface VoiceToTextParser {
    val state: StateFlow<VoiceToTextParserState>
    val finalResult: SharedFlow<String>
    fun startListening(languageCode: String = "id-ID")
    fun stopListening()
    fun reset()
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)
