package com.chillieman.chilliechat.domain.model

data class ChatThread(
    val id: Int,
    val eventId: Int,
    val title: String,
    val tags: String? = null,
    val createdAt: Long,
    val entryCount: Int = 0
)
