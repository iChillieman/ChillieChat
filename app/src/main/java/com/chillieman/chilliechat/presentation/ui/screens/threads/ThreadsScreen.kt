package com.chillieman.chilliechat.presentation.ui.screens.threads

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.model.Event
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ThreadsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEntries: (threadId: Int, threadTitle: String, eventEndTime: Long?) -> Unit,
    viewModel: ThreadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ThreadsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEntries = onNavigateToEntries,
        onRefresh = viewModel::refresh,
        onConfirmAge = viewModel::confirmAge,
        onDismissDaeTip = viewModel::dismissDaeTip
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThreadsScreenContent(
    uiState: ThreadsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEntries: (threadId: Int, threadTitle: String, eventEndTime: Long?) -> Unit,
    onRefresh: () -> Unit,
    onConfirmAge: () -> Unit = {},
    onDismissDaeTip: () -> Unit = {}
) {
    when (val state = uiState) {
        is ThreadsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ThreadsUiState.Success -> {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "event_header") {
                        EventHeader(event = state.event)
                    }

                    item(key = "divider") {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    if (state.threads.isEmpty()) {
                        item(key = "empty") {
                            EmptyThreadsContent()
                        }
                    } else {
                        items(
                            items = state.threads,
                            key = { it.id }
                        ) { thread ->
                            ThreadCard(
                                thread = thread,
                                onClick = {
                                    onNavigateToEntries(
                                        thread.id,
                                        thread.title,
                                        state.event.endTime
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (state.event.id != 4 && !state.hasConfirmedAge) {
                var checked by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { /* forced action */ },
                    title = { Text("Content Warning") },
                    text = {
                        Column {
                            Text("Warning - this event is NOT moderated in real time by Daedalus - you may see offensive language, you must be 18 years or older to enter.")
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.combinedClickable(onClick = {
                                    checked = !checked
                                })
                            ) {
                                Checkbox(checked = checked, onCheckedChange = { checked = it })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("I'm 18, let me in")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = onConfirmAge, enabled = checked) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onNavigateBack) {
                            Text("Cancel")
                        }
                    }
                )
            } else if (state.event.id == 4 && !state.hasSeenDaeTip) {
                var dontRemindMe by remember { mutableStateOf(false) }
                var isLocallyDismissed by remember { mutableStateOf(false) }

                if (!isLocallyDismissed) {
                    AlertDialog(
                        onDismissRequest = { isLocallyDismissed = true },
                        title = { Text("Tip") },
                        text = {
                            Column {
                                Text("Daedalus moderates this chat in real time! When you enter the chatroom, say something that contains Dae, and Daedalus will respond to you as soon as possible.")
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.combinedClickable(onClick = {
                                        dontRemindMe = !dontRemindMe
                                    })
                                ) {
                                    Checkbox(
                                        checked = dontRemindMe,
                                        onCheckedChange = { dontRemindMe = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Don't Remind me again")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (dontRemindMe) {
                                    onDismissDaeTip()
                                } else {
                                    isLocallyDismissed = true
                                }
                            }) {
                                Text("Got it!")
                            }
                        }
                    )
                }
            }
        }

        is ThreadsUiState.Error -> {
            ErrorContent(
                message = state.message,
                onRetry = onRefresh
            )
        }
    }
}

@Composable
private fun EventHeader(event: Event) {
    Column {
        if (!event.description.isNullOrBlank()) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!event.tags.isNullOrBlank()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                event.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                    TagChip(tag)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatTimeRange(event.startTime, event.endTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThreadCard(
    thread: ChatThread,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = thread.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${thread.entryCount} ${if (thread.entryCount == 1) "entry" else "entries"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (!thread.tags.isNullOrBlank()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        thread.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            .forEach { tag ->
                                TagChip(tag)
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(tag: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun EmptyThreadsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "No threads found for this event",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

private fun formatTimestamp(epochSeconds: Long): String {
    val instant = Instant.ofEpochSecond(epochSeconds)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

private fun formatTimeRange(startTime: Long, endTime: Long?): String {
    val start = formatTimestamp(startTime)
    return if (endTime != null) {
        "$start \u2014 ${formatTimestamp(endTime)}"
    } else {
        start
    }
}
