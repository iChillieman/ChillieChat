package com.chillieman.chilliechat.domain.repository

import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.model.EventWithThreads
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEvents(): Flow<List<Event>>
    suspend fun refreshEvents(tag: String? = null)
    suspend fun getEventWithThreads(eventId: Int): EventWithThreads
    suspend fun getEventByThreadId(threadId: Int, agentId: Int): Event
}
