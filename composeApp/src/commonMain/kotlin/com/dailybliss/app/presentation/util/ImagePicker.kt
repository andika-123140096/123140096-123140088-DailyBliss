package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onResult: (List<ByteArray>) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}

interface FileStorage {
    suspend fun saveImage(bytes: ByteArray): String?
    suspend fun loadImage(path: String): ByteArray?
}
