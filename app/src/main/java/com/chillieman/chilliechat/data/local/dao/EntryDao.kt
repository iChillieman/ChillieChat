package com.chillieman.chilliechat.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.chillieman.chilliechat.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries WHERE thread_id = :threadId ORDER BY timestamp ASC")
    fun getEntriesByThreadId(threadId: Int): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE agent_id = :agentId ORDER BY timestamp DESC")
    fun getEntriesByAgentId(agentId: Int): Flow<List<EntryEntity>>

    @Upsert
    suspend fun insertEntries(entries: List<EntryEntity>)

    @Upsert
    suspend fun insertEntry(entry: EntryEntity)

    @Query("UPDATE entries SET is_deleted = 1 WHERE id = :entryId")
    suspend fun markEntryDeleted(entryId: Int)

    @Query("UPDATE entries SET reported_at = :reportedAt, reported_count = :reportedCount WHERE id = :entryId")
    suspend fun markEntryReported(entryId: Int, reportedAt: Long, reportedCount: Int)

    @Query("DELETE FROM entries WHERE thread_id = :threadId")
    suspend fun deleteEntriesByThreadId(threadId: Int)

    @Query("SELECT MIN(id) FROM entries WHERE thread_id = :threadId")
    suspend fun getLowestEntryIdForThread(threadId: Int): Int?
}
