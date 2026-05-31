package com.dailybliss.app.domain.model

data class ChatMessage(
    val role: String,
    val text: String,
    val imageBytes: ByteArray? = null,
    val imagePath: String? = null,
    val isError: Boolean = false,
)
