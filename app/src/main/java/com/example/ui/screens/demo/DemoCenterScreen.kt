package com.example.ui.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DemoScenario
import com.example.engine.DemoScenarios
import com.example.ui.SafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoCenterScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onLaunchAnalysis: () -> Unit
) {
    val guidedStep by viewModel.guidedTourStep.collectAsState()
    var currentTourIndex by remember { mutableIntStateOf(0) }
    val scenarios = DemoScenarios.SCENARIOS

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Threat Lab & Signature Simulator", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Threat Vector Evaluation Controller Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Threat Model Verification Suite",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vector ${currentTourIndex + 1} of ${scenarios.size}: ${scenarios[currentTourIndex].title}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scenarios[currentTourIndex].explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentTourIndex > 0) currentTourIndex--
                            },
                            enabled = currentTourIndex > 0
                        ) {
                            Text("Prev")
                        }

                        Button(
                            onClick = {
                                val s = scenarios[currentTourIndex]
                                launchScenario(s, viewModel, onLaunchAnalysis)
                            },
                            modifier = Modifier.testTag("run_guided_tour_step_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Step ${currentTourIndex + 1}")
                        }

                        OutlinedButton(
                            onClick = {
                                if (currentTourIndex < scenarios.size - 1) currentTourIndex++
                            },
                            enabled = currentTourIndex < scenarios.size - 1
                        ) {
                            Text("Next")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All 8 Seeded Scenarios (1-Tap Runner)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Each scenario simulates high-risk or legitimate Indian digital financial situations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                scenarios.forEach { scenario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { launchScenario(scenario, viewModel, onLaunchAnalysis) }
                            .testTag("scenario_card_${scenario.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${scenario.guidedStepIndex ?: "#"}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(scenario.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(scenario.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Target: ${scenario.expectedCategory.displayName} (${scenario.expectedScoreRange.first}–${scenario.expectedScoreRange.last})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = { launchScenario(scenario, viewModel, onLaunchAnalysis) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Test")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun launchScenario(
    scenario: DemoScenario,
    viewModel: SafetyViewModel,
    onLaunchAnalysis: () -> Unit
) {
    when (scenario.type) {
        "URL" -> viewModel.startUrlAnalysis(scenario.inputContent)
        "QR" -> viewModel.startQrAnalysis(scenario.inputContent)
        "MESSAGE" -> viewModel.startMessageAnalysis(scenario.inputContent)
        "AUDIO" -> viewModel.startAudioAnalysis(scenario.inputContent, 12)
        "PAYMENT" -> {
            viewModel.startPaymentEvaluation(
                com.example.engine.PaymentContext(
                    amount = 180.0,
                    recipientName = "Ramesh Kirana Store",
                    upiId = "ramesh.groceries@okhdfcbank",
                    paymentReason = "Daily groceries",
                    isNewRecipient = false,
                    hasUrgencyOrFear = false,
                    requestedOtpOrPinOrScreenShare = false,
                    claimedImpersonationAuthority = false
                )
            )
        }
    }
    onLaunchAnalysis()
}
