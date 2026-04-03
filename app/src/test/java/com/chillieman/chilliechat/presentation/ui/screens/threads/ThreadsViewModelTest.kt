package com.chillieman.chilliechat.presentation.ui.screens.threads

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.chillieman.chilliechat.MainDispatcherRule
import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.model.EventWithThreads
import com.chillieman.chilliechat.domain.usecase.GetEventWithThreadsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThreadsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testEvent = Event(id = 1, title = "Test Event", startTime = 100L)
    private val testThreads = listOf(
        ChatThread(id = 10, eventId = 1, title = "Thread A", createdAt = 200L, entryCount = 5),
        ChatThread(id = 11, eventId = 1, title = "Thread B", createdAt = 300L, entryCount = 0)
    )
    private val testResult = EventWithThreads(event = testEvent, threads = testThreads)

    private fun createViewModel(
        eventId: Int = 1,
        useCase: GetEventWithThreadsUseCase
    ): ThreadsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId))
        return ThreadsViewModel(savedStateHandle, useCase)
    }

    @Test
    fun `initial state transitions from Loading to Success`() = runTest {
        val useCase = mockk<GetEventWithThreadsUseCase>()
        coEvery { useCase(1) } returns testResult

        val viewModel = createViewModel(useCase = useCase)

        viewModel.uiState.test {
            val items = mutableListOf<ThreadsUiState>()
            items.add(awaitItem())
            if (items.last() is ThreadsUiState.Loading) {
                items.add(awaitItem())
            }
            val success = items.last() as ThreadsUiState.Success
            assertEquals("Test Event", success.event.title)
            assertEquals(2, success.threads.size)
        }
    }

    @Test
    fun `emits Error when use case throws`() = runTest {
        val useCase = mockk<GetEventWithThreadsUseCase>()
        coEvery { useCase(1) } throws RuntimeException("404 Not Found")

        val viewModel = createViewModel(useCase = useCase)

        viewModel.uiState.test {
            val items = mutableListOf<ThreadsUiState>()
            items.add(awaitItem())
            if (items.last() is ThreadsUiState.Loading) {
                items.add(awaitItem())
            }
            val error = items.last() as ThreadsUiState.Error
            assertEquals("Failed to load threads", error.message)
        }
    }

    @Test
    fun `refresh updates data on success`() = runTest {
        val useCase = mockk<GetEventWithThreadsUseCase>()
        val updatedResult = testResult.copy(
            threads = testThreads + ChatThread(id = 12, eventId = 1, title = "New Thread", createdAt = 400L, entryCount = 1)
        )
        coEvery { useCase(1) } returns testResult andThen updatedResult

        val viewModel = createViewModel(useCase = useCase)

        viewModel.uiState.test {
            // Consume initial load
            var state = awaitItem()
            if (state is ThreadsUiState.Loading) state = awaitItem()
            assertEquals(2, (state as ThreadsUiState.Success).threads.size)

            viewModel.refresh()

            val refreshed = expectMostRecentItem() as ThreadsUiState.Success
            assertEquals(3, refreshed.threads.size)
        }
    }

    @Test
    fun `refresh keeps existing data when network fails`() = runTest {
        var callCount = 0
        val useCase = mockk<GetEventWithThreadsUseCase>()
        coEvery { useCase(1) } answers {
            callCount++
            if (callCount == 1) testResult
            else throw RuntimeException("offline")
        }

        val viewModel = createViewModel(useCase = useCase)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is ThreadsUiState.Loading) state = awaitItem()
            assertTrue(state is ThreadsUiState.Success)
            cancelAndConsumeRemainingEvents()
        }

        viewModel.refresh()

        // After failed refresh, state should still have the original data
        val currentState = viewModel.uiState.value
        assertTrue(currentState is ThreadsUiState.Success)
        assertEquals(2, (currentState as ThreadsUiState.Success).threads.size)
        assertFalse(currentState.isRefreshing)
    }
}
