package com.chillieman.chilliechat.presentation.ui.screens.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.domain.model.BlockedAgent
import com.chillieman.chilliechat.domain.repository.BlockedAgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BlockedUsersUiState {
    object Loading : BlockedUsersUiState
    data class Success(val blockedAgents: List<BlockedAgent>) : BlockedUsersUiState
}

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val blockedAgentRepository: BlockedAgentRepository
) : ViewModel() {

    val uiState: StateFlow<BlockedUsersUiState> =
        blockedAgentRepository.getAllBlockedAgents()
            .map { BlockedUsersUiState.Success(it) as BlockedUsersUiState }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = BlockedUsersUiState.Loading
            )

    fun unblockAgent(agentId: Int) {
        viewModelScope.launch {
            blockedAgentRepository.unblockAgent(agentId)
        }
    }
}
