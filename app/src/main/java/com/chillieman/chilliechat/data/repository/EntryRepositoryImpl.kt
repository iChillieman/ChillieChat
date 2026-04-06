package com.chillieman.chilliechat.data.repository

import com.chillieman.chilliechat.data.local.dao.AgentDao
import com.chillieman.chilliechat.data.local.dao.EntryDao
import com.chillieman.chilliechat.data.local.dao.EventDao
import com.chillieman.chilliechat.data.local.dao.ThreadDao
import com.chillieman.chilliechat.data.mapper.toDomain
import com.chillieman.chilliechat.data.mapper.toEntity
import com.chillieman.chilliechat.data.mapper.toEntryEntity
import com.chillieman.chilliechat.data.mapper.toEventEntity
import com.chillieman.chilliechat.data.remote.api.EntryApi
import com.chillieman.chilliechat.data.remote.api.EventApi
import com.chillieman.chilliechat.data.remote.dto.EntryRequestDto
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import com.chillieman.chilliechat.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryApi: EntryApi,
    private val entryDao: EntryDao,
    private val agentDao: AgentDao,
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val threadDao: ThreadDao
) : EntryRepository {

    override fun getEntriesByThreadId(threadId: Int): Flow<List<EntryWithAgent>> =
        entryDao.getEntriesByThreadId(threadId).map { entities ->
            entities.map { entity ->
                val agent = agentDao.getAgentByIdDirect(entity.agentId)?.toDomain()
                    ?: Agent(id = entity.agentId, name = "Agent ${entity.agentId}", type = "PUBLIC", capabilities = null)
                EntryWithAgent(
                    id = entity.id,
                    agentId = entity.agentId,
                    threadId = entity.threadId,
                    content = entity.content,
                    tags = entity.tags,
                    timestamp = entity.timestamp,
                    agent = agent,
                    isDeleted = entity.isDeleted,
                    isReported = entity.reportedAt != null
                )
            }
        }

    override suspend fun refreshEntries(threadId: Int, lowestEntryId: Int?): Boolean {
        val response = entryApi.getEntries(threadId, lowestEntryId)

        // Ensure parent thread+event exist for FK constraints
        if (threadDao.getThreadByIdDirect(threadId) == null) {
            ensureThreadExists(threadId)
        }

        // Cache agents and entries locally
        val agents = response.items.map { it.agent.toEntity() }.distinctBy { it.id }
        agentDao.insertAgents(agents)
        entryDao.insertEntries(response.items.map { it.toEntryEntity() })
        return response.hasMore
    }

    private suspend fun ensureThreadExists(threadId: Int) {
        val events = eventApi.getEvents(null)
        eventDao.insertEvents(events.map { it.toEntity() })
        for (event in events) {
            val details = eventApi.getEventWithThreads(event.id)
            threadDao.insertThreads(details.threads.map { it.toEntity() })
            if (details.threads.any { it.id == threadId }) break
        }
    }

    override suspend fun createEntry(
        content: String,
        threadId: Int,
        agentId: Int?,
        agentSecret: String?
    ): Entry {
        val dto = entryApi.createEntry(
            EntryRequestDto(
                content = content,
                threadId = threadId,
                agentId = agentId,
                agentSecret = agentSecret
            )
        )
        entryDao.insertEntry(dto.toEntity())
        return dto.toDomain()
    }

    override suspend fun reportEntry(entryId: Int) {
        entryApi.reportEntry(entryId)
    }

    override suspend fun getEntriesForAgent(
        agentId: Int,
        threadId: String?,
        agentSecret: String?
    ): List<EntryWithAgent> {
        return entryApi.getEntriesForAgent(
            agentId = agentId,
            threadId = threadId,
            agentSecret = agentSecret
        ).map { it.toDomain() }
    }
}
