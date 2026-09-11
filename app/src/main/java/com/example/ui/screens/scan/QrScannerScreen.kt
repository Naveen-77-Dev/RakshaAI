package com.example.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.SafetyViewModel
import com.example.ui.theme.RakshaNavy
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var manualQrText by remember { mutableStateOf("") }
    val isElderly = viewModel.isElderlyMode()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("QR Code Scam Inspector", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Prominent UPI PIN Warning Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RiskCriticalContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RiskCriticalRed, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CRITICAL UPI SAFETY MANDATE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = RiskCriticalRed)
                        )
                        Text(
                            text = AppStrings.get("upi_pin_warning", AppLanguage.ENGLISH),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = RiskCriticalRed)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Viewfinder Box / Permission Handler
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RakshaNavy)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    RealCameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onQrDetected = { qrPayload ->
                            viewModel.startQrAnalysis(qrPayload)
                            onNavigateToAnalysis()
                        }
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Camera Access Required to Scan",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Point camera at any physical QR code or payment counter stand to scan and inspect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Scan Input or Manual Entry
            Text(
                text = "Manual QR or UPI Link Inspection",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paste any UPI payment link, QR barcode string, or merchant handle to inspect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = manualQrText,
                onValueChange = { manualQrText = it },
                placeholder = { Text("upi://pay?pa=merchant@bank&pn=Store&am=500...") },
                modifier = Modifier.fillMaxWidth().testTag("qr_input_field"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (manualQrText.isNotBlank()) {
                        viewModel.startQrAnalysis(manualQrText.trim())
                        onNavigateToAnalysis()
                    }
                },
                enabled = manualQrText.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("analyze_qr_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Inspect QR Scam Indicators", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inspection Examples (for testing threat models)
            Text(
                text = "Known Scam Pattern Signatures (Evaluation Reference)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val payload = "upi://pay?pa=cashback.desk.refund@icici&pn=Electricity_Refund_Office&am=2500&cu=INR&tn=Refund_Reversal_PIN_Required"
                        viewModel.startQrAnalysis(payload)
                        onNavigateToAnalysis()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("demo_scam_qr_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Evaluate Signature: Reverse UPI Debit Scam ('Refund ₹2,500')")
                }

                OutlinedButton(
                    onClick = {
                        val payload = "https://sbi-yono-update-pan.cc/verify"
                        viewModel.startQrAnalysis(payload)
                        onNavigateToAnalysis()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Evaluate Signature: Phishing Website QR (Fake Banking Portal)")
                }

                OutlinedButton(
                    onClick = {
                        val payload = "upi://pay?pa=ramesh.groceries@okhdfcbank&pn=Ramesh_Kirana_Store&am=150&cu=INR&tn=Groceries"
                        viewModel.startQrAnalysis(payload)
                        onNavigateToAnalysis()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Evaluate Signature: Standard Merchant QR (Genuine ₹150)")
                }
            }
        }
    }
}
