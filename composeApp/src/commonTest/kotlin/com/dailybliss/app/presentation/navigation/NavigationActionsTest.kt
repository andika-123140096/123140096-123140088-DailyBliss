package com.dailybliss.app.presentation.navigation

import androidx.navigation.NavHostController
import com.dailybliss.app.presentation.navigation.NavigationActionsImpl
import com.dailybliss.app.presentation.navigation.Route
import io.mockk.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class NavigationActionsTest {
    private lateinit var navController: NavHostController
    private lateinit var actions: NavigationActionsImpl

    @BeforeTest
    fun setup() {
        navController = mockk(relaxed = true)
        actions = NavigationActionsImpl(navController)
    }

    @Test
    fun `navigateToHome should navigate to Home route`() {
        actions.navigateToHome()
        verify { navController.navigate(Route.Home, any<androidx.navigation.NavOptionsBuilder.() -> Unit>()) }
    }

    @Test
    fun `navigateToJournal should navigate to Journal route`() {
        actions.navigateToJournal()
        verify { navController.navigate(Route.Journal, any<androidx.navigation.NavOptionsBuilder.() -> Unit>()) }
    }

    @Test
    fun `navigateToNews should navigate to News route`() {
        actions.navigateToNews()
        verify { navController.navigate(Route.News, any<androidx.navigation.NavOptionsBuilder.() -> Unit>()) }
    }

    @Test
    fun `navigateToCalendar should navigate to Calendar route`() {
        actions.navigateToCalendar()
        verify { navController.navigate(Route.Calendar, any<androidx.navigation.NavOptionsBuilder.() -> Unit>()) }
    }

    @Test
    fun `navigateToDailyMoments should navigate with dateStr`() {
        actions.navigateToDailyMoments("2024-01-01")
        verify { navController.navigate(Route.DailyMoments("2024-01-01")) }
    }

    @Test
    fun `navigateToCreateMoment should navigate with optional id`() {
        actions.navigateToCreateMoment(123L)
        verify { navController.navigate(Route.CreateMoment(123L)) }
    }

    @Test
    fun `navigateToMomentDetail should navigate with id`() {
        actions.navigateToMomentDetail(456L)
        verify { navController.navigate(Route.MomentDetail(456L)) }
    }

    @Test
    fun `navigateToAIAssistant should navigate to AIAssistant`() {
        actions.navigateToAIAssistant()
        verify { navController.navigate(Route.AIAssistant, any<androidx.navigation.NavOptionsBuilder.() -> Unit>()) }
    }

    @Test
    fun `navigateToSettings should navigate to Settings`() {
        actions.navigateToSettings()
        verify { navController.navigate(Route.Settings) }
    }

    @Test
    fun `navigateToChatHistory should navigate to ChatHistory`() {
        actions.navigateToChatHistory()
        verify { navController.navigate(Route.ChatHistory) }
    }

    @Test
    fun `navigateBack should popBackStack`() {
        actions.navigateBack()
        verify { navController.popBackStack() }
    }
}
