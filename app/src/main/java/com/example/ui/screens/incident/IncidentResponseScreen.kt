package com.example.ui.screens.incident

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.IncidentEntity
import com.example.ui.SafetyViewModel
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentResponseScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val events by viewModel.allEvents.collectAsState()
    val isElderly = viewModel.isElderlyMode()

    var sentMoney by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("15000") }
    var timingOption by remember { mutableStateOf("WITHIN_2_HOURS") } // WITHIN_2_HOURS, TODAY, OLDER
    var channel by remember { mutableStateOf("UPI") } // UPI, NETBANKING, CARD, WALLET
    var utrNumber by remember { mutableStateOf("425619882103") }
    var recipientHandle by remember { mutableStateOf("frauddesk99@ybl") }
    var incidentNotes by remember { mutableStateOf("Caller impersonated electricity board officer threatening power cutoff.") }

    var savedIncidentId by remember { mutableStateOf<Long?>(null) }
    var exportedReportJson by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Emergency Incident Response", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
            // Golden Hour Callout Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RiskCriticalContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = RiskCriticalRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACT FAST • GOLDEN HOUR WINDOW",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = RiskCriticalRed)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reporting cyber financial fraud to 1930 within the first 2 hours enables Indian banks to instantly freeze money before the scammer withdraws it at an ATM.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = RiskCriticalRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Emergency Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.weight(1f).height(54.dp).testTag("call_1930_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalRed)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call 1930", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = {
                        val portalIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in"))
                        context.startActivity(portalIntent)
                    },
                    modifier = Modifier.weight(1f).height(54.dp).testTag("open_cybercrime_portal_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("cybercrime.gov.in", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    viewModel.sendTrustedContactAlert("Emergency report filed: ₹$amountText lost via $channel. Calling 1930.")
                    scope.launch {
                        snackbarHostState.showSnackbar("Alert dispatched to trusted family network.")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("emergency_alert_contact_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddAlert, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notify Family / Guardian of Incident")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Incident Details Form
            Text(
                text = "Record Incident Dossier",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Preserve exact timestamps and reference numbers for police complaint.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount Transferred (₹)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = utrNumber,
                onValueChange = { utrNumber = it },
                label = { Text("12-Digit UPI UTR / Bank Reference Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = recipientHandle,
                onValueChange = { recipientHandle = it },
                label = { Text("Fraudster Phone / UPI Handle") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = incidentNotes,
                onValueChange = { incidentNotes = it },
                label = { Text("How the fraud occurred (Brief notes)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Payment Channel:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                listOf("UPI", "NETBANKING", "CARD").forEach { ch ->
                    FilterChip(
                        selected = channel == ch,
                        onClick = { channel = ch },
                        label = { Text(ch) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save & Export Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            val id = viewModel.repository.saveIncident(
                                amount = amountText.toDoubleOrNull() ?: 0.0,
                                paymentMethod = channel,
                                transactionReference = utrNumber,
                                timestamp = System.currentTimeMillis(),
                                notes = incidentNotes,
                                evidence = recipientHandle
                            )
                            savedIncidentId = id

                            val mockIncident = IncidentEntity(
                                id = id,
                                amount = amountText.toDoubleOrNull() ?: 0.0,
                                paymentMethod = channel,
                                transactionReference = utrNumber,
                                timestamp = System.currentTimeMillis(),
                                notes = incidentNotes,
                                evidenceReferences = recipientHandle
                            )
                            val json = viewModel.repository.generateIncidentReportJson(mockIncident, events)
                            exportedReportJson = json
                            clipboard.setText(AnnotatedString(json))
                            snackbarHostState.showSnackbar("Incident saved locally & evidence dossier copied to clipboard!")
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp).testTag("save_and_export_incident_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Export Dossier")
                }
            }

            // Exported Preview if available
            exportedReportJson?.let { report ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sanitized Police Evidence Dossier", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString(report))
                                scope.launch { snackbarHostState.showSnackbar("Copied JSON dossier to clipboard") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = report,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp),
                            maxLines = 10
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step-by-Step Recovery Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Immediate Cybercrime Protocol Checklist:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("1. Call 1930 and state UTR / reference number clearly.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Contact your bank's 24/7 fraud helpline to block account / debit card.", style = MaterialTheme.typography.bodySmall)
                    Text("3. Change UPI MPIN and netbanking login credentials.", style = MaterialTheme.typography.bodySmall)
                    Text("4. File formal complaint on cybercrime.gov.in with the exported dossier.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
