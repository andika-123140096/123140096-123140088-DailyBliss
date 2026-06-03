package com.dailybliss.app.presentation.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

actual class AudioPlayer actual constructor() {
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isPlaying = false

    actual fun play(pcmBytes: ByteArray) {
        stop()
        isPlaying = true

        Thread {
            var track: AudioTrack? = null
            try {
                var sampleRate = 24000
                var channelConfig = AudioFormat.CHANNEL_OUT_MONO
                var audioFormat = AudioFormat.ENCODING_PCM_16BIT
                var startIndex = 0

                // Robust WAV header parsing
                if (pcmBytes.size > 12 &&
                    pcmBytes[0] == 'R'.code.toByte() &&
                    pcmBytes[1] == 'I'.code.toByte() &&
                    pcmBytes[2] == 'F'.code.toByte() &&
                    pcmBytes[3] == 'F'.code.toByte() &&
                    pcmBytes[8] == 'W'.code.toByte() &&
                    pcmBytes[9] == 'A'.code.toByte() &&
                    pcmBytes[10] == 'V'.code.toByte() &&
                    pcmBytes[11] == 'E'.code.toByte()
                ) {
                    var i = 12
                    while (i < pcmBytes.size - 8) {
                        val chunkId = pcmBytes.decodeToString(i, i + 4)
                        val chunkSize = (pcmBytes[i + 4].toInt() and 0xFF) or
                            ((pcmBytes[i + 5].toInt() and 0xFF) shl 8) or
                            ((pcmBytes[i + 6].toInt() and 0xFF) shl 16) or
                            ((pcmBytes[i + 7].toInt() and 0xFF) shl 24)

                        if (chunkId == "fmt ") {
                            val channels = (pcmBytes[i + 10].toInt() and 0xFF) or ((pcmBytes[i + 11].toInt() and 0xFF) shl 8)
                            sampleRate = (pcmBytes[i + 12].toInt() and 0xFF) or
                                ((pcmBytes[i + 13].toInt() and 0xFF) shl 8) or
                                ((pcmBytes[i + 14].toInt() and 0xFF) shl 16) or
                                ((pcmBytes[i + 15].toInt() and 0xFF) shl 24)
                            channelConfig = if (channels == 2) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO

                            val bitsPerSample = (pcmBytes[i + 22].toInt() and 0xFF) or ((pcmBytes[i + 23].toInt() and 0xFF) shl 8)
                            audioFormat = when (bitsPerSample) {
                                8 -> AudioFormat.ENCODING_PCM_8BIT
                                16 -> AudioFormat.ENCODING_PCM_16BIT
                                32 -> AudioFormat.ENCODING_PCM_FLOAT
                                else -> AudioFormat.ENCODING_PCM_16BIT
                            }
                        } else if (chunkId == "data") {
                            startIndex = i + 8
                            break
                        }
                        i += 8 + (if (chunkSize < 0) 0 else chunkSize)
                        if (i < 0) break // Overflow safety
                    }
                }

                val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                val bufferSize = if (minBufferSize > 0) minBufferSize * 4 else 8192

                track = AudioTrack.Builder()
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

                audioTrack = track
                track.play()

                var offset = startIndex
                while (offset < pcmBytes.size && isPlaying) {
                    val sizeToWrite = minOf(bufferSize, pcmBytes.size - offset)
                    val written = track.write(pcmBytes, offset, sizeToWrite)
                    if (written <= 0) break
                    offset += written
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPlaying = false
                try {
                    track?.stop()
                    track?.release()
                } catch (e: Exception) {
                    // Ignore already released/stopped exceptions
                }
                if (audioTrack == track) {
                    audioTrack = null
                }
            }
        }.start()
    }

    actual fun stop() {
        isPlaying = false
        val track = audioTrack
        audioTrack = null
        try {
            track?.pause()
            track?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun isPlaying(): Boolean = isPlaying
}
