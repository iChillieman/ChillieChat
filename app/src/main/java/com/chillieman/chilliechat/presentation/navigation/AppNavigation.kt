package com.chillieman.chilliechat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chillieman.chilliechat.presentation.ui.screens.blocked.BlockedUsersScreen
import com.chillieman.chilliechat.presentation.ui.screens.compliance.ComplianceScreen
import com.chillieman.chilliechat.presentation.ui.screens.entries.EntriesScreen
import com.chillieman.chilliechat.presentation.ui.screens.events.EventsScreen
import com.chillieman.chilliechat.presentation.ui.screens.settings.SettingsScreen
import com.chillieman.chilliechat.presentation.ui.screens.threads.ThreadsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    hasAgreedToTerms: Boolean,
    onFinishActivity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (hasAgreedToTerms) EventsRoute else ComplianceRoute,
        modifier = modifier
    ) {
        composable<ComplianceRoute> {
            ComplianceScreen(
                onComplete = {
                    navController.navigate(EventsRoute) {
                        popUpTo(ComplianceRoute) { inclusive = true }
                    }
                },
                onCancel = onFinishActivity
            )
        }

        composable<EventsRoute> {
            EventsScreen(
                onNavigateToThreads = { eventId, eventTitle ->
                    navController.navigate(ThreadsRoute(eventId, eventTitle))
                },
                onNavigateToDaeThread = {
                    navController.navigate(EntriesRoute(threadId = 4, threadTitle = "Dae's Corner", eventEndTime = null))
                }
            )
        }

        composable<ThreadsRoute> {
            ThreadsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntries = { threadId, threadTitle, eventEndTime ->
                    navController.navigate(EntriesRoute(threadId, threadTitle, eventEndTime))
                }
            )
        }

        composable<EntriesRoute> {
            EntriesScreen()
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBlockedUsers = {
                    navController.navigate(BlockedUsersRoute)
                }
            )
        }

        composable<BlockedUsersRoute> {
            BlockedUsersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
