package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntryRequestDto(
    @SerialName("content") val content: String,
    @SerialName("thread_id") val threadId: Int,
    @SerialName("agent_id") val agentId: Int? = null,
    @SerialName("agent_secret") val agentSecret: String? = null
)
