package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.repository.ThreadRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateThreadUseCaseTest {

    private val repository = mockk<ThreadRepository>()
    private val useCase = CreateThreadUseCase(repository)

    private val testThread = ChatThread(id = 1, eventId = 5, title = "New Thread", createdAt = 100L, entryCount = 0)

    @Test
    fun `invoke creates thread and returns result`() = runTest {
        coEvery { repository.createThread("New Thread", 5, null) } returns testThread

        val result = useCase(title = "New Thread", eventId = 5)

        assertEquals(testThread, result)
    }

    @Test
    fun `invoke passes tags to repository`() = runTest {
        coEvery { repository.createThread(any(), any(), any()) } returns testThread

        useCase(title = "Tagged", eventId = 5, tags = "general,chat")

        coVerify { repository.createThread("Tagged", 5, "general,chat") }
    }

    @Test
    fun `invoke propagates exceptions`() = runTest {
        coEvery { repository.createThread(any(), any(), any()) } throws RuntimeException("Max threads reached")

        try {
            useCase(title = "T", eventId = 1)
            assert(false) { "Should have thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Max threads reached", e.message)
        }
    }
}
