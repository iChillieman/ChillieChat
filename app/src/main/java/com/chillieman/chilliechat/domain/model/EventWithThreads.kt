package com.chillieman.chilliechat.domain.model

data class EventWithThreads(
    val event: Event,
    val threads: List<ChatThread>
)
