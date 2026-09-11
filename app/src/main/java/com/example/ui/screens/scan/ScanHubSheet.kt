package com.example.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHubSheet(
    viewModel: SafetyViewModel,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismiss: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToQr: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToMessage: () -> Unit
) {
    var universalInput by remember { mutableStateOf("") }
    val isElderly = viewModel.isElderlyMode()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Scan Anything",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isElderly) 24.sp else 20.sp
                        )
                    )
                    Text(
                        text = "Paste any link, message, or select an input vector",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Universal Smart Paste Box with auto-detection
            OutlinedTextField(
                value = universalInput,
                onValueChange = { universalInput = it },
                placeholder = { Text("Paste link, SMS, WhatsApp text, or UPI handle...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("universal_input_field"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 4,
                leadingIcon = {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val trimmed = universalInput.trim()
                    if (trimmed.isNotEmpty()) {
                        onDismiss()
                        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains(".com") || trimmed.contains(".cc") || trimmed.contains(".xyz")) {
                            viewModel.startUrlAnalysis(trimmed)
                        } else if (trimmed.startsWith("upi://") || trimmed.contains("@ok") || trimmed.contains("@icici")) {
                            viewModel.startQrAnalysis(trimmed)
                        } else {
                            viewModel.startMessageAnalysis(trimmed)
                        }
                        onNavigateToAnalysis()
                    }
                },
                enabled = universalInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("analyze_pasted_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Smart Analyze Input",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Or choose dedicated scanner:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HubOptionRow(
                    icon = Icons.Default.QrCodeScanner,
                    title = "Scan UPI / Website QR Code",
                    desc = "Real-time camera scanner with debit PIN warning",
                    color = Color(0xFF059669),
                    onClick = {
                        onDismiss()
                        onNavigateToQr()
                    }
                )
                HubOptionRow(
                    icon = Icons.Default.Mic,
                    title = "Analyze Voice Clip or Call Transcript",
                    desc = "Check synthetic speech & emergency extortion intent",
                    color = Color(0xFFD97706),
                    onClick = {
                        onDismiss()
                        onNavigateToAudio()
                    }
                )
                HubOptionRow(
                    icon = Icons.Default.Image,
                    title = "Inspect Alert Message or Screenshot",
                    desc = "Check bank alerts, SMS texts, or suspicious chats",
                    color = Color(0xFF7C3AED),
                    onClick = {
                        onDismiss()
                        onNavigateToMessage()
                    }
                )
                HubOptionRow(
                    icon = Icons.Default.Payment,
                    title = "Pre-Payment Safety Cooldown",
                    desc = "Before You Pay safety questionnaire",
                    color = Color(0xFF1D4ED8),
                    onClick = {
                        onDismiss()
                        onNavigateToPayment()
                    }
                )
            }
        }
    }
}

@Composable
private fun HubOptionRow(
    icon: ImageVector,
    title: String,
    desc: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
