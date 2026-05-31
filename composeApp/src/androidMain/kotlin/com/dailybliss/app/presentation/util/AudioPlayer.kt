package com.dailybliss.app.presentation.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

actual class AudioPlayer actual constructor() {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    actual fun play(pcmBytes: ByteArray) {
        stop()

        val sampleRate = 24000
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = if (minBufferSize > 0) minBufferSize * 4 else 8192

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build(),
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isPlaying = true

        Thread {
            try {
                // If it's a WAV container, skip the 44 byte header to avoid click sound
                var startIndex = 0
                if (pcmBytes.size > 44 && 
                    pcmBytes[0] == 'R'.code.toByte() && pcmBytes[1] == 'I'.code.toByte() &&
                    pcmBytes[2] == 'F'.code.toByte() && pcmBytes[3] == 'F'.code.toByte()) {
                    startIndex = 44
                }

                var offset = startIndex
                while (offset < pcmBytes.size && isPlaying) {
                    val sizeToWrite = minOf(bufferSize, pcmBytes.size - offset)
                    val written = audioTrack?.write(pcmBytes, offset, sizeToWrite) ?: 0
                    if (written <= 0) {
                        break
                    }
                    offset += written
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPlaying = false
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
            }
        }.start()
    }

    actual fun stop() {
        if (isPlaying) {
            isPlaying = false
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }

    actual fun isPlaying(): Boolean = isPlaying
}
