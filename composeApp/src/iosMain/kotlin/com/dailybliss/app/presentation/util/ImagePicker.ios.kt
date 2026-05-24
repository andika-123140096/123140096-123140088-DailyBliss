package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLauncher(onResult: (List<ByteArray>) -> Unit): ImagePickerLauncher = remember {
    object : ImagePickerLauncher {
        override fun launch() {
            // Not implemented on iOS yet
        }
    }
}

class IosFileStorage : FileStorage {
    override suspend fun saveImage(bytes: ByteArray): String? = null
    override suspend fun loadImage(path: String): ByteArray? = null
}
