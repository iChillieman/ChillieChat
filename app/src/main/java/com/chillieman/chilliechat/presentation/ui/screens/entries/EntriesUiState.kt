package com.chillieman.chilliechat.presentation.ui.screens.entries

import com.chillieman.chilliechat.domain.model.EntryWithAgent

sealed interface EntriesUiState {
    object Loading : EntriesUiState
    data class Success(
        val threadId: Int,
        val threadTitle: String,
        val entries: List<EntryWithAgent>,
        val currentAgentId: Int?,
        val alwaysShowReported: Boolean = false,
        val revealedEntryIds: Set<Int> = emptySet(),
        val isRefreshing: Boolean = false,
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isArchived: Boolean = false
    ) : EntriesUiState
    data class Error(val message: String) : EntriesUiState
}
