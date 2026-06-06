package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable

/**
 * Composable effect to request notification permissions.
 */
@Composable
expect fun NotificationPermissionEffect(
    onPermissionResult: (Boolean) -> Unit,
)
