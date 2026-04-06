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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.presentation.components.ChillieChatTopBar
import com.chillieman.chilliechat.presentation.navigation.AppNavigation
import com.chillieman.chilliechat.presentation.navigation.BlockedUsersRoute
import com.chillieman.chilliechat.presentation.navigation.ComplianceRoute
import com.chillieman.chilliechat.presentation.navigation.EventsRoute
import com.chillieman.chilliechat.presentation.navigation.SettingsRoute
import com.chillieman.chilliechat.presentation.navigation.EntriesRoute
import com.chillieman.chilliechat.presentation.navigation.ThreadsRoute
import com.chillieman.chilliechat.presentation.onboarding.OnboardingManager
import com.chillieman.chilliechat.presentation.onboarding.OnboardingStep
import com.chillieman.chilliechat.presentation.ui.theme.ChillieChatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var onboardingManager: OnboardingManager
    @Inject lateinit var agentPreferencesManager: AgentPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChillieChatTheme {
                ChillieChatApp(
                    onboardingManager = onboardingManager,
                    agentPreferencesManager = agentPreferencesManager,
                    onFinishActivity = { finish() }
                )
            }
        }
    }
}

@Composable
fun ChillieChatApp(
    onboardingManager: OnboardingManager,
    agentPreferencesManager: AgentPreferencesManager,
    onFinishActivity: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val onboardingStep by onboardingManager.currentStep.collectAsStateWithLifecycle()
    val isOnboarding by onboardingManager.isActive.collectAsStateWithLifecycle()
    val prefs by agentPreferencesManager.agentPreferences.collectAsStateWithLifecycle(
        initialValue = null
    )

    LaunchedEffect(Unit) {
        onboardingManager.initialize()
    }

    val isComplianceScreen = currentRoute?.contains("ComplianceRoute") == true
    val isSettingsScreen = currentRoute?.contains("SettingsRoute") == true
    val isThreadsScreen = currentRoute?.contains("ThreadsRoute") == true
    val isEntriesScreen = currentRoute?.contains("EntriesRoute") == true
    val isBlockedUsersScreen = currentRoute?.contains("BlockedUsersRoute") == true
    val isRootScreen = currentRoute?.contains("EventsRoute") == true || currentRoute == null

    val title = when {
        isComplianceScreen -> "ChillieChat"
        isSettingsScreen -> "Settings"
        isBlockedUsersScreen -> "Blocked Users"
        isEntriesScreen -> runCatching {
            navBackStackEntry?.toRoute<EntriesRoute>()?.threadTitle
        }.getOrNull() ?: "Chat"
        isThreadsScreen -> runCatching {
            navBackStackEntry?.toRoute<ThreadsRoute>()?.eventTitle
        }.getOrNull() ?: "Threads"
        else -> "ChillieChat"
    }

    // Wait for prefs to load before rendering
    val currentPrefs = prefs ?: return

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .union(WindowInsets.ime),
        topBar = {
            if (!isComplianceScreen) {
                ChillieChatTopBar(
                    title = title,
                    showSettingsIcon = !isSettingsScreen && !isBlockedUsersScreen,
                    showBackButton = !isRootScreen,
                    highlightSettings = isOnboarding && onboardingStep == OnboardingStep.SPOTLIGHT_SETTINGS,
                    onSettingsClick = {
                        if (isOnboarding && onboardingStep == OnboardingStep.SPOTLIGHT_SETTINGS) {
                            onboardingManager.advanceStep()
                        }
                        navController.navigate(SettingsRoute) {
                            launchSingleTop = true
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            hasAgreedToTerms = currentPrefs.hasAgreedToTerms,
            onFinishActivity = onFinishActivity,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
