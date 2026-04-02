package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntryReportedEventDto(
    @SerialName("type") val type: String,
    @SerialName("entry_id") val entryId: Int,
    @SerialName("reported_at") val reportedAt: Long,
    @SerialName("reported_count") val reportedCount: Int
)
