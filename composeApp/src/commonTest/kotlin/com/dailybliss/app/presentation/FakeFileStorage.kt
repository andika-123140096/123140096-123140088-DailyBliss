package com.dailybliss.app.presentation

import com.dailybliss.app.presentation.util.FileStorage

class FakeFileStorage : FileStorage {
    private val storage = mutableMapOf<String, ByteArray>()
    var onSaveImageReturn: String? = null

    override suspend fun saveImage(bytes: ByteArray): String? {
        if (onSaveImageReturn != null) return onSaveImageReturn

        val path = "path_${bytes.size}"
        storage[path] = bytes
        return path
    }

    override suspend fun loadImage(path: String): ByteArray? = storage[path]
}
