package com.chillieman.chilliechat.presentation.ui.screens.events

import app.cash.turbine.test
import com.chillieman.chilliechat.MainDispatcherRule
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.usecase.GetEventsUseCase
import com.chillieman.chilliechat.presentation.onboarding.OnboardingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onboardingManager = mockk<OnboardingManager>(relaxed = true)

    private val testEvents = listOf(
        Event(id = 1, title = "Event 1", startTime = 100L),
        Event(id = 2, title = "Event 2", startTime = 200L)
    )

    @Test
    fun `initial state is Loading`() = runTest {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase(any()) } returns flow { /* never emits */ }

        val viewModel = EventsViewModel(useCase, onboardingManager)

        viewModel.uiState.test {
            assertEquals(EventsUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `emits Success when events are loaded`() = runTest {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase(any()) } returns flowOf(testEvents)

        val viewModel = EventsViewModel(useCase, onboardingManager)

        viewModel.uiState.test {
            // May get Loading first, then Success
            val items = mutableListOf<EventsUiState>()
            items.add(awaitItem())
            if (items.last() is EventsUiState.Loading) {
                items.add(awaitItem())
            }
            val success = items.last() as EventsUiState.Success
            assertEquals(2, success.events.size)
            assertEquals("Event 1", success.events[0].title)
        }
    }

    @Test
    fun `emits Error when flow throws`() = runTest {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase(any()) } returns flow { throw RuntimeException("Network error") }

        val viewModel = EventsViewModel(useCase, onboardingManager)

        viewModel.uiState.test {
            val items = mutableListOf<EventsUiState>()
            items.add(awaitItem())
            if (items.last() is EventsUiState.Loading) {
                items.add(awaitItem())
            }
            val error = items.last() as EventsUiState.Error
            assertEquals("Failed to load events", error.message)
        }
    }

    @Test
    fun `refresh calls use case refresh`() = runTest {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase(any()) } returns flowOf(testEvents)
        coEvery { useCase.refresh(any()) } returns Unit

        val viewModel = EventsViewModel(useCase, onboardingManager)

        viewModel.uiState.test {
            var state = awaitItem()
            if (state is EventsUiState.Loading) state = awaitItem()
            assertTrue(state is EventsUiState.Success)

            viewModel.refresh()
            cancelAndConsumeRemainingEvents()
        }

        coVerify { useCase.refresh(null) }
    }
}
