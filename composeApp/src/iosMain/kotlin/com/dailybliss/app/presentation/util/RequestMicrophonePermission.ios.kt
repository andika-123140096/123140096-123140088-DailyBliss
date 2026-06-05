package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.AVFoundation.AVAudioSession
import platform.AVFoundation.AVAudioSessionRecordPermissionDenied
import platform.AVFoundation.AVAudioSessionRecordPermissionGranted
import platform.AVFoundation.AVAudioSessionRecordPermissionUndetermined
import platform.AVFoundation.requestRecordPermission

@Composable
actual fun MicrophonePermissionEffect(
    onPermissionResult: (Boolean) -> Unit,
) {
    LaunchedEffect(Unit) {
        val session = AVAudioSession.sharedInstance()
        when (session.recordPermission()) {
            AVAudioSessionRecordPermissionGranted -> onPermissionResult(true)
            AVAudioSessionRecordPermissionDenied -> onPermissionResult(false)
            AVAudioSessionRecordPermissionUndetermined -> {
                session.requestRecordPermission { granted ->
                    onPermissionResult(granted)
                }
            }
        }
    }
}
