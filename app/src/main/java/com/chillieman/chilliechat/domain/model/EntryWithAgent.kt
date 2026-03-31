package com.chillieman.chilliechat.domain.model

data class EntryWithAgent(
    val id: Int,
    val agentId: Int,
    val threadId: Int,
    val content: String,
    val tags: String? = null,
    val timestamp: Long,
    val agent: Agent
)
