package com.dailybliss.app.core.util

import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MomentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class BackgroundAIProcessor(
    private val aiRepository: AIRepository,
    private val momentRepository: MomentRepository,
    private val applicationScope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun processMoment(momentId: Long) {
        applicationScope.launch {
            val moment = momentRepository.getMomentById(momentId).first() ?: return@launch
            // Strip HTML tags for AI processing
            val allText = moment.content.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()

            if (allText.isBlank()) return@launch

            // Perform AI Analysis
            val moodResult = aiRepository.analyzeMood(allText)
            val tagsResult = if (moment.tags.isEmpty()) aiRepository.generateTags(allText) else null

            if (moodResult != null || !tagsResult.isNullOrEmpty()) {
                val updatedMoment = moment.copy(
                    mood = moodResult?.let { "${it.emoji} ${it.mood}" } ?: moment.mood,
                    tags = tagsResult ?: moment.tags
                )
                momentRepository.updateMoment(updatedMoment)
            }
        }
    }
}
