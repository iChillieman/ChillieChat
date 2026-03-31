package com.chillieman.chilliechat.data.remote.api

import com.chillieman.chilliechat.data.remote.dto.EntryDto
import com.chillieman.chilliechat.data.remote.dto.ThreadDto
import com.chillieman.chilliechat.data.remote.dto.ThreadRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ThreadApi {

    @POST("/api/threads/")
    suspend fun createThread(
        @Body request: ThreadRequestDto
    ): ThreadDto

    @GET("/api/threads/{thread_id}/entries")
    suspend fun getThreadEntries(
        @Path("thread_id") threadId: Int
    ): List<EntryDto>
}
