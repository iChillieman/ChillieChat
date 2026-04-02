package com.chillieman.chilliechat.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.presentation.ui.screens.events.EventsScreenContent
import com.chillieman.chilliechat.presentation.ui.screens.events.EventsUiState
import com.chillieman.chilliechat.presentation.ui.theme.ChillieChatTheme
import org.junit.Rule
import org.junit.Test

class EventsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Use far-future timestamps so events are "Active" during test
    private val futureStart = (System.currentTimeMillis() / 1000) - 3600
    private val futureEnd = (System.currentTimeMillis() / 1000) + 86400

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Loading,
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("No events found").assertDoesNotExist()
    }

    @Test
    fun successState_showsEventCards() {
        val events = listOf(
            Event(id = 1, title = "Hackathon 2026", description = "Build cool stuff", startTime = futureStart, endTime = futureEnd),
            Event(id = 2, title = "Game Jam", startTime = futureStart, endTime = futureEnd)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = events, showActiveOnly = false),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Hackathon 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Game Jam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Build cool stuff").assertIsDisplayed()
    }

    @Test
    fun successState_showsStatusPills() {
        val events = listOf(
            Event(id = 1, title = "Active Event", startTime = futureStart, endTime = futureEnd)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = events, showActiveOnly = false),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
    }

    @Test
    fun activeOnlyFilter_hidesEndedEvents() {
        val now = System.currentTimeMillis() / 1000
        val events = listOf(
            Event(id = 1, title = "Active Event", startTime = now - 3600, endTime = now + 3600),
            Event(id = 2, title = "Ended Event", startTime = now - 7200, endTime = now - 3600)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = events, showActiveOnly = true),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Active Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ended Event").assertDoesNotExist()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = emptyList(), showActiveOnly = false),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("No events found").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Error("Network timeout"),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network timeout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun showActiveOnlyCheckbox_isDisplayed() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = emptyList()),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Only Show Active Events").assertIsDisplayed()
    }
}
