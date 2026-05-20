package com.dailybliss.app.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Moment(
    val id: Long = 0,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    val preview: String
        get() {
            if (content.isEmpty()) return ""

            // HTML support - strip tags for preview
            val stripped = content.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
            return if (stripped.length > 120) "${stripped.take(120)}..." else stripped
        }

}

