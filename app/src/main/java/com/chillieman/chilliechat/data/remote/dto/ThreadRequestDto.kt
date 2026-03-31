package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadRequestDto(
    @SerialName("title") val title: String,
    @SerialName("tags") val tags: String? = null,
    @SerialName("event_id") val eventId: Int
)
