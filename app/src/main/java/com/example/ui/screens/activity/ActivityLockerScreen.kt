package com.example.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AnalysisEventEntity
import com.example.engine.RiskLevel
import com.example.ui.SafetyViewModel
import com.example.ui.components.getRiskColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLockerScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit
) {
    val events by viewModel.allEvents.collectAsState()
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, HIGH_RISK, URL, MESSAGE, AUDIO, PAYMENT
    var selectedEventForDetail by remember { mutableStateOf<AnalysisEventEntity?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val filteredEvents = remember(events, selectedFilter) {
        when (selectedFilter) {
            "HIGH_RISK" -> events.filter { it.riskScore >= 45 }
            "URL" -> events.filter { it.type == "URL" || it.type == "QR" }
            "MESSAGE" -> events.filter { it.type == "MESSAGE" }
            "AUDIO" -> events.filter { it.type == "AUDIO" }
            "PAYMENT" -> events.filter { it.type == "PAYMENT" }
            else -> events
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Evidence & Activity Locker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearAllConfirm = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
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
        ) {
            // Filter Chips Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    Pair("ALL", "All Activity (${events.size})"),
                    Pair("HIGH_RISK", "High Risk Only"),
                    Pair("URL", "Links & QR"),
                    Pair("MESSAGE", "Messages & SMS"),
                    Pair("AUDIO", "Audio Scans"),
                    Pair("PAYMENT", "Payments")
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label) }
                    )
                }
            }

            // Export sanitized evidence button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredEvents.size} Records (PII Masked)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        val exportText = buildString {
                            appendLine("=== RAKSHAAI EVIDENCE AUDIT TRAIL ===")
                            appendLine("Preserved on device with client-side PII masking")
                            appendLine("Exported: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                            appendLine()
                            filteredEvents.forEachIndexed { i, ev ->
                                appendLine("[Event ${i + 1}] Type: ${ev.type} | Risk: ${ev.riskScore}/100 (${ev.riskLevel})")
                                appendLine("Category: ${ev.scamCategory}")
                                appendLine("Preview: ${ev.contentPreview}")
                                appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ev.createdAt))}")
                                appendLine("----------------------------------------")
                            }
                        }
                        clipboard.setText(AnnotatedString(exportText))
                        scope.launch { snackbarHostState.showSnackbar("Exported all records to clipboard!") }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Report", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Event List
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No records matching selected filter", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        val level = RiskLevel.valueOf(event.riskLevel)
                        val color = getRiskColor(level)
                        val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(event.createdAt))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEventForDetail = event },
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (event.type) {
                                            "URL" -> Icons.Default.Link
                                            "QR" -> Icons.Default.QrCode
                                            "MESSAGE" -> Icons.Default.Sms
                                            "AUDIO" -> Icons.Default.Mic
                                            else -> Icons.Default.Payment
                                        },
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(event.scamCategory, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            "${event.riskScore}/100",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = color
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        event.contentPreview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        viewModel.repository.deleteEvent(event.id)
                                        snackbarHostState.showSnackbar("Event removed from evidence locker")
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Event Detail Dialog
        selectedEventForDetail?.let { ev ->
            AlertDialog(
                onDismissRequest = { selectedEventForDetail = null },
                title = { Text(ev.scamCategory) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Type: ${ev.type}", style = MaterialTheme.typography.bodySmall)
                        Text("Risk Score: ${ev.riskScore}/100 (${ev.riskLevel})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Confidence: ${(ev.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        Text("Preview:\n${ev.contentPreview}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Signals:\n${ev.signalsJson}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedEventForDetail = null }) {
                        Text("Close")
                    }
                }
            )
        }

        // Clear All Confirm Dialog
        if (showClearAllConfirm) {
            AlertDialog(
                onDismissRequest = { showClearAllConfirm = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Clear All Local Data?") },
                text = { Text("This will permanently delete all scan records, evidence dossiers, and reported incidents from this device.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showClearAllConfirm = false
                            scope.launch {
                                viewModel.repository.clearAllData()
                                snackbarHostState.showSnackbar("All local data wiped cleanly.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearAllConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
