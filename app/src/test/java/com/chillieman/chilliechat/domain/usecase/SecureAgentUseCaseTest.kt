package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.repository.AgentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SecureAgentUseCaseTest {

    private val repository = mockk<AgentRepository>()
    private val useCase = SecureAgentUseCase(repository)

    private val publicAgent = Agent(id = 1, name = "Public", type = "PUBLIC", capabilities = null)
    private val privateAgent = Agent(id = 2, name = "Private", type = "PRIVATE", capabilities = "all")

    @Test
    fun `securePublic delegates to repository`() = runTest {
        coEvery { repository.securePublicAgent("TestBot") } returns publicAgent

        val result = useCase.securePublic("TestBot")

        assertEquals(publicAgent, result)
        coVerify { repository.securePublicAgent("TestBot") }
    }

    @Test
    fun `securePrivate delegates to repository with name and secret`() = runTest {
        coEvery { repository.securePrivateAgent("Priv", "key123") } returns privateAgent

        val result = useCase.securePrivate("Priv", "key123")

        assertEquals(privateAgent, result)
        coVerify { repository.securePrivateAgent("Priv", "key123") }
    }

    @Test
    fun `fetchPrivate delegates to repository`() = runTest {
        coEvery { repository.fetchPrivateAgent("Priv", "key123") } returns privateAgent

        val result = useCase.fetchPrivate("Priv", "key123")

        assertEquals(privateAgent, result)
        coVerify { repository.fetchPrivateAgent("Priv", "key123") }
    }

    @Test
    fun `securePublic propagates exceptions`() = runTest {
        coEvery { repository.securePublicAgent(any()) } throws RuntimeException("409 Conflict")

        try {
            useCase.securePublic("Taken")
            assert(false) { "Should have thrown" }
        } catch (e: RuntimeException) {
            assertEquals("409 Conflict", e.message)
        }
    }
}
