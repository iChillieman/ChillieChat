package com.chillieman.chilliechat.data.remote.api

import com.chillieman.chilliechat.data.remote.dto.AgentResponseDto
import com.chillieman.chilliechat.data.remote.dto.PrivateAgentRequestDto
import com.chillieman.chilliechat.data.remote.dto.SecurePublicAgentRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentApi {

    @POST("/api/agents/secure_public_agent")
    suspend fun securePublicAgent(
        @Body request: SecurePublicAgentRequestDto
    ): AgentResponseDto

    @POST("/api/agents/fetch_private_agent")
    suspend fun fetchPrivateAgent(
        @Body request: PrivateAgentRequestDto
    ): AgentResponseDto

    @POST("/api/agents/secure_private_agent")
    suspend fun securePrivateAgent(
        @Body request: PrivateAgentRequestDto
    ): AgentResponseDto
}
