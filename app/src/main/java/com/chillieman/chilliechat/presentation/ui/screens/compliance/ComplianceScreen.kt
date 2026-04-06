package com.chillieman.chilliechat.presentation.ui.screens.compliance

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ComplianceScreen(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ComplianceViewModel = hiltViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val agreedToTerms by viewModel.agreedToTerms.collectAsStateWithLifecycle()

    ComplianceScreenContent(
        currentPage = currentPage,
        agreedToTerms = agreedToTerms,
        onNext = viewModel::nextPage,
        onPrevious = viewModel::previousPage,
        onToggleAgreed = viewModel::toggleAgreedToTerms,
        onConfirm = { viewModel.confirm(onComplete) },
        onCancel = onCancel
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ComplianceScreenContent(
    currentPage: Int,
    agreedToTerms: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleAgreed: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == currentPage) 10.dp else 8.dp),
                    shape = RoundedCornerShape(50),
                    color = if (index == currentPage)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ) {}
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentPage) {
                0 -> PageOne()
                1 -> PageTwo()
                2 -> PageThree(agreedToTerms = agreedToTerms, onToggleAgreed = onToggleAgreed)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when (currentPage) {
                0 -> {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel")
                    }
                    Button(onClick = onNext) {
                        Text("Next")
                    }
                }
                1 -> {
                    OutlinedButton(onClick = onPrevious) {
                        Text("Previous")
                    }
                    Button(onClick = onNext) {
                        Text("Next")
                    }
                }
                2 -> {
                    OutlinedButton(onClick = onPrevious) {
                        Text("Previous")
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = agreedToTerms
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun PageOne() {
    PolicyPageHeader(
        icon = Icons.Default.Flag,
        title = "AI Content Reporting"
    )

    PolicyQuoteCard(
        text = "Apps that generate content using AI must contain in-app user reporting " +
                "or flagging features that allow users to report or flag offensive content " +
                "to developers without needing to exit the app. Developers should utilize " +
                "user reports to inform content filtering and moderation in their apps."
    )

    Text(
        text = "You always have the ability to Flag any response from Dae / Zeph / " +
                "any participating AI:",
        style = MaterialTheme.typography.bodyLarge
    )

    NumberedStep(
        number = 1,
        text = "Press and hold the message until the Report Dialog appears."
    )
    NumberedStep(
        number = 2,
        text = "Press the Checkbox stating you understand the Report Action you are about to perform."
    )
    NumberedStep(
        number = 3,
        text = "Press Submit."
    )

    Text(
        text = "After you flag the Entry, it will now be Hidden for all other users from now on, " +
                "unless they choose to reveal it.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Text(
        text = "You can toggle default \"Hide Flagged Messages\" behavior in the Settings Screen inside the app.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PageTwo() {
    PolicyPageHeader(
        icon = Icons.Default.Block,
        title = "User Blocking & Moderation"
    )

    PolicyQuoteCard(
        text = "(App) Conduct User Generated Content (UGC) moderation, as is reasonable " +
                "and consistent with the type of UGC hosted by the app. This includes " +
                "providing an in-app system for reporting and blocking objectionable UGC " +
                "and users, and taking action against UGC or users where appropriate."
    )

    Text(
        text = "You always have the ability to Block any user using the Report Dialog:",
        style = MaterialTheme.typography.bodyLarge
    )

    NumberedStep(
        number = 1,
        text = "Press and hold the message until the Report Dialog appears."
    )
    NumberedStep(
        number = 2,
        text = "Press the Checkbox stating \"Block this user\"."
    )
    NumberedStep(
        number = 3,
        text = "Press Submit."
    )

    Text(
        text = "After you block that user, you will no longer see Entries from that User.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Text(
        text = "You can manage your block list on the Settings > Blocked Users screen inside the app.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PageThree(
    agreedToTerms: Boolean,
    onToggleAgreed: () -> Unit
) {
    val context = LocalContext.current

    PolicyPageHeader(
        icon = Icons.Default.Gavel,
        title = "Terms of Use"
    )

    PolicyQuoteCard(
        text = "Requires users accept the app's terms of use and/or user policy before " +
                "users can create or upload User Generated Content."
    )

    Text(
        text = "You must agree to our Terms of Use and Privacy Policies:",
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://chillieman.com/privacy".toUri())
                context.startActivity(intent)
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("Privacy Policy")
        }

        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://chillieman.com/terms".toUri())
                context.startActivity(intent)
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("Terms of Use")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.combinedClickable(onClick = onToggleAgreed)
    ) {
        Checkbox(
            checked = agreedToTerms,
            onCheckedChange = { onToggleAgreed() }
        )
        Text(
            text = "I agree to the Terms of Use and Privacy Policy, I promise I actually read them",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PolicyPageHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PolicyQuoteCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun NumberedStep(number: Int, text: String) {
    Row(
        modifier = Modifier.padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
