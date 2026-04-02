package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntryDeletedEventDto(
    @SerialName("type") val type: String,
    @SerialName("entry_id") val entryId: Int,
    @SerialName("thread_id") val threadId: Int
)
