package com.chillieman.chilliechat.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.chillieman.chilliechat.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY start_time DESC")
    fun getEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Int): Flow<EventEntity?>

    @Upsert
    suspend fun insertEvents(events: List<EventEntity>)

    @Upsert
    suspend fun insertEvent(event: EventEntity)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
