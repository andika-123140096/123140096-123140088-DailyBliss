package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable

/**
 * Composable untuk meminta izin lokasi dari user.
 */
@Composable
expect fun LocationPermissionEffect(
    onPermissionResult: (Boolean) -> Unit
)
