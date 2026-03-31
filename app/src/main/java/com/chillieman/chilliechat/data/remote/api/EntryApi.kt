package com.chillieman.chilliechat.data.remote.api

import com.chillieman.chilliechat.data.remote.dto.EntryDto
import com.chillieman.chilliechat.data.remote.dto.EntryRequestDto
import com.chillieman.chilliechat.data.remote.dto.EntryWithAgentDetailsDto
import com.chillieman.chilliechat.data.remote.dto.PagedListEntryWithAgentDetailsDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface EntryApi {

    @GET("/api/entries/")
    suspend fun getEntries(
        @Query("thread_id") threadId: Int,
        @Query("lowest_entry_id") lowestEntryId: Int? = null
    ): PagedListEntryWithAgentDetailsDto

    @GET("/api/entries/agent")
    suspend fun getEntriesForAgent(
        @Query("agent_id") agentId: Int,
        @Query("thread_id") threadId: String? = null,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null,
        @Header("X-Agent-Secret") agentSecret: String? = null
    ): List<EntryWithAgentDetailsDto>

    @POST("/api/entries/")
    suspend fun createEntry(
        @Body request: EntryRequestDto
    ): EntryDto
}
