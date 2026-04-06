package com.chillieman.chilliechat.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.usecase.SecureAgentUseCase
import com.chillieman.chilliechat.presentation.onboarding.OnboardingManager
import com.chillieman.chilliechat.presentation.onboarding.OnboardingStep
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
    private val agentPreferencesManager: AgentPreferencesManager,
    private val onboardingManager: OnboardingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val onboardingStep: StateFlow<OnboardingStep> = onboardingManager.currentStep
    val isOnboarding: StateFlow<Boolean> = onboardingManager.isActive

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
                    secretInput = prefs.agentSecret ?: "",
                    alwaysShowReported = prefs.alwaysShowReportedMessages,
                    soundEnabled = prefs.soundEnabled
                )
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to load saved settings")
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

    fun login() {
        val state = _uiState.value
        if (state !is SettingsUiState.Success || state.nameInput.isBlank()) return

        val hasSecret = state.secretInput.isNotBlank()

        viewModelScope.launch {
            _uiState.update { (it as SettingsUiState.Success).copy(isSubmitting = true) }
            try {
                val agent = if (hasSecret) {
                    secureAgentUseCase.securePrivate(
                        state.nameInput.trim(),
                        state.secretInput.trim()
                    )
                } else {
                    secureAgentUseCase.securePublic(state.nameInput.trim())
                }
                saveAndUpdateAgent(agent, secret = if (hasSecret) state.secretInput.trim() else null)

                // Advance onboarding: login succeeded → highlight logout
                val step = onboardingManager.currentStep.value
                if (step == OnboardingStep.HIGHLIGHT_LOGIN || step == OnboardingStep.WAIT_LOGIN) {
                    onboardingManager.setStep(OnboardingStep.HIGHLIGHT_LOGOUT)
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            agentPreferencesManager.clearAgent()
            _uiState.value = SettingsUiState.Success()
            // Advance onboarding: logout → complete
            if (onboardingManager.currentStep.value == OnboardingStep.HIGHLIGHT_LOGOUT) {
                onboardingManager.completeOnboarding()
            }
        }
    }

    fun advanceOnboarding() {
        onboardingManager.advanceStep()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingManager.completeOnboarding()
        }
    }

    fun toggleSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            agentPreferencesManager.setSoundEnabled(enabled)
            _uiState.update { state ->
                if (state is SettingsUiState.Success) state.copy(soundEnabled = enabled) else state
            }
        }
    }

    fun toggleAlwaysShowReported(enabled: Boolean) {
        viewModelScope.launch {
            agentPreferencesManager.setAlwaysShowReportedMessages(enabled)
            _uiState.update { state ->
                if (state is SettingsUiState.Success) state.copy(alwaysShowReported = enabled) else state
            }
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
