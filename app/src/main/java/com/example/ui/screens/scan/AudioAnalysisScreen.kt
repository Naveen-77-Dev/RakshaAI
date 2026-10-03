package com.example.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.SafetyViewModel
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed
import com.example.util.LiveVoiceSpeechManager
import com.example.util.VoiceInputState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioAnalysisScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isElderly = viewModel.isElderlyMode()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Mic Listening, 1: Spoken Transcript / Text
    var isListening by remember { mutableStateOf(false) }
    var liveSpokenText by remember { mutableStateOf("") }
    var liveListeningStatus by remember { mutableStateOf("Press 'Start Listening' and speak into your phone's microphone.") }
    var liveRmsDb by remember { mutableStateOf(0f) }
    var recognitionJob by remember { mutableStateOf<Job?>(null) }

    var transcriptText by remember { mutableStateOf("") }

    val speechManager = remember { LiveVoiceSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stop()
            recognitionJob?.cancel()
        }
    }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    fun stopVoiceListening() {
        isListening = false
        speechManager.stop()
        recognitionJob?.cancel()
        recognitionJob = null
    }

    fun startVoiceListening() {
        if (!hasMicPermission) {
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        stopVoiceListening()
        isListening = true
        liveSpokenText = ""
        liveListeningStatus = "Listening to your voice... Speak clearly into the microphone."

        recognitionJob = scope.launch {
            speechManager.startListeningFlow().collect { state ->
                when (state) {
                    is VoiceInputState.Idle -> {
                        liveListeningStatus = "Idle"
                    }
                    is VoiceInputState.ReadyToSpeak -> {
                        liveListeningStatus = "Microphone is open. Speak now!"
                    }
                    is VoiceInputState.Listening -> {
                        liveListeningStatus = "Listening... detecting speech"
                        liveRmsDb = state.rmsDb
                    }
                    is VoiceInputState.PartialHypothesis -> {
                        liveSpokenText = state.text
                        liveListeningStatus = "Hearing you: ${state.text}"
                    }
                    is VoiceInputState.FinalResult -> {
                        isListening = false
                        liveSpokenText = state.spokenText
                        liveListeningStatus = "Captured: ${state.spokenText}"
                    }
                    is VoiceInputState.Error -> {
                        isListening = false
                        liveListeningStatus = "Status: ${state.message}"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Live Voice & Call Forensics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Real-Time Mic",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopVoiceListening()
                        onBack()
                    }) {
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
            // Live Voice Analyzer Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Microphone-Driven Voice Analysis",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "The app listens to what the caller or speaker is saying through your mobile microphone, transcribes live speech, and delivers a full extortion & scam diagnostic report.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Live Mic Listening", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Transcript / Scenarios", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.TextSnippet, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                // Interactive Microphone Listening Mode
                Text(
                    text = "Live Microphone Voice Scanner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Put your phone near the speaker or speak aloud. The app continuously listens and transcribes what is said.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone Active Visualizer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isListening) RiskCriticalContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isListening) RiskCriticalRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isListening) RiskCriticalRed else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isListening) "Microphone Active — Listening..." else "Ready to Listen",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isListening) RiskCriticalRed else MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = liveListeningStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        if (isListening) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RiskCriticalRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcribed Speech Output / Editor Box
                Text(
                    text = "Spoken Speech Transcribed From Mic:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = liveSpokenText,
                    onValueChange = { liveSpokenText = it },
                    placeholder = { Text("What the user speaks through the microphone will automatically appear here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("live_spoken_transcript_box"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isListening) {
                                stopVoiceListening()
                            } else {
                                startVoiceListening()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("record_audio_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) RiskCriticalRed else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(if (isListening) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isListening) "Stop Listening" else "Start Listening")
                    }

                    Button(
                        onClick = {
                            stopVoiceListening()
                            val textToAnalyze = liveSpokenText.trim().ifEmpty {
                                "Emergency call from police station demanding bail money"
                            }
                            viewModel.startLiveVoiceAnalysis(textToAnalyze)
                            onNavigateToAnalysis()
                        },
                        enabled = liveSpokenText.isNotBlank() || isListening,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("generate_report_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze & Report")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick test speech simulator chips in case speech recognizer isn't available in emulator
                Text(
                    text = "Quick Sample Voice Injections (Tap to test):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            liveSpokenText = "Dad, I am at the police station, I was arrested near MG road. Please don't tell Mom, send 25000 immediately to this UPI id for my bail."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simulate: 'Arrested & Police Station Bail' Emergency")
                    }

                    OutlinedButton(
                        onClick = {
                            liveSpokenText = "Hello sir, your electricity bill is unpaid and power will be cut in 1 hour. Call this number and share your OTP right now to pay."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simulate: 'Electricity Cutoff & OTP' Threat")
                    }

                    OutlinedButton(
                        onClick = {
                            liveSpokenText = "Hi Ramesh, I am calling to confirm our meeting tomorrow morning at 10 AM at the coffee shop. See you there."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simulate: 'Authentic Normal Call' (Safe)")
                    }
                }

            } else {
                // Transcript & Detailed Extortion Check Mode
                Text(
                    text = "Conversation Scam Intent & Extortion Check",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type or paste what the caller said to assess emotional pressure, digital arrest, and extortion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = transcriptText,
                    onValueChange = { transcriptText = it },
                    placeholder = { Text("e.g. Dad, I am at the police station. Don't tell Mom. Send ₹25,000 immediately...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("transcript_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (transcriptText.isNotBlank()) {
                            stopVoiceListening()
                            viewModel.startTranscriptAnalysis(transcriptText.trim())
                            onNavigateToAnalysis()
                        }
                    },
                    enabled = transcriptText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("analyze_transcript_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Analyze Conversation Intent", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Preset Voice Call Transcripts:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            transcriptText = "Dad, I met with a serious accident near MG Road. Police are detaining me. I need ₹25,000 right now for hospital bail. Don't call Mom, she will panic. Send to UPI: hospital.emergency@icici"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Son Cloned Kidnap/Accident Extortion")
                    }

                    OutlinedButton(
                        onClick = {
                            transcriptText = "Hello Sir, I am calling from HDFC Bank head office. Your debit card reward points are expiring today. Please share the 6-digit OTP to renew."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Bank Reward Points OTP Request")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Family Verification Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Real-World Verification Protocol",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("1. Hang up the call — never send money in the middle of a distress call.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Dial back the family member on their known real contact number.", style = MaterialTheme.typography.bodySmall)
                    Text("3. Ask a private question only they can answer (e.g. childhood nickname or family memory).", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
