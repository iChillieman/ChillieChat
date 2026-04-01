package com.chillieman.chilliechat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagedListEntryWithAgentDetailsDto(
    @SerialName("items") val items: List<EntryWithAgentDetailsDto>,
    @SerialName("has_more") val hasMore: Boolean
)
