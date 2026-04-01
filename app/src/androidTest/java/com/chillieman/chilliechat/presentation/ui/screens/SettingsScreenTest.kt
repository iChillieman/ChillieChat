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
    private val noOpAction: () -> Unit = {}

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Loading,
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLoginPublic = noOpAction,
                    onLoginPrivate = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction
                )
            }
        }
        composeTestRule.onNodeWithText("Agent Name").assertDoesNotExist()
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
                    onLoginPublic = noOpAction,
                    onLoginPrivate = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction
                )
            }
        }
        composeTestRule.onNodeWithText("Set Up Your Agent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login / Register Publicly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login / Register Privately").assertIsDisplayed()
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
                    onLoginPublic = noOpAction,
                    onLoginPrivate = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction
                )
            }
        }
        composeTestRule.onNodeWithText("Current Agent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chillie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type: PUBLIC  |  ID: 42").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Agent").assertIsDisplayed()
    }

    @Test
    fun loginButtons_disabledWhenNameEmpty() {
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
                    onLoginPublic = noOpAction,
                    onLoginPrivate = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction
                )
            }
        }
        composeTestRule.onNodeWithText("Login / Register Publicly").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Login / Register Privately").assertIsNotEnabled()
    }

    @Test
    fun errorState_showsErrorAndRetry() {
        composeTestRule.setContent {
            ChillieChatTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState.Error("Auth failed"),
                    onNameChanged = noOp,
                    onSecretChanged = noOp,
                    onLoginPublic = noOpAction,
                    onLoginPrivate = noOpAction,
                    onLogout = noOpAction,
                    onDismissError = noOpAction
                )
            }
        }
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auth failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }
}
