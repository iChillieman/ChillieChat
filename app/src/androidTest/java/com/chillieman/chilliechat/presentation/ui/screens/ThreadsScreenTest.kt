package com.chillieman.chilliechat.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.presentation.ui.screens.threads.ThreadsScreenContent
import com.chillieman.chilliechat.presentation.ui.screens.threads.ThreadsUiState
import com.chillieman.chilliechat.presentation.ui.theme.ChillieChatTheme
import org.junit.Rule
import org.junit.Test

class ThreadsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testEvent = Event(
        id = 1,
        title = "Hackathon 2026",
        description = "Build cool stuff",
        tags = "hack,fun",
        startTime = 1711234567L
    )

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            ChillieChatTheme {
                ThreadsScreenContent(
                    uiState = ThreadsUiState.Loading,
                    onNavigateToEntries = { _, _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("No threads found for this event").assertDoesNotExist()
    }

    @Test
    fun successState_showsEventHeaderAndThreads() {
        val threads = listOf(
            ChatThread(id = 10, eventId = 1, title = "General Chat", createdAt = 200L, entryCount = 5),
            ChatThread(id = 11, eventId = 1, title = "Bug Reports", tags = "bugs", createdAt = 300L, entryCount = 12)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                ThreadsScreenContent(
                    uiState = ThreadsUiState.Success(event = testEvent, threads = threads),
                    onNavigateToEntries = { _, _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Build cool stuff").assertIsDisplayed()
        composeTestRule.onNodeWithText("General Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bug Reports").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 entries").assertIsDisplayed()
        composeTestRule.onNodeWithText("12 entries").assertIsDisplayed()
        composeTestRule.onNodeWithText("bugs").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            ChillieChatTheme {
                ThreadsScreenContent(
                    uiState = ThreadsUiState.Success(event = testEvent, threads = emptyList()),
                    onNavigateToEntries = { _, _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("No threads found for this event").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetry() {
        composeTestRule.setContent {
            ChillieChatTheme {
                ThreadsScreenContent(
                    uiState = ThreadsUiState.Error("Server error"),
                    onNavigateToEntries = { _, _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Server error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun singleEntry_showsSingularLabel() {
        val threads = listOf(
            ChatThread(id = 10, eventId = 1, title = "Solo Thread", createdAt = 200L, entryCount = 1)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                ThreadsScreenContent(
                    uiState = ThreadsUiState.Success(event = testEvent, threads = threads),
                    onNavigateToEntries = { _, _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("1 entry").assertIsDisplayed()
    }
}
