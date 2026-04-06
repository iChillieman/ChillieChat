package com.chillieman.chilliechat.data.repository

import com.chillieman.chilliechat.data.local.dao.BlockedAgentDao
import com.chillieman.chilliechat.data.local.entity.BlockedAgentEntity
import com.chillieman.chilliechat.domain.model.BlockedAgent
import com.chillieman.chilliechat.domain.repository.BlockedAgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockedAgentRepositoryImpl @Inject constructor(
    private val blockedAgentDao: BlockedAgentDao
) : BlockedAgentRepository {

    override fun getAllBlockedAgents(): Flow<List<BlockedAgent>> =
        blockedAgentDao.getAllBlockedAgents().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getBlockedAgentIds(): Flow<List<Int>> =
        blockedAgentDao.getBlockedAgentIds()

    override suspend fun blockAgent(agentId: Int, agentName: String) {
        blockedAgentDao.blockAgent(
            BlockedAgentEntity(
                agentId = agentId,
                agentName = agentName,
                blockedAt = System.currentTimeMillis() / 1000
            )
        )
    }

    override suspend fun unblockAgent(agentId: Int) {
        blockedAgentDao.unblockAgent(agentId)
    }

    override suspend fun isBlocked(agentId: Int): Boolean =
        blockedAgentDao.isBlocked(agentId)

    private fun BlockedAgentEntity.toDomain() = BlockedAgent(
        agentId = agentId,
        agentName = agentName,
        blockedAt = blockedAt
    )
}
