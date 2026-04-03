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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chillieman.chilliechat.R
import com.chillieman.chilliechat.domain.model.EntryWithAgent
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun EntriesScreen(
    viewModel: EntriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { ChimeSoundPool.init(context) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EntriesScreenContent(
        uiState = uiState,
        onSubmitEntry = viewModel::submitEntry,
        onLoadMore = viewModel::loadOlderEntries,
        onReportEntry = viewModel::reportEntry,
        onRevealEntry = viewModel::revealEntry,
        onSetAlwaysShowReported = viewModel::setAlwaysShowReported,
        onDismissReportTip = viewModel::dismissReportTip
    )
}

@Composable
internal fun EntriesScreenContent(
    uiState: EntriesUiState,
    onSubmitEntry: (String) -> Unit,
    onLoadMore: () -> Unit = {},
    onReportEntry: (Int) -> Unit = {},
    onRevealEntry: (Int) -> Unit = {},
    onSetAlwaysShowReported: (Boolean) -> Unit = {},
    onDismissReportTip: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var reportDialogEntryId by remember { mutableStateOf<Int?>(null) }
    var uncensorDialogEntryId by remember { mutableStateOf<Int?>(null) }
    var showReportTipDialog by remember { mutableStateOf(false) }

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
            // Show report tip dialog on every visit until dismissed
            LaunchedEffect(state.reportTipDismissed) {
                if (!state.reportTipDismissed) {
                    showReportTipDialog = true
                }
            }

            val context = LocalContext.current
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            var initialScrollDone by remember { mutableStateOf(false) }
            var showNewMessageButton by remember { mutableStateOf(false) }
            var newMessageDividerAfterEntryId by remember { mutableStateOf<Int?>(null) }

            val isNearBottom by remember {
                derivedStateOf {
                    val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = listState.layoutInfo.totalItemsCount
                    totalItems - lastVisibleIndex <= 5
                }
            }

            // Hide the button when user scrolls to bottom (divider stays)
            LaunchedEffect(Unit) {
                snapshotFlow { isNearBottom }
                    .distinctUntilChanged()
                    .collect { nearBottom ->
                        if (nearBottom) showNewMessageButton = false
                    }
            }

            val lastEntry = state.entries.lastOrNull()
            val lastEntryId = lastEntry?.id
            var previousLastEntryId by remember { mutableStateOf<Int?>(null) }
            LaunchedEffect(lastEntryId) {
                if (lastEntryId != null && state.entries.isNotEmpty()) {
                    if (!initialScrollDone) {
                        // Snap to bottom on first arrival / initial load
                        listState.scrollToItem(state.entries.size - 1)
                        initialScrollDone = true
                    } else if (isNearBottom) {
                        // User is at bottom and caught up — clear divider, auto-scroll
                        newMessageDividerAfterEntryId = null
                        listState.animateScrollToItem(state.entries.size - 1)
                    } else {
                        // User is reading older history — don't interrupt, show button
                        showNewMessageButton = true
                        // Only set divider if one isn't already showing
                        if (newMessageDividerAfterEntryId == null && previousLastEntryId != null) {
                            newMessageDividerAfterEntryId = previousLastEntryId
                        }
                    }
                    // Chime or vibrate for new messages from other agents
                    val isFromOtherAgent = lastEntry?.agentId != state.currentAgentId
                    if (previousLastEntryId != null && isFromOtherAgent && state.soundEnabled) {
                        notifyNewMessage(context)
                    }
                    previousLastEntryId = lastEntryId
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
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
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

                        state.entries.forEach { entry ->
                            item(key = entry.id) {
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

                            // Insert "New Messages" divider after the last old entry
                            if (entry.id == newMessageDividerAfterEntryId) {
                                item(key = "new_messages_divider") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "  New Messages  ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating "View New Messages" button
                    if (showNewMessageButton) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            TextButton(
                                onClick = {
                                    showNewMessageButton = false
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(state.entries.size - 1)
                                    }
                                }
                            ) {
                                Text(
                                    text = "\u2B07 View New Messages",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (state.isArchived) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A1A2E)
                    ) {
                        Text(
                            text = "[SIGNAL ARCHIVED]",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0BEC5),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
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

    // Report Tip Dialog — shown every visit until permanently dismissed
    if (showReportTipDialog) {
        ReportTipDialog(
            onDismiss = { dontRemindAgain ->
                showReportTipDialog = false
                if (dontRemindAgain) {
                    onDismissReportTip()
                }
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
private fun ReportTipDialog(
    onDismiss: (dontRemindAgain: Boolean) -> Unit
) {
    var dontRemind by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(dontRemind) },
        title = { Text("Tip") },
        text = {
            Column {
                Text("Press and Hold any message to report it.")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.combinedClickable(onClick = { dontRemind = !dontRemind })
                ) {
                    Checkbox(
                        checked = dontRemind,
                        onCheckedChange = { dontRemind = it }
                    )
                    Text(
                        "Don't Remind Me Again",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss(dontRemind) }) {
                Text("Got it!")
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
    val isChillieman = entry.agent.type == "Chillieman"
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
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .combinedClickable(
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

private object ChimeSoundPool {
    private var soundPool: SoundPool? = null
    private var soundIds: List<Int> = emptyList()
    private var loadedCount = 0
    private const val TOTAL_CHIMES = 10

    fun init(context: Context) {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        pool.setOnLoadCompleteListener { _, _, status -> if (status == 0) loadedCount++ }
        soundIds = listOf(
            pool.load(context, R.raw.chime_1, 1),
            pool.load(context, R.raw.chime_2, 1),
            pool.load(context, R.raw.chime_3, 1),
            pool.load(context, R.raw.chime_4, 1),
            pool.load(context, R.raw.chime_5, 1),
            pool.load(context, R.raw.chime_6, 1),
            pool.load(context, R.raw.chime_7, 1),
            pool.load(context, R.raw.chime_8, 1),
            pool.load(context, R.raw.chime_9, 1),
            pool.load(context, R.raw.chime_10, 1)
        )
        soundPool = pool
    }


    suspend fun playChillieChime() {
        if (loadedCount < TOTAL_CHIMES || soundIds.isEmpty()) return
        val pool = soundPool ?: return
        val pickSize = Random.nextInt(2,4)
        val picks = List(pickSize) { soundIds.random() }
        picks.forEachIndexed { index, soundId ->
            val rate = Random.nextDouble(0.5, 1.0).toFloat()
            pool.play(soundId, 0.15f, 0.15f, 1, 0, rate)
            val delay = Random.nextLong(100, 150)
            if (index < 2) kotlinx.coroutines.delay(delay)
        }
    }
}

private suspend fun notifyNewMessage(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    when (audioManager.ringerMode) {
        AudioManager.RINGER_MODE_SILENT -> Unit // User has it on silent, don't vibrate or chime
        AudioManager.RINGER_MODE_NORMAL -> {
            try {
                ChimeSoundPool.playChillieChime()
            } catch (_: Exception) { }
        }
        else -> {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    manager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) { }
        }
    }
}
