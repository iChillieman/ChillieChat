package com.chillieman.chilliechat.presentation.ui.screens.entries

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.data.remote.WebSocketManager
import com.chillieman.chilliechat.domain.repository.EntryRepository
import com.chillieman.chilliechat.domain.usecase.GetEntriesUseCase
import com.chillieman.chilliechat.domain.usecase.SubmitEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEntriesUseCase: GetEntriesUseCase,
    private val submitEntryUseCase: SubmitEntryUseCase,
    private val agentPreferencesManager: AgentPreferencesManager,
    private val webSocketManager: WebSocketManager,
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val threadId: Int = checkNotNull(savedStateHandle["threadId"])
    private val threadTitle: String = checkNotNull(savedStateHandle["threadTitle"])
    private val eventEndTime: Long? = savedStateHandle["eventEndTime"]

    private val _hasMore = MutableStateFlow(true)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _revealedEntryIds = MutableStateFlow<Set<Int>>(emptySet())

    init {
        webSocketManager.connect(threadId)
    }

    val uiState: StateFlow<EntriesUiState> = combine(
        getEntriesUseCase(threadId)
            .catch { emit(emptyList()) },
        agentPreferencesManager.agentPreferences,
        _hasMore,
        _isLoadingMore,
        _revealedEntryIds
    ) { entries, prefs, hasMore, isLoadingMore, revealedIds ->
        EntriesUiState.Success(
            threadId = threadId,
            threadTitle = threadTitle,
            entries = entries,
            currentAgentId = prefs.agentId,
            alwaysShowReported = prefs.alwaysShowReportedMessages,
            revealedEntryIds = revealedIds,
            hasMore = hasMore,
            isLoadingMore = isLoadingMore,
            isArchived = eventEndTime != null && System.currentTimeMillis() / 1000 > eventEndTime,
            soundEnabled = prefs.soundEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EntriesUiState.Loading
    )

    fun loadOlderEntries() {
        val state = uiState.value
        if (state !is EntriesUiState.Success || state.isLoadingMore || !state.hasMore) return
        val lowestId = state.entries.minOfOrNull { it.id } ?: return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val hasMore = getEntriesUseCase.loadMore(threadId, lowestId)
                _hasMore.value = hasMore
            } catch (_: Exception) {
                // Silently fail — cached data still visible
            }
            _isLoadingMore.value = false
        }
    }

    fun submitEntry(content: String) {
        viewModelScope.launch {
            try {
                val prefs = agentPreferencesManager.agentPreferences.first()
                submitEntryUseCase(
                    threadId = threadId,
                    content = content,
                    agentId = prefs.agentId,
                    agentSecret = prefs.agentSecret
                )
            } catch (_: Exception) {
                // Entry failed to send — the local cache flow still shows existing entries
            }
        }
    }

    fun reportEntry(entryId: Int) {
        viewModelScope.launch {
            try {
                entryRepository.reportEntry(entryId)
            } catch (_: Exception) {
                // Report failed silently — user can try again
            }
        }
    }

    fun revealEntry(entryId: Int) {
        _revealedEntryIds.value = _revealedEntryIds.value + entryId
    }

    fun setAlwaysShowReported(enabled: Boolean) {
        viewModelScope.launch {
            agentPreferencesManager.setAlwaysShowReportedMessages(enabled)
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}
