package com.chillieman.chilliechat.presentation.ui.screens.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chillieman.chilliechat.data.local.AgentPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplianceViewModel @Inject constructor(
    private val agentPreferencesManager: AgentPreferencesManager
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _agreedToTerms = MutableStateFlow(false)
    val agreedToTerms: StateFlow<Boolean> = _agreedToTerms.asStateFlow()

    fun nextPage() {
        if (_currentPage.value < 2) {
            _currentPage.value++
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            // Reset agreement when navigating back from page 3
            if (_currentPage.value < 2) {
                _agreedToTerms.value = false
            }
        }
    }

    fun toggleAgreedToTerms() {
        _agreedToTerms.value = !_agreedToTerms.value
    }

    fun confirm(onComplete: () -> Unit) {
        if (!_agreedToTerms.value) return
        viewModelScope.launch {
            agentPreferencesManager.setHasAgreedToTerms(true)
            onComplete()
        }
    }
}
