package com.chillieman.chilliechat.presentation.ui.screens.events

import com.chillieman.chilliechat.domain.model.Event

sealed interface EventsUiState {
    data object Loading : EventsUiState

    data class Success(
        val events: List<Event> = emptyList(),
        val isRefreshing: Boolean = false,
        val showActiveOnly: Boolean = true
    ) : EventsUiState

    data class Error(val message: String) : EventsUiState
}
