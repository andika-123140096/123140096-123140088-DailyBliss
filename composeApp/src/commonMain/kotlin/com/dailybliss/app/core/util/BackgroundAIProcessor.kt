package com.dailybliss.app.core.util

import com.dailybliss.app.core.util.dateStr
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MomentRepository
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Background AI Processor interface for background tasks like mood analysis and summarization.
 */
interface BackgroundAIProcessor {
    val isProcessing: StateFlow<Boolean>
    fun processMoment(momentId: Long, force: Boolean = false)
    fun updateGlobalSummary()
}

/**
 * Implementation of BackgroundAIProcessor that uses CoroutineScope to run tasks in the background.
 */
class BackgroundAIProcessorImpl(
    private val aiRepository: AIRepository,
    private val momentRepository: MomentRepository,
    private val userPreferences: UserPreferences,
    private val fileStorage: FileStorage,
    private val applicationScope: CoroutineScope,
) : BackgroundAIProcessor {
    private val activeJobs = mutableMapOf<Long, Job>()
    private var globalSummaryJob: Job? = null
    private var isGlobalSummaryRunning = false
    private val jobMutex = Mutex()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private suspend fun updateProcessingState() {
        jobMutex.withLock {
            _isProcessing.value = activeJobs.isNotEmpty() || isGlobalSummaryRunning
        }
    }

    override fun processMoment(momentId: Long, force: Boolean) {
        applicationScope.launch {
            jobMutex.withLock {
                activeJobs[momentId]?.cancel()
                
                val job = applicationScope.launch {
                    try {
                        val moment = momentRepository.getMomentById(momentId).first() ?: return@launch
                        val allText = moment.content.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
                        if (allText.isBlank()) return@launch
                        if (!force && moment.tags.isNotEmpty() && !moment.mood.isNullOrBlank()) return@launch

                        val imageBytes = moment.imageUrl?.let { fileStorage.loadImage(it) }
                        val moodResult = aiRepository.analyzeMood(content = allText, imageBytes = imageBytes)
                        val tagsResult = aiRepository.generateTags(content = allText, imageBytes = imageBytes)

                        if (moodResult != null || tagsResult.isNotEmpty()) {
                            val updatedMoment = moment.copy(
                                mood = moodResult?.let { "${it.emoji} ${it.mood}" } ?: moment.mood,
                                tags = if (tagsResult.isNotEmpty()) tagsResult else moment.tags,
                            )
                            momentRepository.updateMoment(updatedMoment)
                        }
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        println("Error in BackgroundAIProcessor for moment $momentId: ${e.message}")
                    } finally {
                        jobMutex.withLock {
                            if (activeJobs[momentId] == coroutineContext[Job]) {
                                activeJobs.remove(momentId)
                            }
                        }
                        updateProcessingState()
                    }
                }
                activeJobs[momentId] = job
            }
            updateProcessingState()
        }
    }

    override fun updateGlobalSummary() {
        applicationScope.launch {
            jobMutex.withLock {
                globalSummaryJob?.cancel()
                isGlobalSummaryRunning = true
                
                val job = applicationScope.launch {
                    try {
                        val allMoments = momentRepository.getAllMoments().first()
                        if (allMoments.isEmpty()) {
                            userPreferences.setJournalSummary("")
                            return@launch
                        }

                        val recentMoments = allMoments.take(50)
                        val combinedText = recentMoments.joinToString("\n---\n") { moment ->
                            val cleanContent = moment.content.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
                            "Tanggal: ${moment.createdAt.dateStr}\nKonten: $cleanContent"
                        }
                        if (combinedText.isBlank()) return@launch

                        val summary = aiRepository.generateGlobalSummary(combinedText)
                        if (summary != null) userPreferences.setJournalSummary(summary)

                        val dailyInsight = aiRepository.generateDailyInsight(combinedText)
                        if (dailyInsight != null) userPreferences.setDailyInsight(dailyInsight)
                    } catch (e: Exception) {
                        println("Error updating global summary: ${e.message}")
                    } finally {
                        jobMutex.withLock {
                            isGlobalSummaryRunning = false
                        }
                        updateProcessingState()
                    }
                }
                globalSummaryJob = job
            }
            updateProcessingState()
        }
    }
}
