package com.chillieman.chilliechat.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.chillieman.chilliechat.data.local.entity.ThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {

    @Query("SELECT * FROM threads WHERE event_id = :eventId ORDER BY created_at DESC")
    fun getThreadsByEventId(eventId: Int): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE id = :threadId")
    fun getThreadById(threadId: Int): Flow<ThreadEntity?>

    @Upsert
    suspend fun insertThreads(threads: List<ThreadEntity>)

    @Upsert
    suspend fun insertThread(thread: ThreadEntity)

    @Query("DELETE FROM threads WHERE event_id = :eventId")
    suspend fun deleteThreadsByEventId(eventId: Int)
}
