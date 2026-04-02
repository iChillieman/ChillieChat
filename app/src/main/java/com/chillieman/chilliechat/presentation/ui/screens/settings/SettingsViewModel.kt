package com.chillieman.chilliechat.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.usecase.SecureAgentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureAgentUseCase: SecureAgentUseCase,
    private val agentPreferencesManager: AgentPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSavedAgent()
    }

    private fun loadSavedAgent() {
        viewModelScope.launch {
            try {
                val prefs = agentPreferencesManager.agentPreferences.first()
                val agent = if (prefs.agentId != null && prefs.agentName != null) {
                    Agent(
                        id = prefs.agentId,
                        name = prefs.agentName,
                        type = prefs.agentType ?: "PUBLIC",
                        capabilities = null
                    )
                } else null

                _uiState.value = SettingsUiState.Success(
                    currentAgent = agent,
                    nameInput = prefs.agentName ?: "",
                    secretInput = prefs.agentSecret ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to load settings: ${e.message}")
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { state ->
            if (state is SettingsUiState.Success) state.copy(nameInput = name) else state
        }
    }

    fun onSecretChanged(secret: String) {
        _uiState.update { state ->
            if (state is SettingsUiState.Success) state.copy(secretInput = secret) else state
        }
    }

    fun loginPublic() {
        val state = _uiState.value
        if (state !is SettingsUiState.Success || state.nameInput.isBlank()) return

        viewModelScope.launch {
            _uiState.update { (it as SettingsUiState.Success).copy(isSubmitting = true) }
            try {
                val agent = secureAgentUseCase.securePublic(state.nameInput.trim())
                saveAndUpdateAgent(agent, secret = null)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Public login failed: ${e.message}")
            }
        }
    }

    fun loginPrivate() {
        val state = _uiState.value
        if (state !is SettingsUiState.Success || state.nameInput.isBlank() || state.secretInput.isBlank()) return

        viewModelScope.launch {
            _uiState.update { (it as SettingsUiState.Success).copy(isSubmitting = true) }
            try {
                val agent = secureAgentUseCase.securePrivate(
                    state.nameInput.trim(),
                    state.secretInput.trim()
                )
                saveAndUpdateAgent(agent, secret = state.secretInput.trim())
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Private login failed: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            agentPreferencesManager.clearAgent()
            _uiState.value = SettingsUiState.Success()
        }
    }

    fun dismissError() {
        loadSavedAgent()
    }

    private suspend fun saveAndUpdateAgent(agent: Agent, secret: String?) {
        agentPreferencesManager.saveAgent(
            id = agent.id,
            name = agent.name,
            type = agent.type,
            secret = secret
        )
        _uiState.value = SettingsUiState.Success(
            currentAgent = agent,
            nameInput = agent.name,
            secretInput = secret ?: ""
        )
    }
}
