package com.example.ui.screens.onboarding

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppLanguage
import com.example.ui.SafetyViewModel
import com.example.ui.theme.RakshaNavy
import com.example.ui.theme.RakshaSapphire
import com.example.ui.theme.RakshaTeal
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: SafetyViewModel,
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 6
    val scope = rememberCoroutineScope()

    var selectedLang by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var selectedMode by remember { mutableStateOf("STANDARD") }
    var contactName by remember { mutableStateOf("Aarav") }
    var contactPhone by remember { mutableStateOf("+91 98765 43210") }
    var contactRelation by remember { mutableStateOf("Son / Daughter") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(onClick = { currentStep-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Step indicators
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == currentStep) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i == currentStep) MaterialTheme.colorScheme.primary
                                    else if (i < currentStep) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                if (currentStep < totalSteps) {
                    Text(
                        text = "Skip",
                        modifier = Modifier
                            .clickable { onFinish() }
                            .padding(8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                if (currentStep == 3) {
                                    viewModel.setLanguage(selectedLang)
                                } else if (currentStep == 4) {
                                    viewModel.setProtectionMode(selectedMode)
                                } else if (currentStep == 5 && contactName.isNotBlank()) {
                                    scope.launch {
                                        viewModel.repository.addTrustedContact(
                                            name = contactName,
                                            phone = contactPhone,
                                            relationship = contactRelation
                                        )
                                    }
                                }
                                currentStep++
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (currentStep == totalSteps) "Get Started with RakshaAI" else "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                1 -> Step1Welcome()
                2 -> Step2PrivacyAndPrinciples()
                3 -> Step3LanguageSelection(selected = selectedLang, onSelect = { selectedLang = it })
                4 -> Step4ProtectionLevel(selected = selectedMode, onSelect = { selectedMode = it })
                5 -> Step5TrustedContact(
                    name = contactName,
                    phone = contactPhone,
                    relation = contactRelation,
                    onNameChange = { contactName = it },
                    onPhoneChange = { contactPhone = it },
                    onRelationChange = { contactRelation = it }
                )
                6 -> Step6PermissionEducation()
            }
        }
    }
}

@Composable
private fun Step1Welcome() {
    Spacer(modifier = Modifier.height(24.dp))
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(RakshaNavy, RakshaSapphire, RakshaTeal))
            )
            .border(2.dp, RakshaTeal, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.raksha_logo),
            contentDescription = "RakshaAI Logo",
            modifier = Modifier.size(80.dp)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "RakshaAI",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = "Pause. Verify. Protect.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Stay safer from scam calls, suspicious links, and risky payment requests before money is lost.",
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(36.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureItem(
                icon = Icons.Default.Shield,
                title = "Phishing & Fake KYC Detection",
                desc = "Inspect deceptive URLs & QR codes before opening"
            )
            FeatureItem(
                icon = Icons.Default.Mic,
                title = "AI Voice Scam Forensic Check",
                desc = "Identify synthetic cloned voice extortion calls"
            )
            FeatureItem(
                icon = Icons.Default.Security,
                title = "Pre-Payment UPI Cooldown",
                desc = "Intervene before transfers to fraudulent recipients"
            )
        }
    }
}

@Composable
private fun Step2PrivacyAndPrinciples() {
    Spacer(modifier = Modifier.height(16.dp))
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(56.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Our Product Principles",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Transparent AI risk assessment with strict privacy guarantees",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PrincipleRow(
                title = "Probabilistic Risk, Not Absolute Guarantees",
                desc = "No AI tool can guarantee 100% scam detection. RakshaAI gives explainable evidence, confidence scores, and safe next actions."
            )
            PrincipleRow(
                title = "Zero Silent Audio or SMS Monitoring",
                desc = "RakshaAI never secretly records background calls or monitors personal chats. Analysis only runs when you choose to scan, record, or paste."
            )
            PrincipleRow(
                title = "Local Encrypted Data Preservation",
                desc = "Your evidence stays strictly on your device. Phone numbers and UPI handles are masked in reports."
            )
        }
    }
}

@Composable
private fun Step3LanguageSelection(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Choose Your Language",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "RakshaAI supports regional languages for clear, low-literacy guidance.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(lang) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                ) else CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = lang.nativeName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4ProtectionLevel(
    selected: String,
    onSelect: (String) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Select Protection Level",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Tailor interface readability and escalation sensitivity.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    val modes = listOf(
        Triple("STANDARD", "Standard Mode", "Balanced protection with detailed forensic explanations for everyday smartphone users."),
        Triple("ELDERLY", "Elderly-Friendly Mode", "Large readable fonts, high contrast visuals, simple non-technical summaries, and automatic audio readouts."),
        Triple("FAMILY", "Family Protection Mode", "Enables consent-based alerts to trusted children or guardians on high-risk scam events.")
    )

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        modes.forEach { (modeKey, title, desc) ->
            val isSelected = selected == modeKey
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(modeKey) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                ) else CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Step5TrustedContact(
    name: String,
    phone: String,
    relation: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationChange: (String) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Icon(
        imageVector = Icons.Default.People,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Setup Trusted Contact",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Optional family member who receives simulated alerts if a critical scam is detected.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Contact Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        label = { Text("Phone Number") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = relation,
        onValueChange = onRelationChange,
        label = { Text("Relationship (e.g. Son, Daughter, Friend)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun Step6PermissionEducation() {
    Spacer(modifier = Modifier.height(16.dp))
    Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(50.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Permission Education",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "We adhere to least-privilege principles. Only features you initiate require access.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureItem(
                icon = Icons.Default.CameraAlt,
                title = "Camera (QR Scanner)",
                desc = "Used solely to scan payment QR codes when you open the scanner."
            )
            FeatureItem(
                icon = Icons.Default.Mic,
                title = "Microphone (On-Demand Audio Check)",
                desc = "Active ONLY when you tap record to analyze an ongoing or suspicious voice call."
            )
            FeatureItem(
                icon = Icons.Default.PhotoLibrary,
                title = "Photos / Evidence",
                desc = "Accessed only when you explicitly pick a screenshot for OCR analysis."
            )
            FeatureItem(
                icon = Icons.Default.Notifications,
                title = "Security Notifications",
                desc = "Critical alerts if an urgent high-risk payment attempt is flagged."
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PrincipleRow(title: String, desc: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
