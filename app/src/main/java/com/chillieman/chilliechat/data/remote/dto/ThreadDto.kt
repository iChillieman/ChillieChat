package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadDto(
    @SerialName("id") val id: Int,
    @SerialName("event_id") val eventId: Int,
    @SerialName("title") val title: String,
    @SerialName("tags") val tags: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("entry_count") val entryCount: Int? = null
)
