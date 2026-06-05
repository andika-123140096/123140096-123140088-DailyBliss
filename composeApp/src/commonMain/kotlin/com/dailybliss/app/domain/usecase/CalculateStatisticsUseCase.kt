package com.dailybliss.app.domain.usecase

import com.dailybliss.app.domain.model.MoodStatistics
import com.dailybliss.app.domain.repository.MomentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*

class CalculateStatisticsUseCase(private val repository: MomentRepository) {
    operator fun invoke(): Flow<MoodStatistics> = repository.getAllMoments().map { moments ->
        val moodDistribution = moments
            .filter { !it.mood.isNullOrBlank() }
            .groupBy { it.mood!! }
            .mapValues { it.value.size }

        val dominantMood = moodDistribution.maxByOrNull { it.value }?.key

        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date

        val weeklyConsistency = (0..6).map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            moments.any {
                it.createdAt.toLocalDateTime(tz).date == date
            }
        }.reversed()

        MoodStatistics(
            moodDistribution = moodDistribution,
            weeklyConsistency = weeklyConsistency,
            totalMoments = moments.size,
            dominantMood = dominantMood,
        )
    }
}
