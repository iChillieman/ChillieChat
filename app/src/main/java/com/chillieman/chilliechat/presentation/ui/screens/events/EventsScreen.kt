package com.chillieman.chilliechat.presentation.ui.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.presentation.onboarding.DaeCardSpotlightOverlay
import com.chillieman.chilliechat.presentation.onboarding.EventsOnboardingOverlay
import com.chillieman.chilliechat.presentation.onboarding.OnboardingStep

private const val BASE_URL = "https://chillieman.com"

private val imagePool = listOf(
    "/cosmic0.jpg",
    "/cosmic1.jpg",
    "/cosmic2.jpg",
    "/cosmic3.jpg",
    "/cosmic4.jpg",
    "/cosmic5.jpg"
)

private fun getEventImageUrl(eventId: Int): String =
    "$BASE_URL${imagePool[eventId % imagePool.size]}"

private enum class EventStatus(val label: String, val color: Color) {
    ACTIVE("Active", Color(0xFF4CAF50)),
    ENDED("Ended", Color(0xFFF44336)),
    UPCOMING("Upcoming", Color(0xFFFFA726))
}

private fun getEventStatus(event: Event): EventStatus {
    val now = System.currentTimeMillis() / 1000
    return when {
        event.endTime != null && now > event.endTime -> EventStatus.ENDED
        now < event.startTime -> EventStatus.UPCOMING
        else -> EventStatus.ACTIVE
    }
}

@Composable
fun EventsScreen(
    onNavigateToThreads: (eventId: Int, eventTitle: String) -> Unit,
    onNavigateToDaeThread: () -> Unit = {},
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onboardingStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val isOnboarding by viewModel.isOnboarding.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        EventsScreenContent(
            uiState = uiState,
            onNavigateToThreads = onNavigateToThreads,
            onNavigateToDaeThread = onNavigateToDaeThread,
            onRefresh = viewModel::refresh,
            onToggleActiveOnly = viewModel::toggleActiveOnly
        )

        EventsOnboardingOverlay(
            visible = isOnboarding && onboardingStep == OnboardingStep.SPOTLIGHT_SETTINGS,
            onSkipOnboarding = viewModel::skipOnboarding
        )

        DaeCardSpotlightOverlay(
            visible = isOnboarding && onboardingStep == OnboardingStep.HIGHLIGHT_DAE_CARD,
            onTapDaeCard = {
                viewModel.completeOnboarding()
                onNavigateToDaeThread()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventsScreenContent(
    uiState: EventsUiState,
    onNavigateToThreads: (eventId: Int, eventTitle: String) -> Unit,
    onNavigateToDaeThread: () -> Unit = {},
    onRefresh: () -> Unit,
    onToggleActiveOnly: () -> Unit = {}
) {
    when (val state = uiState) {
        is EventsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is EventsUiState.Success -> {
            val filteredEvents = if (state.showActiveOnly) {
                state.events.filter { getEventStatus(it) == EventStatus.ACTIVE }
            } else {
                state.events
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Talk to Dae Card
                    TalkToDaeCard(
                        onClick = onNavigateToDaeThread,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Filter row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.showActiveOnly,
                            onCheckedChange = { onToggleActiveOnly() }
                        )
                        Text(
                            text = "Only Show Active Events",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (filteredEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyEventsContent(isFiltered = state.showActiveOnly && state.events.isNotEmpty())
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredEvents,
                                key = { it.id }
                            ) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onNavigateToThreads(event.id, event.title) }
                                )
                            }
                        }
                    }
                }
            }
        }

        is EventsUiState.Error -> {
            ErrorContent(
                message = state.message,
                onRetry = onRefresh
            )
        }
    }
}

@Composable
private fun TalkToDaeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Talk to Dae & Zeph \uD83E\uDD16",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Jump directly into the main AI chat room!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    val status = getEventStatus(event)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            AsyncImage(
                model = getEventImageUrl(event.id),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Dark scrim for readability
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.45f)
            ) {}

            // Status pill — top right
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = status.color
            ) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Title + description — bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!event.description.isNullOrBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyEventsContent(isFiltered: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = if (isFiltered) "No active events right now" else "No events found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isFiltered) "Uncheck the filter to see all events" else "Pull down to refresh",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
