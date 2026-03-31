package com.chillieman.chilliechat.presentation.ui.screens.entries

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.domain.usecase.GetEntriesUseCase
import com.chillieman.chilliechat.domain.usecase.SubmitEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEntriesUseCase: GetEntriesUseCase,
    private val submitEntryUseCase: SubmitEntryUseCase,
    private val agentPreferencesManager: AgentPreferencesManager
) : ViewModel() {

    private val threadId: Int = checkNotNull(savedStateHandle["threadId"])
    private val threadTitle: String = checkNotNull(savedStateHandle["threadTitle"])

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<EntriesUiState> = combine(
        getEntriesUseCase(threadId).onStart { refresh() }.catch { /* handle */ },
        agentPreferencesManager.agentPreferences,
        _isRefreshing
    ) { entries, prefs, isRefreshing ->
        EntriesUiState.Success(
            threadId = threadId,
            threadTitle = threadTitle,
            entries = entries,
            currentAgentId = prefs.agentId,
            isRefreshing = isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EntriesUiState.Loading
    )

    fun refresh() {
        // Implementation for pagination/refresh
    }

    fun submitEntry(content: String) {
        viewModelScope.launch {
            val prefs = agentPreferencesManager.agentPreferences.first()
            submitEntryUseCase(
                threadId = threadId,
                content = content,
                agentId = prefs.agentId,
                agentSecret = prefs.agentSecret
            )
        }
    }
}
