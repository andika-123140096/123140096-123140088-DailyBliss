package com.dailybliss.app.presentation.util

import kotlinx.cinterop.*
import platform.AVFAudio.*
import platform.Foundation.*
import platform.posix.memcpy

actual class AudioPlayer actual constructor() {
    private var engine: AVAudioEngine? = null
    private var playerNode: AVAudioPlayerNode? = null

    private var isPlaying = false

    actual fun play(pcmBytes: ByteArray) {
        stop()

        try {
            engine = AVAudioEngine()
            playerNode = AVAudioPlayerNode()

            var sampleRate = 24000.0
            var channels = 1u
            var startIndex = 0

            // Robust WAV header parsing
            if (pcmBytes.size > 12 &&
                pcmBytes[0] == 'R'.code.toByte() && pcmBytes[1] == 'I'.code.toByte() &&
                pcmBytes[2] == 'F'.code.toByte() && pcmBytes[3] == 'F'.code.toByte() &&
                pcmBytes[8] == 'W'.code.toByte() && pcmBytes[9] == 'A'.code.toByte() &&
                pcmBytes[10] == 'V'.code.toByte() && pcmBytes[11] == 'E'.code.toByte()
            ) {
                var i = 12
                while (i < pcmBytes.size - 8) {
                    val chunkId = pcmBytes.decodeToString(i, i + 4)
                    val chunkSize = (pcmBytes[i + 4].toInt() and 0xFF) or
                            ((pcmBytes[i + 5].toInt() and 0xFF) shl 8) or
                            ((pcmBytes[i + 6].toInt() and 0xFF) shl 16) or
                            ((pcmBytes[i + 7].toInt() and 0xFF) shl 24)

                    if (chunkId == "fmt ") {
                        channels = ((pcmBytes[i + 10].toInt() and 0xFF) or ((pcmBytes[i + 11].toInt() and 0xFF) shl 8)).toUInt()
                        sampleRate = ((pcmBytes[i + 12].toInt() and 0xFF) or
                                ((pcmBytes[i + 13].toInt() and 0xFF) shl 8) or
                                ((pcmBytes[i + 14].toInt() and 0xFF) shl 16) or
                                ((pcmBytes[i + 15].toInt() and 0xFF) shl 24)).toDouble()
                    } else if (chunkId == "data") {
                        startIndex = i + 8
                        break
                    }
                    i += 8 + (if (chunkSize < 0) 0 else chunkSize)
                    if (i < 0) break
                }
            } else if (pcmBytes.size > 44 &&
                pcmBytes[0] == 'R'.code.toByte() && pcmBytes[1] == 'I'.code.toByte() &&
                pcmBytes[2] == 'F'.code.toByte() && pcmBytes[3] == 'F'.code.toByte()) {
                startIndex = 44
            }

            val audioFormat = AVAudioFormat(
                commonFormat = AVAudioPCMFormatInt16,
                sampleRate = sampleRate,
                channels = channels,
                interleaved = false,
            )

            engine?.attachNode(playerNode!!)
            engine?.connect(playerNode!!, to = engine?.mainMixerNode!!, format = audioFormat)

            val actualDataSize = pcmBytes.size - startIndex

            // Calculate number of frames. PCM 16-bit Mono => 2 bytes per frame
            val frameCount = (actualDataSize / (2 * channels.toInt())).toUInt()
            val buffer = AVAudioPCMBuffer(pcmFormat = audioFormat!!, frameCapacity = frameCount)
            buffer!!.frameLength = frameCount

            // Copy data to buffer
            val audioBufferList = buffer.audioBufferList.pointed
            val dataPointer = audioBufferList.mBuffers.mData
            if (dataPointer != null) {
                pcmBytes.usePinned { pinned ->
                    memcpy(dataPointer, pinned.addressOf(startIndex), actualDataSize.convert())
                }
            }

            engine?.prepare()
            val error = alloc<ObjCObjectVar<NSError?>>()
            if (!engine!!.startAndReturnError(error.ptr)) {
                println("Failed to start AVAudioEngine: ${error.value}")
                return
            }

            playerNode?.scheduleBuffer(buffer, completionHandler = {
                isPlaying = false
            })
            playerNode?.play()
            isPlaying = true
        } catch (e: Exception) {
            println("Exception playing audio on iOS: ${e.message}")
            isPlaying = false
        }
    }

    actual fun stop() {
        if (isPlaying) {
            playerNode?.stop()
            engine?.stop()
            isPlaying = false
        }
        playerNode = null
        engine = null
    }

    actual fun isPlaying(): Boolean = isPlaying
}
