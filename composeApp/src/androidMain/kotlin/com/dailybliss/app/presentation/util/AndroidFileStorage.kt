package com.dailybliss.app.presentation.util

import com.dailybliss.app.core.util.AndroidPlatformContext
import com.dailybliss.app.core.util.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AndroidFileStorage(private val context: PlatformContext) : FileStorage {
    private val androidContext = (context as AndroidPlatformContext).androidContext

    override suspend fun saveImage(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "moment_${UUID.randomUUID()}.jpg"
            val file = File(androidContext.filesDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (ignore: Exception) {
            null
        }
    }

    override suspend fun loadImage(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = if (path.startsWith("/")) File(path) else File(androidContext.filesDir, path)
            if (file.exists()) file.readBytes() else null
        } catch (ignore: Exception) {
            null
        }
    }

    override suspend fun saveFile(bytes: ByteArray, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(androidContext.filesDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (ignore: Exception) {
            null
        }
    }

    override suspend fun loadFile(path: String): ByteArray? = loadImage(path)
}
