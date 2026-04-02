package com.chillieman.chilliechat.presentation.ui.screens.entries

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontStyle
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
        onLoadMore = viewModel::loadOlderEntries,
        onReportEntry = viewModel::reportEntry,
        onRevealEntry = viewModel::revealEntry,
        onSetAlwaysShowReported = viewModel::setAlwaysShowReported
    )
}

@Composable
internal fun EntriesScreenContent(
    uiState: EntriesUiState,
    onSubmitEntry: (String) -> Unit,
    onLoadMore: () -> Unit = {},
    onReportEntry: (Int) -> Unit = {},
    onRevealEntry: (Int) -> Unit = {},
    onSetAlwaysShowReported: (Boolean) -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var reportDialogEntryId by remember { mutableStateOf<Int?>(null) }
    var uncensorDialogEntryId by remember { mutableStateOf<Int?>(null) }

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
            var initialScrollDone by remember { mutableStateOf(false) }

            val lastEntryId = state.entries.lastOrNull()?.id
            LaunchedEffect(lastEntryId) {
                if (lastEntryId != null && state.entries.isNotEmpty()) {
                    if(!initialScrollDone) {
                        // Snap to bottom on first arrival/ initial load
                        listState.scrollToItem(state.entries.size - 1)
                        initialScrollDone = true
                    } else {
                        // If new entries arrive vai WebSocket...
                        // Only scroll to the very bottom if the user is AT the bottom
                        // (If the user is reading older history don't scroll them to the bottom...)
                        listState.animateScrollToItem(state.entries.size - 1)
                    }
                }
            }

            val shouldLoadMore by remember {
                derivedStateOf {
                    val firstVisible = listState.firstVisibleItemIndex
                    initialScrollDone && firstVisible <= 2 && state.hasMore && !state.isLoadingMore
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow { shouldLoadMore }
                    .distinctUntilChanged()
                    .collect { load ->
                        if (load) onLoadMore()
                    }
            }

            Column(modifier = Modifier.fillMaxSize()) {
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
                        val isContentHidden = entry.isReported
                                && !state.alwaysShowReported
                                && entry.id !in state.revealedEntryIds

                        EntryBubble(
                            entry = entry,
                            isMine = entry.agent.id == state.currentAgentId,
                            isContentHidden = isContentHidden,
                            onLongPress = { reportDialogEntryId = entry.id },
                            onShowAnyways = { uncensorDialogEntryId = entry.id }
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

    // Report Confirmation Dialog
    reportDialogEntryId?.let { entryId ->
        ReportDialog(
            onDismiss = { reportDialogEntryId = null },
            onConfirm = {
                onReportEntry(entryId)
                reportDialogEntryId = null
            }
        )
    }

    // Uncensor Dialog
    uncensorDialogEntryId?.let { entryId ->
        UncensorDialog(
            onDismiss = { uncensorDialogEntryId = null },
            onReveal = { alwaysShow ->
                onRevealEntry(entryId)
                if (alwaysShow) {
                    onSetAlwaysShowReported(true)
                }
                uncensorDialogEntryId = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var understood by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Message") },
        text = {
            Column {
                Text("Are you sure you want to report this message as inappropriate or offensive?")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.combinedClickable(onClick = { understood = !understood })
                ) {
                    Checkbox(
                        checked = understood,
                        onCheckedChange = { understood = it }
                    )
                    Text("I Understand", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = understood
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun UncensorDialog(
    onDismiss: () -> Unit,
    onReveal: (alwaysShow: Boolean) -> Unit
) {
    var wantToReveal by remember { mutableStateOf(false) }
    var alwaysShow by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reported Content") },
        text = {
            Column {
                Text("This message was reported as inappropriate or misleading. Are you sure you want to view it?")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.combinedClickable(onClick = {
                        wantToReveal = !wantToReveal
                    })
                ) {
                    Checkbox(
                        checked = wantToReveal,
                        onCheckedChange = { wantToReveal = it }
                    )
                    Text(
                        "Yes, I want to reveal this message",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.combinedClickable(onClick = { alwaysShow = !alwaysShow })
                ) {
                    Checkbox(
                        checked = alwaysShow,
                        onCheckedChange = { alwaysShow = it }
                    )
                    Text(
                        "Always Show Reported Messages",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onReveal(alwaysShow) },
                enabled = wantToReveal
            ) {
                Text("Reveal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryBubble(
    entry: EntryWithAgent,
    isMine: Boolean,
    isContentHidden: Boolean = false,
    onLongPress: () -> Unit = {},
    onShowAnyways: () -> Unit = {}
) {
    val isAdmin = entry.agent.type in adminTypes
    val agentIcon = getAgentIcon(entry.agent.type)
    val showLock = entry.agent.capabilities?.contains("has_secret") == true

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
            },
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Agent header: icon + name + lock + flag + timestamp
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
                    // Red flag for reported entries
                    if (entry.isReported) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Reported",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFE53935)
                        )
                    }
                }
                // Entry content
                if (entry.isDeleted) {
                    Text(
                        text = "\u26A0\uFE0F This entry was removed for abuse.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = when {
                            isAdmin -> Color.White.copy(alpha = 0.5f)
                            isMine -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                } else if (isContentHidden) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Message has been reported.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = when {
                                isAdmin -> Color.White.copy(alpha = 0.5f)
                                isMine -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        OutlinedButton(
                            onClick = onShowAnyways,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Show Anyways",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                } else {
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
    "Human" -> Color(0xFF5C6BC0)
    "AI" -> Color(0xFF4CAF50)
    else -> if (isMine) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
}

private val adminTypes = setOf("Chillieman", "ChillieZeph", "ChillieDae", "Founder")

private fun formatEntryTimestamp(epochSeconds: Long): String {
    val instant = Instant.ofEpochSecond(epochSeconds)
    val formatter = DateTimeFormatter.ofPattern("M/d/yyyy '@' hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
