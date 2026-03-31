package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateAgentRequestDto(
    @SerialName("agent_name") val agentName: String,
    @SerialName("agent_secret") val agentSecret: String
)
