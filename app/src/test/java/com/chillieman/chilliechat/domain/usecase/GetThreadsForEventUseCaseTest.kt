package com.chillieman.chilliechat.domain.usecase

import app.cash.turbine.test
import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.repository.ThreadRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetThreadsForEventUseCaseTest {

    private val repository = mockk<ThreadRepository>()
    private val useCase = GetThreadsForEventUseCase(repository)

    private val testThreads = listOf(
        ChatThread(id = 1, eventId = 5, title = "Thread 1", createdAt = 100L, entryCount = 3),
        ChatThread(id = 2, eventId = 5, title = "Thread 2", createdAt = 200L, entryCount = 0)
    )

    @Test
    fun `invoke emits threads from repository`() = runTest {
        coEvery { repository.getThreadsByEventId(5) } returns flowOf(testThreads)
        coEvery { repository.refreshThreadsForEvent(5) } returns Unit

        useCase(5).test {
            assertEquals(testThreads, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke triggers refresh on start`() = runTest {
        coEvery { repository.getThreadsByEventId(5) } returns flowOf(testThreads)
        coEvery { repository.refreshThreadsForEvent(5) } returns Unit

        useCase(5).test {
            awaitItem()
            awaitComplete()
        }

        coVerify { repository.refreshThreadsForEvent(5) }
    }

    @Test
    fun `invoke still emits cached data when refresh fails`() = runTest {
        coEvery { repository.getThreadsByEventId(5) } returns flowOf(testThreads)
        coEvery { repository.refreshThreadsForEvent(5) } throws RuntimeException("Network")

        useCase(5).test {
            assertEquals(testThreads, awaitItem())
            awaitComplete()
        }
    }
}
