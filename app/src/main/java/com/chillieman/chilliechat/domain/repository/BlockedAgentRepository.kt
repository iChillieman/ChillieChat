package com.chillieman.chilliechat.domain.repository

import com.chillieman.chilliechat.domain.model.BlockedAgent
import kotlinx.coroutines.flow.Flow

interface BlockedAgentRepository {
    fun getAllBlockedAgents(): Flow<List<BlockedAgent>>
    fun getBlockedAgentIds(): Flow<List<Int>>
    suspend fun blockAgent(agentId: Int, agentName: String)
    suspend fun unblockAgent(agentId: Int)
    suspend fun isBlocked(agentId: Int): Boolean
}
