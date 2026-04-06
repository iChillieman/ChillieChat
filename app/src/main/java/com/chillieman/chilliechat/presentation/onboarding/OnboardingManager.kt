package com.chillieman.chilliechat.presentation.onboarding

import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingManager @Inject constructor(
    private val preferencesManager: AgentPreferencesManager
) {
    private val _currentStep = MutableStateFlow(OnboardingStep.COMPLETED)
    val currentStep: StateFlow<OnboardingStep> = _currentStep

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    suspend fun initialize() {
        val prefs = preferencesManager.agentPreferences.first()
        if (!prefs.onboardingCompleted) {
            _isActive.value = true
            _currentStep.value = OnboardingStep.SPOTLIGHT_SETTINGS
        }
    }

    fun advanceStep() {
        val next = when (_currentStep.value) {
            OnboardingStep.SPOTLIGHT_SETTINGS -> OnboardingStep.FOCUS_AGENT_NAME
            OnboardingStep.FOCUS_AGENT_NAME -> OnboardingStep.FOCUS_SECRET
            OnboardingStep.FOCUS_SECRET -> OnboardingStep.HIGHLIGHT_LOGIN
            OnboardingStep.HIGHLIGHT_LOGIN -> OnboardingStep.WAIT_LOGIN
            OnboardingStep.WAIT_LOGIN -> OnboardingStep.HIGHLIGHT_LOGOUT
            OnboardingStep.HIGHLIGHT_LOGOUT -> OnboardingStep.HIGHLIGHT_DAE_CARD
            OnboardingStep.HIGHLIGHT_DAE_CARD -> OnboardingStep.COMPLETED
            OnboardingStep.COMPLETED -> OnboardingStep.COMPLETED
        }
        _currentStep.value = next
    }

    fun setStep(step: OnboardingStep) {
        _currentStep.value = step
    }

    suspend fun completeOnboarding() {
        _currentStep.value = OnboardingStep.COMPLETED
        _isActive.value = false
        preferencesManager.setOnboardingCompleted(true)
    }
}
