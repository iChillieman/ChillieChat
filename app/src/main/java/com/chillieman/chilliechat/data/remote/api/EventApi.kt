package com.chillieman.chilliechat.data.remote.api

import com.chillieman.chilliechat.data.remote.dto.EventDto
import com.chillieman.chilliechat.data.remote.dto.EventWithThreadsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {

    @GET("/api/events/")
    suspend fun getEvents(
        @Query("tag") tag: String? = null
    ): List<EventDto>

    @GET("/api/events/{event_id}")
    suspend fun getEventWithThreads(
        @Path("event_id") eventId: Int
    ): EventWithThreadsDto

    @GET("/api/events/single")
    suspend fun getEventByThreadId(
        @Query("thread_id") threadId: Int,
        @Query("agent_id") agentId: Int
    ): EventDto
}
