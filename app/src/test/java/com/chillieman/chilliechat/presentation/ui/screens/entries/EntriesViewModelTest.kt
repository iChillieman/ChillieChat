package com.chillieman.chilliechat.presentation.ui.screens.entries

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.chillieman.chilliechat.MainDispatcherRule
import com.chillieman.chilliechat.data.local.AgentPreferences
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import com.chillieman.chilliechat.data.remote.WebSocketManager
import com.chillieman.chilliechat.domain.usecase.GetEntriesUseCase
import com.chillieman.chilliechat.domain.usecase.SubmitEntryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testAgent = Agent(id = 1, name = "Bot", type = "PUBLIC", capabilities = null)
    private val testEntries = listOf(
        EntryWithAgent(id = 100, agentId = 1, threadId = 10, content = "Hello", timestamp = 500L, agent = testAgent),
        EntryWithAgent(id = 101, agentId = 2, threadId = 10, content = "World", timestamp = 600L,
            agent = Agent(id = 2, name = "Other", type = "PUBLIC", capabilities = null))
    )

    private fun createViewModel(
        getEntriesUseCase: GetEntriesUseCase = mockk(),
        submitEntryUseCase: SubmitEntryUseCase = mockk(relaxed = true),
        prefsManager: AgentPreferencesManager = mockk(),
        webSocketManager: WebSocketManager = mockk(relaxed = true)
    ): EntriesViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("threadId" to 10, "threadTitle" to "Test Chat"))
        return EntriesViewModel(savedStateHandle, getEntriesUseCase, submitEntryUseCase, prefsManager, webSocketManager)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val getEntries = mockk<GetEntriesUseCase>()
        val prefsManager = mockk<AgentPreferencesManager>()
        // Never-completing flow to keep it in Loading
        every { getEntries(10) } returns MutableStateFlow(emptyList())
        every { prefsManager.agentPreferences } returns MutableStateFlow(AgentPreferences())

        val viewModel = createViewModel(getEntriesUseCase = getEntries, prefsManager = prefsManager)

        // The initial value is Loading before stateIn starts collecting
        assertEquals(EntriesUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `emits Success when entries and prefs flow`() = runTest {
        val getEntries = mockk<GetEntriesUseCase>()
        val prefsManager = mockk<AgentPreferencesManager>()
        every { getEntries(10) } returns flowOf(testEntries)
        every { prefsManager.agentPreferences } returns flowOf(
            AgentPreferences(agentId = 1, agentName = "Bot")
        )

        val viewModel = createViewModel(getEntriesUseCase = getEntries, prefsManager = prefsManager)

        viewModel.uiState.test {
            val items = mutableListOf<EntriesUiState>()
            items.add(awaitItem())
            if (items.last() is EntriesUiState.Loading) {
                items.add(awaitItem())
            }
            val success = items.last() as EntriesUiState.Success
            assertEquals(10, success.threadId)
            assertEquals("Test Chat", success.threadTitle)
            assertEquals(2, success.entries.size)
            assertEquals(1, success.currentAgentId)
        }
    }

    @Test
    fun `submitEntry calls use case with correct params`() = runTest {
        val getEntries = mockk<GetEntriesUseCase>()
        val submitEntry = mockk<SubmitEntryUseCase>()
        val prefsManager = mockk<AgentPreferencesManager>()
        val testEntry = Entry(id = 200, agentId = 1, threadId = 10, content = "New msg", timestamp = 700L)

        every { getEntries(10) } returns flowOf(testEntries)
        every { prefsManager.agentPreferences } returns flowOf(
            AgentPreferences(agentId = 1, agentName = "Bot", agentSecret = "s3cret")
        )
        coEvery { submitEntry(any(), any(), any(), any()) } returns testEntry

        val viewModel = createViewModel(
            getEntriesUseCase = getEntries,
            submitEntryUseCase = submitEntry,
            prefsManager = prefsManager
        )

        // Let it settle
        viewModel.uiState.test {
            var state = awaitItem()
            if (state is EntriesUiState.Loading) state = awaitItem()
            assertTrue(state is EntriesUiState.Success)
        }

        viewModel.submitEntry("New msg")

        coVerify {
            submitEntry(
                threadId = 10,
                content = "New msg",
                agentId = 1,
                agentSecret = "s3cret"
            )
        }
    }
}
