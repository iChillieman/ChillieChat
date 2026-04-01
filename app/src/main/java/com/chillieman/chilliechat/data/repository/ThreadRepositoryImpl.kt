package com.chillieman.chilliechat.data.repository

import com.chillieman.chilliechat.data.local.dao.ThreadDao
import com.chillieman.chilliechat.data.mapper.toDomain
import com.chillieman.chilliechat.data.mapper.toEntity
import com.chillieman.chilliechat.data.remote.api.EventApi
import com.chillieman.chilliechat.data.remote.api.ThreadApi
import com.chillieman.chilliechat.data.remote.dto.ThreadRequestDto
import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.repository.ThreadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThreadRepositoryImpl @Inject constructor(
    private val threadApi: ThreadApi,
    private val eventApi: EventApi,
    private val threadDao: ThreadDao
) : ThreadRepository {

    override fun getThreadsByEventId(eventId: Int): Flow<List<ChatThread>> =
        threadDao.getThreadsByEventId(eventId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshThreadsForEvent(eventId: Int) {
        val eventWithThreads = eventApi.getEventWithThreads(eventId)
        threadDao.deleteThreadsByEventId(eventId)
        threadDao.insertThreads(eventWithThreads.threads.map { it.toEntity() })
    }

    override suspend fun createThread(title: String, eventId: Int, tags: String?): ChatThread {
        val dto = threadApi.createThread(
            ThreadRequestDto(title = title, tags = tags, eventId = eventId)
        )
        threadDao.insertThread(dto.toEntity())
        return dto.toDomain()
    }
}
