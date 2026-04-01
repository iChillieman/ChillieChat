package com.chillieman.chilliechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.chillieman.chilliechat.presentation.components.ChillieChatTopBar
import com.chillieman.chilliechat.presentation.navigation.AppNavigation
import com.chillieman.chilliechat.presentation.navigation.EventsRoute
import com.chillieman.chilliechat.presentation.navigation.SettingsRoute
import com.chillieman.chilliechat.presentation.navigation.EntriesRoute
import com.chillieman.chilliechat.presentation.navigation.ThreadsRoute
import com.chillieman.chilliechat.presentation.ui.theme.ChillieChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChillieChatTheme {
                ChillieChatApp()
            }
        }
    }
}

@Composable
fun ChillieChatApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isSettingsScreen = currentRoute?.contains("SettingsRoute") == true
    val isThreadsScreen = currentRoute?.contains("ThreadsRoute") == true
    val isEntriesScreen = currentRoute?.contains("EntriesRoute") == true
    val isRootScreen = currentRoute?.contains("EventsRoute") == true || currentRoute == null

    val title = when {
        isSettingsScreen -> "Settings"
        isEntriesScreen -> runCatching {
            navBackStackEntry?.toRoute<EntriesRoute>()?.threadTitle
        }.getOrNull() ?: "Chat"
        isThreadsScreen -> runCatching {
            navBackStackEntry?.toRoute<ThreadsRoute>()?.eventTitle
        }.getOrNull() ?: "Threads"
        else -> "ChillieChat"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .union(WindowInsets.ime),
        topBar = {
            ChillieChatTopBar(
                title = title,
                showSettingsIcon = !isSettingsScreen,
                showBackButton = !isRootScreen,
                onSettingsClick = {
                    navController.navigate(SettingsRoute) {
                        launchSingleTop = true
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
