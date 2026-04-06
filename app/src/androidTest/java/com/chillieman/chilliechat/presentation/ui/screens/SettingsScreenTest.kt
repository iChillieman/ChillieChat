package com.chillieman.chilliechat.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.presentation.ui.screens.settings.SettingsScreenContent
import com.chillieman.chilliechat.presentation.ui.screens.settings.SettingsUiState
import com.chillieman.chilliechat.presentation.ui.theme.ChillieChatTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val noOp: (String) -> Unit = {}
    private val noOpBool: (Boolean) -> Unit = {}
    private val noOpAction: () -> Unit = {}

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Loading,
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLogin = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction,
                    onToggleAlwaysShowReported = noOpBool,
                    onToggleSoundEnabled = noOpBool
                )
            }
        }
        composeTestRule.onNodeWithText("Your name").assertDoesNotExist()
    }

    @Test
    fun successState_noAgent_showsSetupForm() {
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Success(
                        currentAgent = null,
                        nameInput = "",
                        secretInput = ""
                    ),
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLogin = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction,
                    onToggleAlwaysShowReported = noOpBool,
                    onToggleSoundEnabled = noOpBool
                )
            }
        }
        composeTestRule.onNodeWithText("Set Up Your Identity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login / Register").assertIsDisplayed()
    }

    @Test
    fun successState_withAgent_showsAgentCard() {
        val agent = Agent(id = 42, name = "Chillie", type = "PUBLIC")
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Success(
                        currentAgent = agent,
                        nameInput = "Chillie",
                        secretInput = ""
                    ),
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLogin = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction,
                    onToggleAlwaysShowReported = noOpBool,
                    onToggleSoundEnabled = noOpBool
                )
            }
        }
        composeTestRule.onNodeWithText("Current Agent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chillie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type: PUBLIC  |  ID: 42").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Identity").assertIsDisplayed()
    }

    @Test
    fun loginButton_disabledWhenNameEmpty() {
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Success(
                        currentAgent = null,
                        nameInput = "",
                        secretInput = ""
                    ),
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLogin = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction,
                    onToggleAlwaysShowReported = noOpBool,
                    onToggleSoundEnabled = noOpBool
                )
            }
        }
        composeTestRule.onNodeWithText("Login / Register").assertIsNotEnabled()
    }

    @Test
    fun errorState_showsErrorAndRetry() {
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Error("Auth failed"),
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLogin = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction,
                    onToggleAlwaysShowReported = noOpBool,
                    onToggleSoundEnabled = noOpBool
                )
            }
        }
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auth failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }
}
