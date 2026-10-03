package com.example.ui.screens.scan

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RiskAssessment
import com.example.engine.RiskLevel
import com.example.engine.RiskSignal
import com.example.engine.UpiPayload
import com.example.engine.AudioForensicResult
import com.example.ui.AnalysisStage
import com.example.ui.SafetyViewModel
import kotlinx.coroutines.launch

// Premium Cybersecurity Mobile Color Palette
private val ColorBgDark = Color(0xFF090D16)
private val ColorCardSurface = Color(0xFF111827)
private val ColorCardBorder = Color(0xFF1F2937)

private val SafeGreen = Color(0xFF10B981)
private val SafeGreenBg = Color(0xFF064E3B).copy(alpha = 0.35f)
private val SafeGreenBorder = Color(0xFF10B981).copy(alpha = 0.45f)

private val CautionAmber = Color(0xFFF59E0B)
private val CautionAmberBg = Color(0xFF78350F).copy(alpha = 0.35f)
private val CautionAmberBorder = Color(0xFFF59E0B).copy(alpha = 0.45f)

private val HighOrange = Color(0xFFEA580C)
private val HighOrangeBg = Color(0xFF7C2D12).copy(alpha = 0.35f)
private val HighOrangeBorder = Color(0xFFEA580C).copy(alpha = 0.45f)

private val CriticalRed = Color(0xFFEF4444)
private val CriticalRedBg = Color(0xFF7F1D1D).copy(alpha = 0.35f)
private val CriticalRedBorder = Color(0xFFEF4444).copy(alpha = 0.50f)

private val ActionBlue = Color(0xFF3B82F6)

@Composable
fun ScanAnalysisScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateEmergency: () -> Unit
) {
    val stage by viewModel.analysisStage.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showOpenAnywayDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBgDark,
        topBar = {
            // Section 3: Compact Mobile Header (52–56dp height)
            MobileHeader(onBack = onBack)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ColorBgDark)
        ) {
            when (val currentStage = stage) {
                is AnalysisStage.Idle -> {
                    MobileIdleState(onBack = onBack)
                }
                is AnalysisStage.Progress -> {
                    MobileProgressState(stage = currentStage)
                }
                is AnalysisStage.Completed -> {
                    MobileCompletedAnalysis(
                        assessment = currentStage.assessment,
                        upi = currentStage.upiPayload,
                        audio = currentStage.audioForensic,
                        inputType = currentStage.inputType,
                        onAlertContact = {
                            viewModel.sendTrustedContactAlert(currentStage.assessment.scamCategory.displayName)
                            scope.launch {
                                snackbarHostState.showSnackbar("Alert dispatched to trusted family contact")
                            }
                        },
                        onNavigateEmergency = onNavigateEmergency,
                        onOpenAnywayRequested = { showOpenAnywayDialog = true },
                        onCopyText = { text ->
                            clipboard.setText(AnnotatedString(text))
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard")
                            }
                        }
                    )

                    // Deliberate Confirmation Dialog for "Open Anyway"
                    if (showOpenAnywayDialog) {
                        AlertDialog(
                            onDismissRequest = { showOpenAnywayDialog = false },
                            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed) },
                            title = {
                                Text(
                                    "Proceed with Caution?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            text = {
                                Text(
                                    "RakshaAI flagged this content with a risk score of ${currentStage.assessment.riskScore}/100.\n\nNever share passwords, OTPs, or UPI PIN on unverified websites.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showOpenAnywayDialog = false
                                        try {
                                            val url = currentStage.assessment.inputPayload
                                            val target = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
                                            context.startActivity(browserIntent)
                                        } catch (_: Exception) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Unable to open browser")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                                ) {
                                    Text("Open Destination")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showOpenAnywayDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 3: Compact Mobile Header
 * Exactly 54dp high with consistent 16dp horizontal padding.
 */
@Composable
private fun MobileHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        color = ColorBgDark,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SafeGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RakshaAI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFFF8FAFC)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                )
            }

            // Right-aligned status pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(SafeGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Real-time",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/**
 * Section 2 & 4–11: Single-Column Responsive Mobile Analysis Layout
 */
@Composable
private fun MobileCompletedAnalysis(
    assessment: RiskAssessment,
    upi: UpiPayload?,
    audio: AudioForensicResult?,
    inputType: String,
    onAlertContact: () -> Unit,
    onNavigateEmergency: () -> Unit,
    onOpenAnywayRequested: () -> Unit,
    onCopyText: (String) -> Unit
) {
    val isSafe = assessment.riskLevel == RiskLevel.SAFE
    val riskColor = when (assessment.riskLevel) {
        RiskLevel.SAFE -> SafeGreen
        RiskLevel.CAUTION -> CautionAmber
        RiskLevel.HIGH_RISK -> HighOrange
        RiskLevel.CRITICAL -> CriticalRed
        RiskLevel.UNKNOWN -> CautionAmber
    }

    val verdictContainerBg = when (assessment.riskLevel) {
        RiskLevel.SAFE -> SafeGreenBg
        RiskLevel.CAUTION -> CautionAmberBg
        RiskLevel.HIGH_RISK -> HighOrangeBg
        RiskLevel.CRITICAL -> CriticalRedBg
        RiskLevel.UNKNOWN -> ColorCardSurface
    }

    val verdictBorderColor = when (assessment.riskLevel) {
        RiskLevel.SAFE -> SafeGreenBorder
        RiskLevel.CAUTION -> CautionAmberBorder
        RiskLevel.HIGH_RISK -> HighOrangeBorder
        RiskLevel.CRITICAL -> CriticalRedBorder
        RiskLevel.UNKNOWN -> ColorCardBorder
    }

    var isDisclaimerExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("scan_analysis_completed_column")
    ) {

        // ==========================================
        // SECTION 4: MAIN VERDICT CARD
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = ColorCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, verdictBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Status Icon Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(verdictContainerBg, CircleShape)
                        .border(1.5.dp, riskColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSafe) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Large Status Label (28–34px)
                Text(
                    text = when (assessment.riskLevel) {
                        RiskLevel.SAFE -> "SAFE"
                        RiskLevel.CAUTION -> "CAUTION"
                        RiskLevel.HIGH_RISK -> "HIGH RISK"
                        RiskLevel.CRITICAL -> "CRITICAL THREAT"
                        RiskLevel.UNKNOWN -> "UNVERIFIED"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        letterSpacing = 0.5.sp,
                        color = riskColor
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle Descriptor
                Text(
                    text = when (assessment.riskLevel) {
                        RiskLevel.SAFE -> "Low Risk Content"
                        RiskLevel.CAUTION -> "Suspicious Attributes Detected"
                        RiskLevel.HIGH_RISK -> "Dangerous Threat Signatures"
                        RiskLevel.CRITICAL -> "Active Scam / Fraud Pattern"
                        RiskLevel.UNKNOWN -> "Unknown Verification State"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Concise Explanation
                Text(
                    text = if (isSafe) {
                        "No suspicious indicators were detected in this assessment."
                    } else {
                        assessment.signals.firstOrNull()?.explanation
                            ?: "High deception probability identified by forensic heuristics."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // SECTION 5: RISK SCORE & HORIZONTAL METER
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ColorCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Risk Score",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                    Text(
                        text = "${assessment.riskScore} / 100",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = riskColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clean Horizontal Segmented Meter
                MobileRiskMeterBar(score = assessment.riskScore)

                Spacer(modifier = Modifier.height(6.dp))

                // Meter Labels (Fit comfortably on 360px without overlap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SAFE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = SafeGreen, fontWeight = FontWeight.Bold))
                    Text("CAUTION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CautionAmber, fontWeight = FontWeight.Bold))
                    Text("HIGH", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = HighOrange, fontWeight = FontWeight.Bold))
                    Text("CRITICAL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CriticalRed, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // SECTION 6: AI CONFIDENCE & STATUS ROW
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Confidence
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ActionBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Confidence",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(assessment.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }

                // Detection Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isSafe) SafeGreen else CriticalRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSafe) "No threats found" else "Threat flagged",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSafe) SafeGreen else CriticalRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // SECTION 7: DETECTED RISK SIGNALS
        // ==========================================
        Text(
            text = "Detected Risk Signals",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFFF8FAFC)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (assessment.signals.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ColorCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No suspicious signals detected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assessment.signals.forEach { signal ->
                    MobileSignalItem(signal = signal)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // SECTION 8: SCANNED CONTENT / PAYLOAD
        // ==========================================
        Text(
            text = "Scanned Content",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFFF8FAFC)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = ColorCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // If structured UPI payload exists
                if (upi != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPI Payee:",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                        IconButton(
                            onClick = { onCopyText(upi.payeeVpa) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy VPA",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Text(
                        text = upi.payeeVpa.ifEmpty { "Standard UPI Payee" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    )
                    if (upi.payeeName.isNotEmpty() || upi.amount.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Name: ${upi.payeeName.ifEmpty { "N/A" }}  •  Amount: ${if (upi.amount.isNotEmpty()) "₹${upi.amount}" else "Dynamic"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 12.sp)
                        )
                    }
                } else if (audio != null) {
                    // Audio forensic preview
                    Text(
                        text = "Voice Diagnostics:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Authenticity: ${audio.voiceAuthenticityRisk}  •  Intent: ${audio.scamIntentRisk}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFF8FAFC), fontSize = 12.sp)
                    )
                } else {
                    // Regular URL or Message text
                    val displayText = assessment.inputPayload.ifBlank { "Input content analyzed" }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onCopyText(displayText) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Content",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // SECTION 9: RECOMMENDED ACTION
        // ==========================================
        Text(
            text = "Recommended Action",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFFF8FAFC)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = ColorCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (isSafe) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SafeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No immediate action required",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC),
                                fontSize = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Continue using standard digital safety practices.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    )
                } else {
                    assessment.recommendedActions.take(3).forEachIndexed { idx, action ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CautionAmber
                                )
                            )
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // SECTION 10: COLLAPSIBLE SAFETY DISCLAIMER
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDisclaimerExpanded = !isDisclaimerExpanded },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About this assessment",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                    Icon(
                        imageVector = if (isDisclaimerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isDisclaimerExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = assessment.disclaimer.ifBlank {
                                "RakshaAI provides an automated on-device risk assessment and cannot guarantee that content is completely safe. Always verify unexpected financial requests through trusted official channels."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // SECTION 11: EMERGENCY & PRIMARY ACTIONS
        // Full width, minimum 50dp height touch targets
        // ==========================================
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Primary Safety Action: Alert Family Contact
            Button(
                onClick = onAlertContact,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("notify_trusted_contact_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alert Trusted Contact",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            // Optional URL "Open Anyway" Button
            if (inputType == "URL") {
                OutlinedButton(
                    onClick = onOpenAnywayRequested,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_anyway_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Link (Review Warnings)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            // Secondary Emergency Action: Helpline 1930
            // Uses subtle emergency styling rather than overly aggressive red unless critical
            Button(
                onClick = onNavigateEmergency,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("report_1930_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSafe) Color(0xFF1E293B) else Color(0xFF991B1B)
                ),
                border = if (isSafe) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)) else null
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = if (isSafe) Color(0xFFE2E8F0) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cybercrime Helpline 1930",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) Color(0xFFE2E8F0) else Color.White,
                        fontSize = 14.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Section 5: Minimal horizontal risk meter bar with pointer indicator
 */
@Composable
private fun MobileRiskMeterBar(score: Int) {
    val clamped = score.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped / 100f,
        label = "riskMeterProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E293B))
    ) {
        // Gradient track spanning Safe to Critical
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            SafeGreen,
                            CautionAmber,
                            HighOrange,
                            CriticalRed
                        )
                    )
                )
        )

        // Overlay mask to reflect exact score
        if (animatedProgress < 1f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f - animatedProgress)
                    .height(8.dp)
                    .align(Alignment.CenterEnd)
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            )
        }
    }
}

/**
 * Section 7: Compact Risk Signal Row
 */
@Composable
private fun MobileSignalItem(signal: RiskSignal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ColorCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CautionAmberBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = CautionAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = signal.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        fontSize = 13.sp
                    )
                )
                if (signal.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = signal.explanation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Clean Mobile Idle State
 */
@Composable
private fun MobileIdleState(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = ActionBlue,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Active Assessment",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scan a message, link, QR code, or audio clip to view real-time cybersecurity diagnostics.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
        ) {
            Text("Return to Hub")
        }
    }
}

/**
 * Clean Mobile Scanning State
 */
@Composable
private fun MobileProgressState(stage: AnalysisStage.Progress) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = { stage.progress },
            modifier = Modifier.size(64.dp),
            strokeWidth = 5.dp,
            color = ActionBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Running Forensic Scan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC),
                fontSize = 17.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stage.step,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        )
        Spacer(modifier = Modifier.height(18.dp))
        LinearProgressIndicator(
            progress = { stage.progress },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = ActionBlue,
            trackColor = Color(0xFF1E293B)
        )
    }
}
