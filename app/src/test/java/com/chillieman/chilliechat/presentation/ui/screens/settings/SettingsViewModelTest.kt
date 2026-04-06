package com.chillieman.chilliechat.presentation.ui.screens.settings

import app.cash.turbine.test
import com.chillieman.chilliechat.MainDispatcherRule
import com.chillieman.chilliechat.data.local.AgentPreferences
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.usecase.SecureAgentUseCase
import com.chillieman.chilliechat.presentation.onboarding.OnboardingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val secureAgentUseCase = mockk<SecureAgentUseCase>()
    private val prefsManager = mockk<AgentPreferencesManager>(relaxed = true)
    private val onboardingManager = mockk<OnboardingManager>(relaxed = true)

    @Test
    fun `loads saved agent on init`() = runTest {
        every { prefsManager.agentPreferences } returns flowOf(
            AgentPreferences(agentId = 1, agentName = "Bot", agentType = "PUBLIC", agentSecret = null)
        )

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            val items = mutableListOf<SettingsUiState>()
            items.add(awaitItem())
            if (items.last() is SettingsUiState.Loading) items.add(awaitItem())
            val success = items.last() as SettingsUiState.Success
            assertEquals("Bot", success.currentAgent?.name)
            assertEquals("Bot", success.nameInput)
        }
    }

    @Test
    fun `shows empty state when no saved agent`() = runTest {
        every { prefsManager.agentPreferences } returns flowOf(AgentPreferences())

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            val items = mutableListOf<SettingsUiState>()
            items.add(awaitItem())
            if (items.last() is SettingsUiState.Loading) items.add(awaitItem())
            val success = items.last() as SettingsUiState.Success
            assertNull(success.currentAgent)
            assertEquals("", success.nameInput)
        }
    }

    @Test
    fun `onNameChanged updates name input`() = runTest {
        every { prefsManager.agentPreferences } returns flowOf(AgentPreferences())

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is SettingsUiState.Loading) state = awaitItem()

            viewModel.onNameChanged("NewName")
            val updated = awaitItem() as SettingsUiState.Success
            assertEquals("NewName", updated.nameInput)
        }
    }

    @Test
    fun `login without secret calls securePublic and saves agent`() = runTest {
        val agent = Agent(id = 5, name = "TestBot", type = "PUBLIC", capabilities = null)
        every { prefsManager.agentPreferences } returns flowOf(AgentPreferences())
        coEvery { secureAgentUseCase.securePublic("TestBot") } returns agent
        coEvery { prefsManager.saveAgent(any(), any(), any(), any()) } returns Unit

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is SettingsUiState.Loading) state = awaitItem()

            viewModel.onNameChanged("TestBot")
            awaitItem() // nameInput updated

            viewModel.login()

            // Consume states until we get the final Success with agent
            val finalState = expectMostRecentItem()
            assertTrue(finalState is SettingsUiState.Success || finalState is SettingsUiState.Error)
            if (finalState is SettingsUiState.Success) {
                assertEquals("TestBot", finalState.currentAgent?.name)
            }
        }

        coVerify { prefsManager.saveAgent(5, "TestBot", "PUBLIC", null) }
    }

    @Test
    fun `login with secret calls securePrivate and saves agent`() = runTest {
        val agent = Agent(id = 6, name = "SecureBot", type = "PRIVATE", capabilities = null)
        every { prefsManager.agentPreferences } returns flowOf(AgentPreferences())
        coEvery { secureAgentUseCase.securePrivate("SecureBot", "mySecret") } returns agent
        coEvery { prefsManager.saveAgent(any(), any(), any(), any()) } returns Unit

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is SettingsUiState.Loading) state = awaitItem()

            viewModel.onNameChanged("SecureBot")
            awaitItem()
            viewModel.onSecretChanged("mySecret")
            awaitItem()

            viewModel.login()

            val finalState = expectMostRecentItem()
            assertTrue(finalState is SettingsUiState.Success || finalState is SettingsUiState.Error)
            if (finalState is SettingsUiState.Success) {
                assertEquals("SecureBot", finalState.currentAgent?.name)
            }
        }

        coVerify { prefsManager.saveAgent(6, "SecureBot", "PRIVATE", "mySecret") }
    }

    @Test
    fun `login shows error on failure`() = runTest {
        every { prefsManager.agentPreferences } returns flowOf(AgentPreferences())
        coEvery { secureAgentUseCase.securePublic(any()) } throws RuntimeException("Server error")

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is SettingsUiState.Loading) state = awaitItem()

            viewModel.onNameChanged("Fail")
            awaitItem()

            viewModel.login()

            val error = expectMostRecentItem()
            assertTrue(error is SettingsUiState.Error)
            assertTrue((error as SettingsUiState.Error).message.contains("Server error"))
        }
    }

    @Test
    fun `logout clears preferences`() = runTest {
        every { prefsManager.agentPreferences } returns flowOf(
            AgentPreferences(agentId = 1, agentName = "Bot", agentType = "PUBLIC")
        )
        coEvery { prefsManager.clearAgent() } returns Unit

        val viewModel = SettingsViewModel(secureAgentUseCase, prefsManager, onboardingManager)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is SettingsUiState.Loading) state = awaitItem()

            viewModel.logout()

            val loggedOut = expectMostRecentItem() as SettingsUiState.Success
            assertNull(loggedOut.currentAgent)
        }

        coVerify { prefsManager.clearAgent() }
    }
}
