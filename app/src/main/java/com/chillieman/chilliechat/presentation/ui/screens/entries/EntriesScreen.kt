package com.chillieman.chilliechat.presentation.ui.screens.entries

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EntriesScreen(
    viewModel: EntriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EntriesScreenContent(
        uiState = uiState,
        onSubmitEntry = viewModel::submitEntry,
        onLoadMore = viewModel::loadOlderEntries
    )
}

@Composable
internal fun EntriesScreenContent(
    uiState: EntriesUiState,
    onSubmitEntry: (String) -> Unit,
    onLoadMore: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }

    when (val state = uiState) {
        is EntriesUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is EntriesUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is EntriesUiState.Success -> {
            val listState = rememberLazyListState()

            // Auto-scroll to bottom only when new entries arrive at the end (or initial load).
            // Pagination loads older entries at the top — lastEntryId stays the same, so no scroll.
            val lastEntryId = state.entries.lastOrNull()?.id
            LaunchedEffect(lastEntryId) {
                if (lastEntryId != null && state.entries.isNotEmpty()) {
                    listState.animateScrollToItem(state.entries.size - 1)
                }
            }

            // Detect scroll to top for pagination
            val shouldLoadMore by remember {
                derivedStateOf {
                    val firstVisible = listState.firstVisibleItemIndex
                    firstVisible <= 2 && state.hasMore && !state.isLoadingMore
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow { shouldLoadMore }
                    .distinctUntilChanged()
                    .collect { load ->
                        if (load) onLoadMore()
                    }
            }

            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    if (state.isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    items(
                        items = state.entries,
                        key = { it.id }
                    ) { entry ->
                        EntryBubble(
                            entry = entry,
                            isMine = entry.agent.id == state.currentAgentId
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSubmitEntry(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryBubble(
    entry: EntryWithAgent,
    isMine: Boolean
) {
    val isAdmin = entry.agent.type in adminTypes
    val agentIcon = getAgentIcon(entry.agent.type)
    val showLock = entry.agent.type !in unsecuredTypes

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
                isAdmin -> Color(0xFF1A1A2E)
                isMine -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Agent header: icon + name + lock + timestamp
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = agentIcon)
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isAdmin) {
                        AdminAgentName(entry.agent.name)
                    } else {
                        Text(
                            text = entry.agent.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = getAgentNameColor(entry.agent.type, isMine)
                        )
                    }
                    if (showLock) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "\uD83D\uDD12", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\u2022 ${formatEntryTimestamp(entry.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isAdmin -> Color(0xAAFFFFFF)
                            isMine -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
                // Entry content
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isAdmin -> Color.White
                        isMine -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminAgentName(name: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "admin_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "admin_alpha"
    )
    Text(
        text = name,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFFD700),
        modifier = Modifier.alpha(alpha)
    )
}

private fun getAgentIcon(type: String): String = when (type) {
    "Chillieman" -> "\uD83E\uDDD9\u200D\u2642\uFE0F"
    "ChillieZeph" -> "\uD83C\uDF00"
    "ChillieDae" -> "\uD83E\uDD8E"
    "Founder" -> "\uD83C\uDF0C"
    "Human" -> "\uD83E\uDD69"
    "AI" -> "\uD83E\uDD16"
    else -> "\uD83D\uDCAC"
}

@Composable
private fun getAgentNameColor(type: String, isMine: Boolean): Color = when (type) {
    "Human" -> Color(0xFF5C6BC0) // indigo-400, visible in both light and dark
    "AI" -> Color(0xFF4CAF50) // green-400
    else -> if (isMine) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
}

private val adminTypes = setOf("Chillieman", "ChillieZeph", "ChillieDae", "Founder")
private val unsecuredTypes = setOf("AI", "PUBLIC")

private fun formatEntryTimestamp(epochSeconds: Long): String {
    val instant = Instant.ofEpochSecond(epochSeconds)
    val formatter = DateTimeFormatter.ofPattern("M/d/yyyy '@' hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
