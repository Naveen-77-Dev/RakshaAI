package com.example.ui.screens.urlguard

import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RiskAssessment
import com.example.engine.RiskLevel
import com.example.engine.UrlAnalyzer
import com.example.ui.components.RiskMeter
import com.example.ui.components.getAdaptiveRiskContainerColor
import com.example.ui.theme.RiskCautionAmber
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskSafeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlGuardScreen(
    urlToInspect: String,
    onClose: () -> Unit,
    onOpenExternalBrowser: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val assessment = remember(urlToInspect) {
        UrlAnalyzer.analyze(urlToInspect)
    }

    var showUnsafeConfirmDialog by remember { mutableStateOf(false) }

    val isDangerous = assessment.riskLevel != RiskLevel.SAFE
    val isCritical = assessment.riskLevel == RiskLevel.CRITICAL || assessment.riskLevel == RiskLevel.HIGH_RISK

    val themeBackground = MaterialTheme.colorScheme.background
    val bannerBg = if (isDangerous) {
        if (isCritical) Color(0xFF381219) else Color(0xFF362810)
    } else {
        Color(0xFF0F2E22)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDangerous) Icons.Default.Shield else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDangerous) (if (isCritical) RiskCriticalRed else RiskCautionAmber) else RiskSafeGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDangerous) "RakshaAI Fraud Link Intercept" else "RakshaAI Link Shield",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
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
            // High-Impact Intercept Alert Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("url_guard_intercept_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bannerBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            if (isDangerous) RiskCriticalRed.copy(alpha = 0.8f) else RiskSafeGreen.copy(alpha = 0.8f),
                            if (isDangerous) RiskCautionAmber.copy(alpha = 0.4f) else RiskSafeGreen.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDangerous) RiskCriticalRed.copy(alpha = 0.25f) else RiskSafeGreen.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDangerous) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDangerous) (if (isCritical) Color(0xFFFCA5A5) else RiskCautionAmber) else RiskSafeGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isDangerous) "POTENTIAL FRAUD DETECTED" else "LINK VERIFIED AS SAFE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isDangerous) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isDangerous) {
                            "RakshaAI intercepted this link before it could load in your browser. This web destination exhibits deceptive characteristics known in financial and credential theft scams."
                        } else {
                            "This destination matches a recognized official web structure without known fraud signatures or deceptive subdomains."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // URL Details Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Target Web Address",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = urlToInspect,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Risk Meter
            RiskMeter(
                score = assessment.riskScore,
                level = assessment.riskLevel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Forensic Red Flags
            Text(
                text = "Detected Forensic Signals (${assessment.signals.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assessment.signals.forEach { signal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (signal.severity == com.example.engine.SignalSeverity.HIGH) Icons.Default.Warning else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (signal.severity == com.example.engine.SignalSeverity.HIGH) RiskCriticalRed else RiskCautionAmber,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = signal.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = signal.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (isDangerous) {
                // Primary Safe Action: BLOCK
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("url_guard_block_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RiskSafeGreen)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🛡️ Block Link & Return to Safety",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Copy Threat Dossier
                OutlinedButton(
                    onClick = {
                        val report = buildString {
                            append("🚨 RakshaAI Scam Link Intercept Report\n")
                            append("Target: $urlToInspect\n")
                            append("Risk Score: ${assessment.riskScore}/100 (${assessment.riskLevel})\n")
                            append("Category: ${assessment.scamCategory.displayName}\n")
                            assessment.signals.forEach { append("• ${it.name}: ${it.explanation}\n") }
                        }
                        clipboardManager.setText(AnnotatedString(report))
                        scope.launch {
                            snackbarHostState.showSnackbar("Threat dossier copied to clipboard")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Threat Dossier")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Unsafe Bypass Option
                OutlinedButton(
                    onClick = { showUnsafeConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("url_guard_proceed_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RiskCriticalRed)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Proceed Anyway (Unsafe)", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                // Safe URL Actions
                Button(
                    onClick = {
                        onOpenExternalBrowser(urlToInspect)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("url_guard_open_safe_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue to Verified Webpage",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Close Shield")
                }
            }
        }
    }

    // Confirmation dialog before opening a dangerous URL
    if (showUnsafeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUnsafeConfirmDialog = false },
            title = {
                Text("⚠️ Bypassing Safety Shield", fontWeight = FontWeight.Bold, color = RiskCriticalRed)
            },
            text = {
                Text(
                    "You are about to open a link that RakshaAI flagged as high risk (${assessment.riskScore}/100). " +
                    "Never enter bank credentials, credit card details, or UPI PINs on this webpage."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsafeConfirmDialog = false
                        onOpenExternalBrowser(urlToInspect)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalRed)
                ) {
                    Text("Proceed at My Own Risk", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnsafeConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
