package com.chillieman.chilliechat.presentation.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.domain.usecase.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            getEventsUseCase()
                .catch { e ->
                    _uiState.value = EventsUiState.Error("Failed to load events")
                }
                .collect { events ->
                    _uiState.update { current ->
                        val showActiveOnly = (current as? EventsUiState.Success)?.showActiveOnly ?: true
                        EventsUiState.Success(events = events, showActiveOnly = showActiveOnly)
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                if (it is EventsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                getEventsUseCase.refresh()
            } catch (_: Exception) {
                // Room Flow will still emit cached data
            }
            _uiState.update {
                if (it is EventsUiState.Success) it.copy(isRefreshing = false) else it
            }
        }
    }

    fun toggleActiveOnly() {
        _uiState.update {
            if (it is EventsUiState.Success) it.copy(showActiveOnly = !it.showActiveOnly) else it
        }
    }
}
