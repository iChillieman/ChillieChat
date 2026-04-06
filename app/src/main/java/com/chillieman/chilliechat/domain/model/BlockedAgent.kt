package com.chillieman.chilliechat.domain.model

data class BlockedAgent(
    val agentId: Int,
    val agentName: String,
    val blockedAt: Long
)
