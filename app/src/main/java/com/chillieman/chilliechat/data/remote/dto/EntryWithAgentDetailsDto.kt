package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntryWithAgentDetailsDto(
    @SerialName("id") val id: Int,
    @SerialName("agent_id") val agentId: Int,
    @SerialName("thread_id") val threadId: Int,
    @SerialName("content") val content: String,
    @SerialName("tags") val tags: String? = null,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("agent") val agent: AgentResponseDto
)
