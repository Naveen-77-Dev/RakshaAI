package com.example.ui.screens.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PaymentContext
import com.example.engine.PaymentRiskAnalyzer
import com.example.engine.RiskLevel
import com.example.ui.SafetyViewModel
import com.example.ui.components.RiskMeter
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSafetyScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateToEmergency: () -> Unit
) {
    var amountText by remember { mutableStateOf("25000") }
    var recipientName by remember { mutableStateOf("Inspector Sharma / Bail Desk") }
    var upiId by remember { mutableStateOf("delhicrimebranch.bail@icici") }
    var paymentReason by remember { mutableStateOf("Emergency security deposit to avoid arrest") }

    var isNewRecipient by remember { mutableStateOf(true) }
    var hasUrgency by remember { mutableStateOf(true) }
    var requestedOtpOrScreenShare by remember { mutableStateOf(true) }
    var claimedAuthority by remember { mutableStateOf(true) }
    var isReceivingMoneyMisconception by remember { mutableStateOf(false) }

    var showUnderstandRiskDialog by remember { mutableStateOf(false) }
    var cooldownSeconds by remember { mutableIntStateOf(300) } // 5-minute pause timer
    var isCooldownActive by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isElderly = viewModel.isElderlyMode()

    // 5-minute cooldown countdown
    LaunchedEffect(isCooldownActive) {
        if (isCooldownActive) {
            while (cooldownSeconds > 0) {
                delay(1000)
                cooldownSeconds--
            }
        }
    }

    val liveAssessment = remember(
        amountText, recipientName, upiId, paymentReason,
        isNewRecipient, hasUrgency, requestedOtpOrScreenShare, claimedAuthority, isReceivingMoneyMisconception
    ) {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        PaymentRiskAnalyzer.evaluate(
            PaymentContext(
                amount = amount,
                recipientName = recipientName,
                upiId = upiId,
                paymentReason = paymentReason,
                isNewRecipient = isNewRecipient,
                hasUrgencyOrFear = hasUrgency,
                requestedOtpOrPinOrScreenShare = requestedOtpOrScreenShare,
                claimedImpersonationAuthority = claimedAuthority,
                isReceivingMoneyMisconception = isReceivingMoneyMisconception
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Before You Pay • Safety Guard", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
            // Real-time Risk Dial
            RiskMeter(
                score = liveAssessment.riskScore,
                level = liveAssessment.riskLevel,
                isElderlyMode = isElderly
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cooldown Timer Banner if high risk
            if (liveAssessment.riskLevel == RiskLevel.HIGH_RISK || liveAssessment.riskLevel == RiskLevel.CRITICAL) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = RiskCriticalContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = RiskCriticalRed, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mandatory 5-Minute Cool-Off Recommended",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RiskCriticalRed)
                            )
                            val mins = cooldownSeconds / 60
                            val secs = cooldownSeconds % 60
                            Text(
                                text = "Pause remaining: ${String.format("%02d:%02d", mins, secs)} — Scammers rely on rushed actions.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = RiskCriticalRed)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Payment Details Form
            Text(
                text = "Transaction Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Transfer Amount (₹)") },
                modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Recipient Name / Business") },
                modifier = Modifier.fillMaxWidth().testTag("payment_recipient_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("Recipient UPI ID / VPA") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = paymentReason,
                onValueChange = { paymentReason = it },
                label = { Text("Reason for Payment") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Behavioral Risk Questions
            Text(
                text = "Safety Questionnaire",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Toggle all conditions that apply to this situation:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            QuestionToggle(
                label = "Is this the first time paying this person/business?",
                checked = isNewRecipient,
                onCheckedChange = { isNewRecipient = it }
            )
            QuestionToggle(
                label = "Did they demand immediate transfer within 1 hour?",
                checked = hasUrgency,
                onCheckedChange = { hasUrgency = it }
            )
            QuestionToggle(
                label = "Did they request OTP, PIN, or AnyDesk screen sharing?",
                checked = requestedOtpOrScreenShare,
                onCheckedChange = { requestedOtpOrScreenShare = it }
            )
            QuestionToggle(
                label = "Claimed to be police, bank official, courier, or relative?",
                checked = claimedAuthority,
                onCheckedChange = { claimedAuthority = it }
            )
            QuestionToggle(
                label = "Are you trying to RECEIVE money instead of sending?",
                checked = isReceivingMoneyMisconception,
                onCheckedChange = { isReceivingMoneyMisconception = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Verification Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pre-Payment Verification Checklist",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text("✓ Never enter your UPI PIN if someone is supposedly sending you money.", style = MaterialTheme.typography.bodySmall)
                    Text("✓ Police, customs, and banks NEVER demand settlement via personal UPI VPAs.", style = MaterialTheme.typography.bodySmall)
                    Text("✓ Send a ₹1 trial transfer first to verify the merchant's legal registered name.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.sendTrustedContactAlert("Pre-payment risk flagged for ₹$amountText to $recipientName")
                        scope.launch {
                            snackbarHostState.showSnackbar("Alert simulated and sent to your trusted family contact.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("alert_family_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consult Family Member Before Paying", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                if (liveAssessment.riskLevel == RiskLevel.HIGH_RISK || liveAssessment.riskLevel == RiskLevel.CRITICAL) {
                    OutlinedButton(
                        onClick = { showUnderstandRiskDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("understand_risk_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed Anyway (I Understand The Risk)", color = RiskCriticalRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Safe Simulation Disclaimer
            Text(
                text = "Safe Simulation: RakshaAI never connects to bank accounts or processes real transactions. All evaluations are purely educational local assessments.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Confirmation Dialog
        if (showUnderstandRiskDialog) {
            AlertDialog(
                onDismissRequest = { showUnderstandRiskDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RiskCriticalRed) },
                title = { Text("High Fraud Risk Acknowledgment") },
                text = {
                    Text(
                        "You are considering sending ₹$amountText to $recipientName.\n\n" +
                                "RakshaAI strongly advises NOT proceeding. If this involves digital arrest, investment returns, or emergency calls, you are likely targeted by organized fraudsters."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showUnderstandRiskDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Logged user override to local safety timeline.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalRed)
                    ) {
                        Text("I Acknowledge Risk")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showUnderstandRiskDialog = false }) {
                        Text("Cancel Payment")
                    }
                }
            )
        }
    }
}

@Composable
private fun QuestionToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
