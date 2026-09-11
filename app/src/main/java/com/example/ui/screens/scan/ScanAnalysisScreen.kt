package com.example.ui.screens.scan

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RiskLevel
import com.example.localization.AppStrings
import com.example.ui.AnalysisStage
import com.example.ui.SafetyViewModel
import com.example.ui.components.ConfidenceLabel
import com.example.ui.components.DisclaimerPanel
import com.example.ui.components.RecommendedActionCard
import com.example.ui.components.RiskMeter
import com.example.ui.components.ScamCategoryBadge
import com.example.ui.components.ScamReasonCard
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAnalysisScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateEmergency: () -> Unit
) {
    val stage by viewModel.analysisStage.collectAsState()
    val isElderly = viewModel.isElderlyMode()
    val lang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showOpenAnywayDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RakshaAI Risk Assessment",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val currentStage = stage) {
                is AnalysisStage.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active scan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onBack) {
                            Text("Select an item to scan")
                        }
                    }
                }
                is AnalysisStage.Progress -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { currentStage.progress },
                            modifier = Modifier.size(72.dp),
                            strokeWidth = 6.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "Analyzing Threat Indicators",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentStage.step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(
                            progress = { currentStage.progress },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
                is AnalysisStage.Completed -> {
                    val assessment = currentStage.assessment
                    val upi = currentStage.upiPayload
                    val audio = currentStage.audioForensic

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Risk Meter
                        RiskMeter(
                            score = assessment.riskScore,
                            level = assessment.riskLevel,
                            isElderlyMode = isElderly
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category & Confidence
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ScamCategoryBadge(category = assessment.scamCategory)
                            ConfidenceLabel(confidence = assessment.confidence)
                        }

                        // Scanned Payload & Payee Details
                        if (upi != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            val isSafeUpi = assessment.riskLevel.isSafe
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSafeUpi) com.example.ui.theme.RiskSafeContainer else RiskCriticalContainer
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSafeUpi) com.example.ui.theme.RiskSafeGreen else RiskCriticalRed
                                    )
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSafeUpi) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isSafeUpi) com.example.ui.theme.RiskSafeGreen else RiskCriticalRed
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isSafeUpi) "VERIFIED UPI TRANSACTION DETAILS" else "CRITICAL UPI SAFETY WARNING",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSafeUpi) com.example.ui.theme.RiskSafeGreen else RiskCriticalRed
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isSafeUpi) {
                                            "Payee and transaction structure are normal. Standard rule: entering your UPI PIN will transfer money out of your account."
                                        } else {
                                            AppStrings.get("upi_pin_warning", lang)
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (isElderly) 16.sp else 14.sp
                                        ),
                                        color = if (isSafeUpi) MaterialTheme.colorScheme.onSurface else RiskCriticalRed
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Payee VPA: ${upi.payeeVpa.ifEmpty { "Standard VPA" }}\nMerchant / Name: ${upi.payeeName.ifEmpty { "Verified Payee" }}\nAmount: ${if (upi.amount.isNotEmpty()) "₹" + upi.amount else "Dynamic / User-specified"}\nNote: ${upi.transactionNote.ifEmpty { "Standard Transaction" }}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (assessment.inputPayload.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Scanned Content / URL:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = assessment.inputPayload,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        maxLines = 3
                                    )
                                }
                            }
                        }

                        // Audio Forensic breakdown
                        if (audio != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Voice & Conversation Diagnostics",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Voice Authenticity Risk:", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            audio.voiceAuthenticityRisk,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (audio.voiceAuthenticityRisk == "High") RiskCriticalRed else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Scam Intent & Urgency Risk:", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            audio.scamIntentRisk,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (audio.scamIntentRisk == "Critical" || audio.scamIntentRisk == "High") RiskCriticalRed else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (audio.syntheticFeaturesDetected.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Synthetic Acoustic Artifacts:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        audio.syntheticFeaturesDetected.forEach { feat ->
                                            Text("• $feat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Evidence & Reasons
                        Text(
                            text = "Detected Risk Signals (${assessment.signals.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderly) 18.sp else 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            assessment.signals.forEach { sig ->
                                ScamReasonCard(signal = sig, isElderlyMode = isElderly)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Recommended Actions
                        Text(
                            text = "Recommended Safe Next Actions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderly) 18.sp else 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            assessment.recommendedActions.forEach { act ->
                                RecommendedActionCard(action = act, isElderlyMode = isElderly)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Probabilistic Disclaimer
                        DisclaimerPanel(disclaimerText = assessment.disclaimer)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Notify Trusted Contact
                            Button(
                                onClick = {
                                    viewModel.sendTrustedContactAlert(assessment.scamCategory.displayName)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Alert simulated and sent to trusted family network")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isElderly) 56.dp else 48.dp)
                                    .testTag("notify_trusted_contact_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.AddAlert, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Alert Trusted Family Contact", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            // If URL, Open Anyway button with deliberate confirmation
                            if (currentStage.inputType == "URL") {
                                OutlinedButton(
                                    onClick = { showOpenAnywayDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isElderly) 54.dp else 46.dp)
                                        .testTag("open_anyway_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Destination (Review Warnings)")
                                }
                            }

                            // Report to Helpline 1930 / cybercrime.gov.in
                            Button(
                                onClick = onNavigateEmergency,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isElderly) 56.dp else 48.dp)
                                    .testTag("report_1930_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalRed)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Helpline 1930 & Cybercrime Portal", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Deliberate Confirmation Dialog for "Open Anyway"
                    if (showOpenAnywayDialog) {
                        AlertDialog(
                            onDismissRequest = { showOpenAnywayDialog = false },
                            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RiskCriticalRed) },
                            title = { Text("Proceed at Your Own Risk?") },
                            text = {
                                Text(
                                    "RakshaAI flagged this link with a risk score of ${assessment.riskScore}/100.\n\nNever enter OTPs, ATM PINs, bank passwords, or download any APK on the destination page."
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showOpenAnywayDialog = false
                                        try {
                                            val url = assessment.inputPayload
                                            val browserIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(if (url.startsWith("http")) url else "https://$url")
                                            )
                                            context.startActivity(browserIntent)
                                        } catch (_: Exception) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Unable to open external browser")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalRed)
                                ) {
                                    Text("I Understand, Open Link")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showOpenAnywayDialog = false }) {
                                    Text("Stay Safe / Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
