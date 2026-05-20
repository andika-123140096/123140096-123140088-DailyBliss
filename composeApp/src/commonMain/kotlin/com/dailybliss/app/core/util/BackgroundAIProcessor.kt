package com.dailybliss.app.core.util

import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MomentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BackgroundAIProcessor(
    private val aiRepository: AIRepository,
    private val momentRepository: MomentRepository,
    private val applicationScope: CoroutineScope,
) {
    private val activeJobs = mutableMapOf<Long, Job>()
    private val jobMutex = Mutex()

    fun processMoment(momentId: Long, force: Boolean = false) {
        applicationScope.launch {
            jobMutex.withLock {
                // Cancel existing job for this moment if it's still running
                activeJobs[momentId]?.cancel()

                val job =
                    applicationScope.launch {
                        try {
                            val moment = momentRepository.getMomentById(momentId).first() ?: return@launch

                            // Strip HTML tags for AI processing
                            val allText =
                                moment.content
                                    .replace(Regex("<[^>]*>"), " ")
                                    .replace(Regex("\\s+"), " ")
                                    .trim()

                            if (allText.isBlank()) return@launch

                            // Only process if tags are empty or if forced (e.g. content changed significantly)
                            if (!force && moment.tags.isNotEmpty() && !moment.mood.isNullOrBlank()) {
                                return@launch
                            }

                            // Perform AI Analysis
                            val moodResult = aiRepository.analyzeMood(allText)
                            val tagsResult = aiRepository.generateTags(allText)

                            if (moodResult != null || tagsResult.isNotEmpty()) {
                                val updatedMoment =
                                    moment.copy(
                                        mood = moodResult?.let { "${it.emoji} ${it.mood}" } ?: moment.mood,
                                        tags = if (tagsResult.isNotEmpty()) tagsResult else moment.tags,
                                    )
                                momentRepository.updateMoment(updatedMoment)
                            }
                        } catch (e: Exception) {
                            if (e !is kotlinx.coroutines.CancellationException) {
                                println("Error in BackgroundAIProcessor for moment $momentId: ${e.message}")
                            }
                        } finally {
                            jobMutex.withLock {
                                if (activeJobs[momentId] == coroutineContext[Job]) {
                                    activeJobs.remove(momentId)
                                }
                            }
                        }
                    }
                activeJobs[momentId] = job
            }
        }
    }
}
