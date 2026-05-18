package com.dailybliss.app.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class IosVoiceToTextParser : VoiceToTextParser {
    private val _state = MutableStateFlow(VoiceToTextParserState())
    override val state: StateFlow<VoiceToTextParserState> = _state.asStateFlow()

    private val _finalResult = MutableSharedFlow<String>()
    override val finalResult: SharedFlow<String> = _finalResult.asSharedFlow()

    override fun startListening(languageCode: String) {
        // Not implemented for iOS in this demo
    }

    override fun stopListening() {}
    override fun reset() {}
}
