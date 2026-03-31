package com.chillieman.chilliechat.presentation.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreenContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onSecretChanged = viewModel::onSecretChanged,
        onLoginPublic = viewModel::loginPublic,
        onLoginPrivate = viewModel::loginPrivate,
        onLogout = viewModel::logout,
        onDismissError = viewModel::dismissError
    )
}

@Composable
internal fun SettingsScreenContent(
    uiState: SettingsUiState,
    onNameChanged: (String) -> Unit,
    onSecretChanged: (String) -> Unit,
    onLoginPublic: () -> Unit,
    onLoginPrivate: () -> Unit,
    onLogout: () -> Unit,
    onDismissError: () -> Unit
) {
    when (val state = uiState) {
        is SettingsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SettingsUiState.Success -> {
            SettingsContent(
                state = state,
                onNameChanged = onNameChanged,
                onSecretChanged = onSecretChanged,
                onLoginPublic = onLoginPublic,
                onLoginPrivate = onLoginPrivate,
                onLogout = onLogout
            )
        }

        is SettingsUiState.Error -> {
            ErrorContent(
                message = state.message,
                onDismiss = onDismissError
            )
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState.Success,
    onNameChanged: (String) -> Unit,
    onSecretChanged: (String) -> Unit,
    onLoginPublic: () -> Unit,
    onLoginPrivate: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Agent Card
        if (state.currentAgent != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Agent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = state.currentAgent.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Type: ${state.currentAgent.type}  |  ID: ${state.currentAgent.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    if (state.secretInput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Has secret",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Secured with secret",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }
            }
        }

        // Input Section
        Text(
            text = if (state.currentAgent != null) "Change Agent" else "Set Up Your Agent",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = state.nameInput,
            onValueChange = onNameChanged,
            label = { Text("Agent Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting
        )

        OutlinedTextField(
            value = state.secretInput,
            onValueChange = onSecretChanged,
            label = { Text("Secret (optional)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting
        )

        // Action Buttons
        if (state.isSubmitting) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Button(
                onClick = onLoginPublic,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.nameInput.isNotBlank()
            ) {
                Text("Login / Register Publicly")
            }

            OutlinedButton(
                onClick = onLoginPrivate,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.nameInput.isNotBlank() && state.secretInput.isNotBlank()
            ) {
                Text("Login / Register Privately")
            }

            Text(
                text = "Public agents share the name with anyone who uses it. " +
                        "Private agents require the secret to authenticate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = onDismiss) {
                    Text("Try Again")
                }
            }
        }
    }
}
