package com.example.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import com.example.engine.MessageAnalyzer
import com.example.ui.SafetyViewModel
import com.example.util.DeviceSmsItem
import com.example.util.SmsReaderUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageAnalysisScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isElderly = viewModel.isElderlyMode()

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    var deviceSmsList by remember { mutableStateOf<List<DeviceSmsItem>>(emptyList()) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSmsPermission = granted
        if (granted) {
            deviceSmsList = SmsReaderUtil.readRecentInboxMessages(context)
            scope.launch {
                snackbarHostState.showSnackbar("Loaded ${deviceSmsList.size} messages from device inbox.")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("SMS permission denied. You can still paste messages manually.")
            }
        }
    }

    LaunchedEffect(hasSmsPermission) {
        if (hasSmsPermission) {
            deviceSmsList = SmsReaderUtil.readRecentInboxMessages(context)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("SMS & Message Fraud Check", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
            Text(
                text = "Inspect Any Message",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paste text received via SMS, WhatsApp, Telegram, or choose an SMS directly from your device inbox below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("e.g. Your electricity will be disconnected tonight at 9:30pm...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("message_input_box"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.startMessageAnalysis(messageText.trim())
                            onNavigateToAnalysis()
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("analyze_message_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Analyze Scam Signals", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = {
                        if (!hasSmsPermission) {
                            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                        } else {
                            deviceSmsList = SmsReaderUtil.readRecentInboxSms(context)
                            scope.launch {
                                snackbarHostState.showSnackbar("Refreshed ${deviceSmsList.size} SMS from device inbox.")
                            }
                        }
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Device SMS")
                }
            }

            // Device Inbox Section (Real SMS & RCS from Phone)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Device Message Inbox",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Real incoming SMS, MMS, & Rich Chat/RCS text alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasSmsPermission) {
                    IconButton(
                        onClick = {
                            deviceSmsList = SmsReaderUtil.readRecentInboxMessages(context)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Messages", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (!hasSmsPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Grant SMS access to inspect real incoming texts from your phone with a single tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { smsPermissionLauncher.launch(Manifest.permission.READ_SMS) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant SMS Permission")
                        }
                    }
                }
            } else if (deviceSmsList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Device inbox is empty. As new SMS or RCS messages arrive on your phone, they will appear here for instant fraud analysis.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    deviceSmsList.take(8).forEach { sms ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    messageText = sms.body
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Loaded ${sms.protocolType} message from ${sms.address}")
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sms.address,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = sms.protocolType,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                    Text(
                                        text = sms.dateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sms.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap to load into inspector →",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Common Scam Pattern Signatures (Reference Examples)
            Text(
                text = "Threat Signatures (Reference Test Scenarios)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetMessageItem(
                    title = "Digital Arrest Extortion",
                    text = "This is Inspector Sharma from Delhi Crime Branch. A parcel with narcotics and fake passports was seized in your name. You are under Digital Arrest. Transfer ₹1,50,000 security deposit immediately or police will raid your house in 1 hour.",
                    onClick = { messageText = it }
                )
                PresetMessageItem(
                    title = "Electricity Power Cutoff Panic",
                    text = "Dear Customer, Your electricity power will be disconnected tonight at 9:30 PM from the main office because your previous bill was not updated. Please immediately call Electricity Officer at 9876543210 to avoid blackout.",
                    onClick = { messageText = it }
                )
                PresetMessageItem(
                    title = "Telegram / YouTube Part-Time Job",
                    text = "Part time job offer: Earn ₹3,000 to ₹5,000 daily by simply liking YouTube videos and rating Google hotels! No experience required. Daily payout to your UPI. Join our Telegram channel to receive your first ₹500 task now.",
                    onClick = { messageText = it }
                )
                PresetMessageItem(
                    title = "Remote Desktop App Instruction",
                    text = "Dear Customer, To complete your bank KYC verification, please download QuickSupport from Play Store and share the 9-digit code with our agent to verify screen.",
                    onClick = { messageText = it }
                )
                PresetMessageItem(
                    title = "Genuine Bank Salary Credit",
                    text = "Your A/C ending in 4102 has been credited with INR 45,000.00 on 10-Sep-2026 by NEFT/Salary from TCS Ltd. Available Bal: INR 78,410.20 - HDFC Bank.",
                    onClick = { messageText = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Copyable Family Advisory Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Family Advisory Generator",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quickly copy a formatted warning message to send to parents or family on WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val advisory = MessageAnalyzer.generateFamilyAdvisory(
                                assessment = MessageAnalyzer.analyze(messageText.ifEmpty { "Suspicious alert message" }),
                                messagePreview = messageText.ifEmpty { "Warning message" }
                            )
                            clipboard.setText(AnnotatedString(advisory))
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied WhatsApp Family Warning Advisory to clipboard!")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Family WhatsApp Warning")
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetMessageItem(
    title: String,
    text: String,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Button(
                    onClick = { onClick(text) },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Load", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

