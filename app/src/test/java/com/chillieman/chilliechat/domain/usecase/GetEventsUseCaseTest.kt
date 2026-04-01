package com.chillieman.chilliechat.domain.usecase

import app.cash.turbine.test
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetEventsUseCaseTest {

    private val repository = mockk<EventRepository>()
    private val useCase = GetEventsUseCase(repository)

    private val testEvents = listOf(
        Event(id = 1, title = "Event 1", startTime = 100L),
        Event(id = 2, title = "Event 2", startTime = 200L)
    )

    @Test
    fun `invoke emits events from repository`() = runTest {
        coEvery { repository.getEvents() } returns flowOf(testEvents)
        coEvery { repository.refreshEvents(any()) } returns Unit

        useCase().test {
            assertEquals(testEvents, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke triggers refresh on start`() = runTest {
        coEvery { repository.getEvents() } returns flowOf(testEvents)
        coEvery { repository.refreshEvents(any()) } returns Unit

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        coVerify { repository.refreshEvents(null) }
    }

    @Test
    fun `invoke passes tag to refresh`() = runTest {
        coEvery { repository.getEvents() } returns flowOf(testEvents)
        coEvery { repository.refreshEvents(any()) } returns Unit

        useCase(tag = "dev").test {
            awaitItem()
            awaitComplete()
        }

        coVerify { repository.refreshEvents("dev") }
    }

    @Test
    fun `invoke still emits cached data when refresh fails`() = runTest {
        coEvery { repository.getEvents() } returns flowOf(testEvents)
        coEvery { repository.refreshEvents(any()) } throws RuntimeException("Network error")

        useCase().test {
            assertEquals(testEvents, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `refresh delegates to repository`() = runTest {
        coEvery { repository.refreshEvents(any()) } returns Unit

        useCase.refresh("test")

        coVerify { repository.refreshEvents("test") }
    }

    @Test
    fun `refresh propagates exceptions`() = runTest {
        coEvery { repository.refreshEvents(any()) } throws RuntimeException("fail")

        try {
            useCase.refresh()
            assert(false) { "Should have thrown" }
        } catch (e: RuntimeException) {
            assertEquals("fail", e.message)
        }
    }
}
