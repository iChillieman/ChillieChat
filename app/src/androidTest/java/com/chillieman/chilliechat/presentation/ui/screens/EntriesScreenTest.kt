package com.chillieman.chilliechat.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.chillieman.chilliechat.domain.model.Agent
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import com.chillieman.chilliechat.presentation.ui.screens.entries.EntriesScreenContent
import com.chillieman.chilliechat.presentation.ui.screens.entries.EntriesUiState
import com.chillieman.chilliechat.presentation.ui.theme.ChillieChatTheme
import org.junit.Rule
import org.junit.Test

class EntriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val agentMe = Agent(id = 1, name = "Chillie", type = "PUBLIC")
    private val agentOther = Agent(id = 2, name = "Dae", type = "PRIVATE")

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EntriesScreenContent(
                    uiState = EntriesUiState.Loading,
                    onSubmitEntry = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Type a message...").assertDoesNotExist()
    }

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EntriesScreenContent(
                    uiState = EntriesUiState.Error("Connection lost"),
                    onSubmitEntry = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Connection lost").assertIsDisplayed()
    }

    @Test
    fun successState_showsEntriesAndInput() {
        val entries = listOf(
            EntryWithAgent(id = 1, agentId = 1, threadId = 10, content = "Hello world!", timestamp = 100L, agent = agentMe),
            EntryWithAgent(id = 2, agentId = 2, threadId = 10, content = "Hey there!", timestamp = 200L, agent = agentOther)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                EntriesScreenContent(
                    uiState = EntriesUiState.Success(
                        threadId = 10,
                        threadTitle = "General",
                        entries = entries,
                        currentAgentId = 1
                    ),
                    onSubmitEntry = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Hello world!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hey there!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chillie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dae").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type a message...").assertIsDisplayed()
    }

    @Test
    fun sendButton_disabledWhenEmpty() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EntriesScreenContent(
                    uiState = EntriesUiState.Success(
                        threadId = 10,
                        threadTitle = "General",
                        entries = emptyList(),
                        currentAgentId = 1
                    ),
                    onSubmitEntry = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Send").assertIsNotEnabled()
    }
}
