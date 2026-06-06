package com.dailybliss.app.domain.model

data class MoodStatistics(
    val moodDistribution: Map<String, Int>,
    val weeklyConsistency: List<Boolean>, // Last 7 days, true if at least one moment created
    val totalMoments: Int,
    val dominantMood: String?,
)
