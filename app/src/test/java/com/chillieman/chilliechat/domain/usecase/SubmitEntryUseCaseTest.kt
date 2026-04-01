package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.repository.EntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SubmitEntryUseCaseTest {

    private val repository = mockk<EntryRepository>()
    private val useCase = SubmitEntryUseCase(repository)

    private val testEntry = Entry(id = 1, agentId = 5, threadId = 10, content = "Test", timestamp = 100L)

    @Test
    fun `invoke creates entry and returns result`() = runTest {
        coEvery { repository.createEntry("Hello", 10, 5, null) } returns testEntry

        val result = useCase(content = "Hello", threadId = 10, agentId = 5)

        assertEquals(testEntry, result)
    }

    @Test
    fun `invoke passes all parameters including secret`() = runTest {
        coEvery { repository.createEntry(any(), any(), any(), any()) } returns testEntry

        useCase(content = "msg", threadId = 10, agentId = 5, agentSecret = "s3cret")

        coVerify { repository.createEntry("msg", 10, 5, "s3cret") }
    }

    @Test
    fun `invoke works with null optional params`() = runTest {
        coEvery { repository.createEntry(any(), any(), any(), any()) } returns testEntry

        useCase(content = "anon", threadId = 10)

        coVerify { repository.createEntry("anon", 10, null, null) }
    }

    @Test
    fun `invoke propagates exceptions`() = runTest {
        coEvery { repository.createEntry(any(), any(), any(), any()) } throws RuntimeException("403")

        try {
            useCase(content = "x", threadId = 1)
            assert(false) { "Should have thrown" }
        } catch (e: RuntimeException) {
            assertEquals("403", e.message)
        }
    }
}
