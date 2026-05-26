package com.dailybliss.app.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Journal : Route

    @Serializable
    data class CreateMoment(val momentId: Long? = null) : Route

    @Serializable
    data class MomentDetail(val momentId: Long) : Route

    @Serializable
    data object News : Route

    @Serializable
    data object AIAssistant : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class DailyMoments(val dateStr: String) : Route
}

interface NavigationActions {
    fun navigateToHome()

    fun navigateToNews()

    fun navigateToJournal()

    fun navigateToCalendar()

    fun navigateToDailyMoments(dateStr: String)

    fun navigateToCreateMoment(momentId: Long? = null)

    fun navigateToMomentDetail(momentId: Long)

    fun navigateToAIAssistant()

    fun navigateToSettings()

    fun navigateBack()
}
