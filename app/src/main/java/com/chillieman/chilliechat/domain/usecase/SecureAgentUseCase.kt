package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.repository.AgentRepository
import javax.inject.Inject

class SecureAgentUseCase @Inject constructor(
    private val agentRepository: AgentRepository
) {
    suspend fun securePublic(name: String): Agent =
        agentRepository.securePublicAgent(name)

    suspend fun securePrivate(name: String, secret: String): Agent =
        agentRepository.securePrivateAgent(name, secret)

    suspend fun fetchPrivate(name: String, secret: String): Agent =
        agentRepository.fetchPrivateAgent(name, secret)
}
