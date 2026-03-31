package com.chillieman.chilliechat.domain.model

data class Event(
    val id: Int,
    val title: String,
    val description: String? = null,
    val tags: String? = null,
    val maxThreadAmount: Int? = null,
    val startTime: Long,
    val endTime: Long? = null
)
