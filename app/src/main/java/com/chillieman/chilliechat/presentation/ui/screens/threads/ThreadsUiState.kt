package com.chillieman.chilliechat.presentation.ui.screens.threads

import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.model.Event

sealed interface ThreadsUiState {
    data object Loading : ThreadsUiState

    data class Success(
        val event: Event,
        val threads: List<ChatThread> = emptyList(),
        val isRefreshing: Boolean = false
    ) : ThreadsUiState

    data class Error(val message: String) : ThreadsUiState
}
