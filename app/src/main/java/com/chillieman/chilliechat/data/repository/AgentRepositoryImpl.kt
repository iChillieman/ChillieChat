package com.chillieman.chilliechat.data.repository

import com.chillieman.chilliechat.data.local.dao.AgentDao
import com.chillieman.chilliechat.data.mapper.toDomain
import com.chillieman.chilliechat.data.mapper.toEntity
import com.chillieman.chilliechat.data.remote.api.AgentApi
import com.chillieman.chilliechat.data.remote.dto.PrivateAgentRequestDto
import com.chillieman.chilliechat.data.remote.dto.SecurePublicAgentRequestDto
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AgentRepositoryImpl @Inject constructor(
    private val agentApi: AgentApi,
    private val agentDao: AgentDao
) : AgentRepository {

    override fun getAgentById(agentId: Int): Flow<Agent?> =
        agentDao.getAgentById(agentId).map { it?.toDomain() }

    override suspend fun securePublicAgent(name: String): Agent {
        val dto = agentApi.securePublicAgent(SecurePublicAgentRequestDto(agentName = name))
        agentDao.insertAgent(dto.toEntity())
        return dto.toDomain()
    }

    override suspend fun fetchPrivateAgent(name: String, secret: String): Agent {
        val dto = agentApi.fetchPrivateAgent(
            PrivateAgentRequestDto(agentName = name, agentSecret = secret)
        )
        agentDao.insertAgent(dto.toEntity())
        return dto.toDomain()
    }

    override suspend fun securePrivateAgent(name: String, secret: String): Agent {
        val dto = agentApi.securePrivateAgent(
            PrivateAgentRequestDto(agentName = name, agentSecret = secret)
        )
        agentDao.insertAgent(dto.toEntity())
        return dto.toDomain()
    }
}
