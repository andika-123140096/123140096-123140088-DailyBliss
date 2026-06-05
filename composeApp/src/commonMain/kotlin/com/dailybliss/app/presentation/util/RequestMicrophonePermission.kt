package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun MicrophonePermissionEffect(
    onPermissionResult: (Boolean) -> Unit,
)
