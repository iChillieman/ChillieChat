package com.chillieman.chilliechat.domain.repository

import com.chillieman.chilliechat.domain.model.ChatThread
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    fun getThreadsByEventId(eventId: Int): Flow<List<ChatThread>>
    suspend fun refreshThreadsForEvent(eventId: Int)
    suspend fun createThread(title: String, eventId: Int, tags: String? = null): ChatThread
}
