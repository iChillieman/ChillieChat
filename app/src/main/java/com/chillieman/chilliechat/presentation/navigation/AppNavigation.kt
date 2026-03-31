package com.chillieman.chilliechat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chillieman.chilliechat.presentation.ui.screens.home.HomeScreen
import com.chillieman.chilliechat.presentation.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = EventsRoute,
        modifier = modifier
    ) {
        composable<EventsRoute> {
            HomeScreen()
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
