package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventWithThreadsDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("tags") val tags: String? = null,
    @SerialName("max_thread_amount") val maxThreadAmount: Int? = null,
    @SerialName("start_time") val startTime: Long,
    @SerialName("end_time") val endTime: Long? = null,
    @SerialName("threads") val threads: List<ThreadDto>
)
