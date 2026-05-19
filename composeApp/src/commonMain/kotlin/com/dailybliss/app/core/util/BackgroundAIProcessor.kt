package com.dailybliss.app.core.util

import com.dailybliss.app.domain.model.ContentBlock
import com.dailybliss.app.domain.model.MomentContent
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
            
            val contentBlocks = try {
                if (moment.content.startsWith("{\"blocks\":")) {
                    json.decodeFromString<MomentContent>(moment.content).blocks
                } else if (moment.content.contains("<") || moment.content.isNotEmpty()) {
                    MomentContent.fromHtml(moment.content).blocks
                } else {
                    listOf(ContentBlock.Html(moment.content))
                }
            } catch (e: Exception) {
                listOf(ContentBlock.Html(moment.content))
            }

            val allText = contentBlocks.filterIsInstance<ContentBlock.Html>().joinToString("\n") { it.content }
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
