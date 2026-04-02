package com.chillieman.chilliechat.presentation.ui.screens.threads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.domain.usecase.GetEventWithThreadsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreadsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventWithThreadsUseCase: GetEventWithThreadsUseCase
) : ViewModel() {

    private val eventId: Int = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow<ThreadsUiState>(ThreadsUiState.Loading)
    val uiState: StateFlow<ThreadsUiState> = _uiState.asStateFlow()

    init {
        loadThreads()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                if (it is ThreadsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                val result = getEventWithThreadsUseCase(eventId)
                _uiState.value = ThreadsUiState.Success(
                    event = result.event,
                    threads = result.threads
                )
            } catch (e: Exception) {
                val current = _uiState.value
                if (current is ThreadsUiState.Success) {
                    _uiState.value = current.copy(isRefreshing = false)
                } else {
                    _uiState.value = ThreadsUiState.Error("Failed to refresh threads")
                }
            }
        }
    }

    private fun loadThreads() {
        viewModelScope.launch {
            try {
                val result = getEventWithThreadsUseCase(eventId)
                _uiState.value = ThreadsUiState.Success(
                    event = result.event,
                    threads = result.threads
                )
            } catch (e: Exception) {
                _uiState.value = ThreadsUiState.Error("Failed to load threads")
            }
        }
    }
}
