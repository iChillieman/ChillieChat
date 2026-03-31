package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SecurePublicAgentRequestDto(
    @SerialName("agent_name") val agentName: String
)
