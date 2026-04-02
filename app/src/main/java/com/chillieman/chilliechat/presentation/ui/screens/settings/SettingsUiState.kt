package com.chillieman.chilliechat.presentation.ui.screens.settings

import com.chillieman.chilliechat.domain.model.Agent

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val currentAgent: Agent? = null,
        val nameInput: String = "",
        val secretInput: String = "",
        val isSubmitting: Boolean = false,
        val alwaysShowReported: Boolean = false
    ) : SettingsUiState

    data class Error(val message: String) : SettingsUiState
}
