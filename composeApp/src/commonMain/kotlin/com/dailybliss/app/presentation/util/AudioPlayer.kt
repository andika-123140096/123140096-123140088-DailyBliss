package com.dailybliss.app.presentation.util

expect class AudioPlayer() {
    fun play(pcmBytes: ByteArray)
    fun stop()
    fun isPlaying(): Boolean
}
