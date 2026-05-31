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

            val audioFormat = AVAudioFormat(
                commonFormat = AVAudioPCMFormatInt16,
                sampleRate = 24000.0,
                channels = 1u,
                interleaved = false,
            )

            engine?.attachNode(playerNode!!)
            engine?.connect(playerNode!!, to = engine?.mainMixerNode!!, format = audioFormat)

            // Skip WAV header if present (44 bytes)
            var startIndex = 0
            if (pcmBytes.size > 44 && 
                pcmBytes[0] == 'R'.code.toByte() && pcmBytes[1] == 'I'.code.toByte() &&
                pcmBytes[2] == 'F'.code.toByte() && pcmBytes[3] == 'F'.code.toByte()) {
                startIndex = 44
            }
            val actualDataSize = pcmBytes.size - startIndex

            // Calculate number of frames. PCM 16-bit Mono => 2 bytes per frame
            val frameCount = (actualDataSize / 2).toUInt()
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
