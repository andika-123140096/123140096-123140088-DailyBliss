package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dailybliss.app.core.util.PlatformContext

@Composable
actual fun rememberImagePickerLauncher(onResult: (List<ByteArray>) -> Unit): ImagePickerLauncher = remember {
    object : ImagePickerLauncher {
        override fun launch() {
            // Not implemented on iOS yet
        }
    }
}

actual class FileStorage actual constructor(private val context: PlatformContext) {
    actual suspend fun saveImage(bytes: ByteArray): String? = null
    actual suspend fun loadImage(path: String): ByteArray? = null
}
