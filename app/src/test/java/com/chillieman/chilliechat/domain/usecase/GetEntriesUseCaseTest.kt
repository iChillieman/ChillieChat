package com.chillieman.chilliechat.domain.usecase

import app.cash.turbine.test
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import com.chillieman.chilliechat.domain.repository.EntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetEntriesUseCaseTest {

    private val repository = mockk<EntryRepository>()
    private val useCase = GetEntriesUseCase(repository)

    private val testAgent = Agent(id = 1, name = "Bot", type = "PUBLIC", capabilities = null)
    private val testEntries = listOf(
        EntryWithAgent(id = 100, agentId = 1, threadId = 10, content = "Hello", timestamp = 500L, agent = testAgent)
    )

    @Test
    fun `invoke emits entries from repository`() = runTest {
        coEvery { repository.getEntriesByThreadId(10) } returns flowOf(testEntries)
        coEvery { repository.refreshEntries(10) } returns false

        useCase(10).test {
            assertEquals(testEntries, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke triggers refresh on start`() = runTest {
        coEvery { repository.getEntriesByThreadId(10) } returns flowOf(testEntries)
        coEvery { repository.refreshEntries(10) } returns false

        useCase(10).test {
            awaitItem()
            awaitComplete()
        }

        coVerify { repository.refreshEntries(10) }
    }

    @Test
    fun `invoke still emits when refresh fails`() = runTest {
        coEvery { repository.getEntriesByThreadId(10) } returns flowOf(testEntries)
        coEvery { repository.refreshEntries(10) } throws RuntimeException("offline")

        useCase(10).test {
            assertEquals(testEntries, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `refresh delegates to repository and returns hasMore`() = runTest {
        coEvery { repository.refreshEntries(10) } returns true

        val hasMore = useCase.refresh(10)

        assertTrue(hasMore)
        coVerify { repository.refreshEntries(10) }
    }
}
