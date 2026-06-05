package com.dailybliss.app.core.util

import kotlinx.coroutines.flow.StateFlow

interface SpeechToTextManager {
    val isListening: StateFlow<Boolean>
    val recognizedText: StateFlow<String>

    fun startListening()
    fun stopListening()
}

expect class DailyBlissSpeechToTextManager(context: PlatformContext) : SpeechToTextManager
