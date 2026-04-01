package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.model.EventWithThreads
import com.chillieman.chilliechat.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetEventWithThreadsUseCaseTest {

    private val repository = mockk<EventRepository>()
    private val useCase = GetEventWithThreadsUseCase(repository)

    private val testResult = EventWithThreads(
        event = Event(id = 1, title = "Event", startTime = 100L),
        threads = listOf(
            ChatThread(id = 10, eventId = 1, title = "Thread A", createdAt = 200L, entryCount = 5)
        )
    )

    @Test
    fun `invoke returns event with threads from repository`() = runTest {
        coEvery { repository.getEventWithThreads(1) } returns testResult

        val result = useCase(1)

        assertEquals(testResult.event.title, result.event.title)
        assertEquals(1, result.threads.size)
        assertEquals("Thread A", result.threads[0].title)
    }

    @Test
    fun `invoke passes correct eventId`() = runTest {
        coEvery { repository.getEventWithThreads(42) } returns testResult

        useCase(42)

        coVerify { repository.getEventWithThreads(42) }
    }

    @Test
    fun `invoke propagates exceptions`() = runTest {
        coEvery { repository.getEventWithThreads(any()) } throws RuntimeException("Not found")

        try {
            useCase(999)
            assert(false) { "Should have thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Not found", e.message)
        }
    }
}
