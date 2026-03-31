package com.chillieman.chilliechat.domain.repository

import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import kotlinx.coroutines.flow.Flow

interface EntryRepository {
    fun getEntriesByThreadId(threadId: Int): Flow<List<Entry>>
    suspend fun refreshEntries(threadId: Int, lowestEntryId: Int? = null): Boolean
    suspend fun createEntry(
        content: String,
        threadId: Int,
        agentId: Int? = null,
        agentSecret: String? = null
    ): Entry
    suspend fun getEntriesForAgent(
        agentId: Int,
        threadId: String? = null,
        agentSecret: String? = null
    ): List<EntryWithAgent>
}
