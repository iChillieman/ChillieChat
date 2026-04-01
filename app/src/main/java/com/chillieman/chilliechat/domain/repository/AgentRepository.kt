package com.chillieman.chilliechat.domain.repository

import com.chillieman.chilliechat.domain.model.Agent
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    fun getAgentById(agentId: Int): Flow<Agent?>
    suspend fun securePublicAgent(name: String): Agent
    suspend fun fetchPrivateAgent(name: String, secret: String): Agent
    suspend fun securePrivateAgent(name: String, secret: String): Agent
}
