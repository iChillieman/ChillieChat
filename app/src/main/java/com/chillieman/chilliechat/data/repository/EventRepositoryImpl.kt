package com.chillieman.chilliechat.data.repository

import com.chillieman.chilliechat.data.local.dao.EventDao
import com.chillieman.chilliechat.data.local.dao.ThreadDao
import com.chillieman.chilliechat.data.mapper.toDomain
import com.chillieman.chilliechat.data.mapper.toEntity
import com.chillieman.chilliechat.data.mapper.toEventEntity
import com.chillieman.chilliechat.data.remote.api.EventApi
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.model.EventWithThreads
import com.chillieman.chilliechat.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val threadDao: ThreadDao
) : EventRepository {

    override fun getEvents(): Flow<List<Event>> =
        eventDao.getEvents().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshEvents(tag: String?) {
        val dtos = eventApi.getEvents(tag)
        eventDao.insertEvents(dtos.map { it.toEntity() })
    }

    override suspend fun getEventWithThreads(eventId: Int): EventWithThreads {
        val dto = eventApi.getEventWithThreads(eventId)
        // Cache event and threads locally
        eventDao.insertEvent(dto.toEventEntity())
        threadDao.insertThreads(dto.threads.map { it.toEntity() })
        return dto.toDomain()
    }

    override suspend fun getEventByThreadId(threadId: Int, agentId: Int): Event {
        val dto = eventApi.getEventByThreadId(threadId, agentId)
        eventDao.insertEvent(dto.toEntity())
        return dto.toDomain()
    }
}
