package com.dailybliss.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dailybliss.app.presentation.screens.addnote.CreateMomentScreen
import com.dailybliss.app.presentation.screens.ai.AIAssistantScreen
import com.dailybliss.app.presentation.screens.calendar.CalendarScreen
import com.dailybliss.app.presentation.screens.detail.MomentDetailScreen
import com.dailybliss.app.presentation.screens.home.HomeScreen
import com.dailybliss.app.presentation.screens.home.JournalScreen
import com.dailybliss.app.presentation.screens.settings.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), modifier: Modifier = Modifier) {
    val actions = remember(navController) { NavigationActionsImpl(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val hideBottomBarScreens = listOf(
        Route.CreateMoment::class,
        Route.MomentDetail::class,
        Route.Settings::class,
    )

    val isAIAssistant = currentDestination?.hierarchy?.any { it.hasRoute(Route.AIAssistant::class) } == true

    val showBottomBar = hideBottomBarScreens.none { route ->
        currentDestination?.hierarchy?.any { it.hasRoute(route) } == true
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = Color.Gray,
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(Route.Home::class) } == true,
                        onClick = { actions.navigateToHome() },
                        icon = {
                            Icon(
                                if (currentDestination?.hierarchy?.any { it.hasRoute(Route.Home::class) } ==
                                    true
                                ) {
                                    Icons.Filled.Home
                                } else {
                                    Icons.Outlined.Home
                                },
                                contentDescription = "Home",
                            )
                        },
                        label = { Text("Beranda") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                        ),
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(Route.Journal::class) } == true,
                        onClick = { actions.navigateToJournal() },
                        icon = {
                            Icon(
                                if (currentDestination?.hierarchy?.any { it.hasRoute(Route.Journal::class) } ==
                                    true
                                ) {
                                    Icons.AutoMirrored.Filled.MenuBook
                                } else {
                                    Icons.AutoMirrored.Outlined.MenuBook
                                },
                                contentDescription = "Journal",
                            )
                        },
                        label = { Text("Jurnal") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                        ),
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(Route.Calendar::class) } == true,
                        onClick = { actions.navigateToCalendar() },
                        icon = {
                            Icon(
                                if (currentDestination?.hierarchy?.any { it.hasRoute(Route.Calendar::class) } ==
                                    true
                                ) {
                                    Icons.Default.DateRange
                                } else {
                                    Icons.Outlined.DateRange
                                },
                                contentDescription = "Calendar",
                            )
                        },
                        label = { Text("Kalender") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                        ),
                    )

                    NavigationBarItem(
                        selected = isAIAssistant,
                        onClick = { actions.navigateToAIAssistant() },
                        icon = {
                            Icon(
                                if (isAIAssistant) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Assistant",
                            )
                        },
                        label = { Text("Asisten AI") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                        ),
                    )
                }
            }
        },
        content = { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Route.Home,
                modifier = modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300),
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300),
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300),
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300),
                    )
                },
            ) {
                composable<Route.Home> {
                    HomeScreen(
                        onNavigateToCreateMoment = { actions.navigateToCreateMoment() },
                        onNavigateToDetail = { id -> actions.navigateToMomentDetail(id) },
                        onNavigateToSettings = { actions.navigateToSettings() },
                    )
                }

                composable<Route.Journal> {
                    JournalScreen(
                        onNavigateToCreateMoment = { actions.navigateToCreateMoment() },
                        onNavigateToMomentDetail = { id -> actions.navigateToMomentDetail(id) },
                    )
                }

                composable<Route.Calendar> {
                    CalendarScreen(
                        onNavigateToMomentDetail = { id -> actions.navigateToMomentDetail(id) },
                    )
                }

                composable<Route.CreateMoment> {
                    CreateMomentScreen(
                        onNavigateBack = { actions.navigateBack() },
                    )
                }

                composable<Route.MomentDetail> { backStackEntry ->
                    val route: Route.MomentDetail = backStackEntry.toRoute()
                    MomentDetailScreen(
                        momentId = route.momentId,
                        onNavigateBack = { actions.navigateBack() },
                    )
                }

                composable<Route.AIAssistant> {
                    AIAssistantScreen(
                        onNavigateBack = { actions.navigateBack() },
                    )
                }

                composable<Route.Settings> {
                    SettingsScreen(
                        onNavigateBack = { actions.navigateBack() },
                    )
                }
            }
        },
    )
}

private class NavigationActionsImpl(private val navController: NavHostController) : NavigationActions {
    override fun navigateToHome() {
        navController.navigate(Route.Home) {
            popUpTo(Route.Home) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToJournal() {
        navController.navigate(Route.Journal) {
            popUpTo(Route.Home) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToCalendar() {
        navController.navigate(Route.Calendar) {
            popUpTo(Route.Home) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToCreateMoment(momentId: Long?) {
        navController.navigate(Route.CreateMoment(momentId))
    }

    override fun navigateToMomentDetail(momentId: Long) {
        navController.navigate(Route.MomentDetail(momentId))
    }

    override fun navigateToAIAssistant() {
        navController.navigate(Route.AIAssistant) {
            popUpTo(Route.Home) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToSettings() {
        navController.navigate(Route.Settings)
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}
