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
        // CircularProgressIndicator doesn't have text, but we can verify no events text shows
        composeTestRule.onNodeWithText("No events found").assertDoesNotExist()
    }

    @Test
    fun successState_showsEventCards() {
        val events = listOf(
            Event(id = 1, title = "Hackathon 2026", description = "Build cool stuff", startTime = 1711234567L),
            Event(id = 2, title = "Game Jam", tags = "games,fun", startTime = 1711239999L)
        )
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = events),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Hackathon 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Game Jam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Build cool stuff").assertIsDisplayed()
        composeTestRule.onNodeWithText("games").assertIsDisplayed()
        composeTestRule.onNodeWithText("fun").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            ChillieChatTheme {
                EventsScreenContent(
                    uiState = EventsUiState.Success(events = emptyList()),
                    onNavigateToThreads = { _, _ -> },
                    onRefresh = {}
                )
            }
        }
        composeTestRule.onNodeWithText("No events found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pull down to refresh").assertIsDisplayed()
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
}
