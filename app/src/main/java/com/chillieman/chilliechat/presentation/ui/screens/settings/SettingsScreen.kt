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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chillieman.chilliechat.presentation.onboarding.OnboardingInstructionCard
import com.chillieman.chilliechat.presentation.onboarding.OnboardingStep
import com.chillieman.chilliechat.presentation.onboarding.onboardingHighlight
import kotlinx.coroutines.delay
import androidx.core.net.toUri

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onboardingStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val isOnboarding by viewModel.isOnboarding.collectAsStateWithLifecycle()

    SettingsScreenContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onSecretChanged = viewModel::onSecretChanged,
        onLogin = viewModel::login,
        onLogout = viewModel::logout,
        onDismissError = viewModel::dismissError,
        onToggleAlwaysShowReported = viewModel::toggleAlwaysShowReported,
        onToggleSoundEnabled = viewModel::toggleSoundEnabled,
        isOnboarding = isOnboarding,
        onboardingStep = onboardingStep,
        onAdvanceOnboarding = viewModel::advanceOnboarding,
        onCompleteOnboarding = viewModel::completeOnboarding
    )
}

@Composable
internal fun SettingsScreenContent(
    uiState: SettingsUiState,
    onNameChanged: (String) -> Unit,
    onSecretChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onDismissError: () -> Unit,
    onToggleAlwaysShowReported: (Boolean) -> Unit,
    onToggleSoundEnabled: (Boolean) -> Unit,
    isOnboarding: Boolean = false,
    onboardingStep: OnboardingStep = OnboardingStep.COMPLETED,
    onAdvanceOnboarding: () -> Unit = {},
    onCompleteOnboarding: () -> Unit = {}
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
                onLogin = onLogin,
                onLogout = onLogout,
                onToggleAlwaysShowReported = onToggleAlwaysShowReported,
                onToggleSoundEnabled = onToggleSoundEnabled,
                isOnboarding = isOnboarding,
                onboardingStep = onboardingStep,
                onAdvanceOnboarding = onAdvanceOnboarding,
                onCompleteOnboarding = onCompleteOnboarding
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
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onToggleAlwaysShowReported: (Boolean) -> Unit,
    onToggleSoundEnabled: (Boolean) -> Unit,
    isOnboarding: Boolean,
    onboardingStep: OnboardingStep,
    onAdvanceOnboarding: () -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }
    val secretFocusRequester = remember { FocusRequester() }
    var nameFieldFocused by remember { mutableStateOf(false) }
    var secretFieldFocused by remember { mutableStateOf(false) }

    // Auto-focus the Agent Name field
    LaunchedEffect(onboardingStep) {
        if (onboardingStep == OnboardingStep.FOCUS_AGENT_NAME) {
            delay(400) // Let the screen settle
            nameFocusRequester.requestFocus()
        }
        if (onboardingStep == OnboardingStep.FOCUS_SECRET) {
            delay(400)
            secretFocusRequester.requestFocus()
        }
    }

    // Advance when name is filled and user defocuses
    LaunchedEffect(nameFieldFocused) {
        if (isOnboarding
            && onboardingStep == OnboardingStep.FOCUS_AGENT_NAME
            && !nameFieldFocused
            && state.nameInput.isNotBlank()
        ) {
            onAdvanceOnboarding()
        }
    }

    // Inactivity timer: auto-advance after typing name
    LaunchedEffect(state.nameInput, onboardingStep) {
        if (isOnboarding
            && onboardingStep == OnboardingStep.FOCUS_AGENT_NAME
            && state.nameInput.isNotBlank()
        ) {
            delay(2000)
            onAdvanceOnboarding()
        }
    }

    // Auto-advance from secret step after inactivity (resets on each keystroke)
    LaunchedEffect(state.secretInput, onboardingStep) {
        if (isOnboarding && onboardingStep == OnboardingStep.FOCUS_SECRET) {
            delay(4000)
            onAdvanceOnboarding()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .onboardingHighlight(
                                    active = isOnboarding && onboardingStep == OnboardingStep.HIGHLIGHT_LOGOUT
                                )
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }

            // Input Section
            Text(
                text = if (state.currentAgent != null) "Change Identity" else "Set Up Your Identity",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = state.nameInput,
                onValueChange = onNameChanged,
                label = { Text("Your name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester)
                    .onFocusChanged { nameFieldFocused = it.isFocused }
                    .onboardingHighlight(
                        active = isOnboarding && onboardingStep == OnboardingStep.FOCUS_AGENT_NAME
                    ),
                enabled = !state.isSubmitting
            )

            OutlinedTextField(
                value = state.secretInput,
                onValueChange = onSecretChanged,
                label = { Text("Your Secret (optional)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(secretFocusRequester)
                    .onFocusChanged { secretFieldFocused = it.isFocused }
                    .onboardingHighlight(
                        active = isOnboarding && onboardingStep == OnboardingStep.FOCUS_SECRET
                    ),
                enabled = !state.isSubmitting
            )

            // Action Button
            if (state.isSubmitting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onboardingHighlight(
                            active = isOnboarding && onboardingStep == OnboardingStep.HIGHLIGHT_LOGIN
                        ),
                    enabled = state.nameInput.isNotBlank()
                ) {
                    Text("Login / Register")
                }

                Text(
                    text = if (state.secretInput.isNotBlank())
                        "You will be logged in privately with your secret."
                    else
                        "You will be logged in publicly. Add a secret above to go private.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Content Preferences Section
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Content Preferences",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Always Show Reported Messages",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Reported messages will be visible without needing to reveal them individually.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = state.alwaysShowReported,
                        onCheckedChange = onToggleAlwaysShowReported
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Message Sounds & Vibration",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Play a chime or vibrate when new messages arrive.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = state.soundEnabled,
                        onCheckedChange = onToggleSoundEnabled
                    )
                }
            }

            val context = LocalContext.current
            // Code Section
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Code",
                style = MaterialTheme.typography.headlineMedium
            )


            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://github.com/iChillieman/ChillieChat".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ChillieChat source code")
            }

            // Privacy Policy Section
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Legal",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://chillieman.com/privacy".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Privacy Policy")
            }

            // Extra bottom spacing when onboarding card is showing
            if (isOnboarding && onboardingStep != OnboardingStep.COMPLETED) {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Onboarding instruction card — floating at bottom
        if (isOnboarding) {
            val message = when (onboardingStep) {
                OnboardingStep.FOCUS_AGENT_NAME -> "Enter a name"
                OnboardingStep.FOCUS_SECRET -> "Optionally add a secret to make your account private"
                OnboardingStep.HIGHLIGHT_LOGIN -> "Tap \"Login / Register\" to set up your identity"
                OnboardingStep.WAIT_LOGIN -> "Setting up your account..."
                OnboardingStep.HIGHLIGHT_LOGOUT -> "If you want to go back to being anonymous, just logout"
                else -> null
            }

            val showDismiss = onboardingStep == OnboardingStep.HIGHLIGHT_LOGOUT

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                OnboardingInstructionCard(
                    message = message ?: "",
                    visible = message != null,
                    dismissLabel = if (showDismiss) "Got it!" else null,
                    onDismiss = if (showDismiss) onAdvanceOnboarding else null
                )
            }
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
