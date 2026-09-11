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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioAnalysisScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Audio Recording, 1: Transcript Mode
    var isRecording by remember { mutableStateOf(false) }
    var transcriptText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val isElderly = viewModel.isElderlyMode()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AI Voice Clone & Call Forensics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
            // Probabilistic Safety Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Voice analysis is probabilistic. Always verify caller identity over a known telephone number or video call before sending funds.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Audio Recording", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Call Transcript", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.TextSnippet, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                // Audio Recording Mode
                Text(
                    text = "Microphone-Initiated Audio Forensics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Capture a call excerpt or audio message. Recording runs strictly on-demand.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) RiskCriticalRed else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isRecording) "Recording Excerpt (Analyzing Acoustic Resonances...)" else "Ready to Record Call Excerpt",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (!hasMicPermission) {
                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                if (!isRecording) {
                                    isRecording = true
                                    scope.launch {
                                        val summary = com.example.util.RealAudioRecorderUtil.recordAndAnalyzeAcoustics(context, 4)
                                        isRecording = false
                                        viewModel.startAudioAnalysis(summary.recordedAudioToken, summary.sampleDurationSec)
                                        onNavigateToAnalysis()
                                    }
                                } else {
                                    isRecording = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("record_audio_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) RiskCriticalRed else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRecording) "Recording & Analyzing Live Mic..." else "Record Call Excerpt (Live Mic)")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Acoustic Threat Signatures (Reference Profiles):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.startAudioAnalysis("clone_son_accident_emergency_hospital_call.wav", 12)
                            onNavigateToAnalysis()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("sample_cloned_voice_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Profile A: Cloned Voice 'Accident Bail' Extortion Call")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.startAudioAnalysis("authentic_normal_call.wav", 10)
                            onNavigateToAnalysis()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Profile B: Authentic Human Natural Voice Call")
                    }
                }
            } else {
                // Transcript Mode
                Text(
                    text = "Conversation Scam Intent & Extortion Check",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type or paste what the caller said to assess emotional pressure and coercion.",
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
                            viewModel.startTranscriptAnalysis(transcriptText.trim())
                            onNavigateToAnalysis()
                        }
                    },
                    enabled = transcriptText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("analyze_transcript_button"),
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
