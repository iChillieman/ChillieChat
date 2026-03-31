package com.chillieman.chilliechat.presentation.ui.screens.entries

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chillieman.chilliechat.presentation.ui.theme.AdminGold
import com.chillieman.chilliechat.presentation.ui.theme.AiGreen
import com.chillieman.chilliechat.presentation.ui.theme.DefaultRed
import com.chillieman.chilliechat.presentation.ui.theme.HumanIndigo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Reusable shimmer (works in both themes)
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translate by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    return this.then(
        Modifier.drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.55f),
                        Color.Transparent
                    ),
                    start = Offset(translate, 0f),
                    end = Offset(translate + 120f, size.height)
                )
            )
        }
    )
}

@Composable
fun PulsingAdminName(
    emoji: String,
    name: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(text = emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = AdminGold, // stays vibrant in both themes
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .shimmerEffect()
        )
    }
}

@Composable
fun EntriesScreen(
    viewModel: EntriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = state.entries, key = { it.id }) { entry ->
                        val isMine = entry.agent.id == state.currentAgentId
                        val hasSecret = entry.agent.capabilities?.contains("has_secret") == true

                        val timestamp = remember(entry.timestamp) {
                            val formatter = DateTimeFormatter.ofPattern("M/d/yyyy @ h:mm a")
                            Instant.ofEpochSecond(entry.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .format(formatter)
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Column(
                                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                                modifier = Modifier.widthIn(max = 320.dp)
                            ) {
                                // Agent header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    when (entry.agent.type) {
                                        "Chillieman", "ChillieZeph", "ChillieDae", "Founder" -> {
                                            PulsingAdminName(
                                                emoji = when (entry.agent.type) {
                                                    "Chillieman" -> "🧙‍♂️"
                                                    "ChillieZeph" -> "🌀"
                                                    "ChillieDae" -> "🦎"
                                                    else -> "🌌"
                                                },
                                                name = entry.agent.name
                                            )
                                        }
                                        "Human" -> {
                                            Text("🥩 ", fontSize = 18.sp)
                                            Text(
                                                text = entry.agent.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = HumanIndigo
                                            )
                                        }
                                        "AI" -> {
                                            Text("🤖 ", fontSize = 18.sp)
                                            Text(
                                                text = entry.agent.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = AiGreen
                                            )
                                        }
                                        else -> {
                                            Text("🥷 ", fontSize = 18.sp)
                                            Text(
                                                text = entry.agent.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = DefaultRed
                                            )
                                        }
                                    }

                                    if (hasSecret) {
                                        Text("🔒", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Text(
                                        text = "• $timestamp",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Message bubble
                                Surface(
                                    shape = if (isMine) {
                                        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                                    } else {
                                        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                                    },
                                    color = if (isMine) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        text = entry.content,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMine) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Input bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.submitEntry(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}