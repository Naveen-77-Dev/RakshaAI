package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RiskLevel
import com.example.localization.AppStrings
import com.example.ui.SafetyViewModel
import com.example.ui.components.getRiskColor
import com.example.ui.theme.RakshaNavy
import com.example.ui.theme.RakshaNavyLight
import com.example.ui.theme.RakshaTeal
import com.example.ui.theme.RakshaTealLight
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SafetyViewModel,
    onOpenScanHub: () -> Unit,
    onOpenLinkScan: () -> Unit,
    onOpenQrScan: () -> Unit,
    onOpenMessageCheck: () -> Unit,
    onOpenAudioCheck: () -> Unit,
    onOpenPaymentSafety: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenDemoCenter: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val contacts by viewModel.activeContacts.collectAsState()
    val isElderly = viewModel.isElderlyMode()
    val familyAlert by viewModel.lastSimulatedFamilyAlert.collectAsState()

    val tips = listOf(
        Pair("Never Enter PIN to Receive Money", AppStrings.get("upi_pin_warning", lang)),
        Pair("Beware of 'Digital Arrest' Calls", "Police and judges never conduct video-call arrests or demand financial settlements."),
        Pair("Voice Cloning is Real", "If a caller claims a family emergency, hang up and call them directly on their known real number."),
        Pair("Check Courier Links", "India Post and couriers never request ₹5 or ₹25 address fees over random links.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top App Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppStrings.get("app_title", lang),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isElderly) 26.sp else 22.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "INDIA SAFEGUARD",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = AppStrings.get("tagline", lang),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenDemoCenter,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("demo_center_button")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Threat Lab", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Family Alert Notification Simulation
        AnimatedVisibility(visible = familyAlert != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RiskCriticalContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, tint = RiskCriticalRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = familyAlert ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = RiskCriticalRed,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearFamilyAlert() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RiskCriticalRed)
                    }
                }
            }
        }

        // Active Protection Shield Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(RakshaNavy, RakshaNavyLight, RakshaTeal.copy(alpha = 0.8f)))
                    )
                    .padding(if (isElderly) 24.dp else 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(RakshaTealLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get("shield_status_protected", lang),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isElderly) 20.sp else 16.sp
                                ),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.get("shield_status_subtitle", lang),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isElderly) 14.sp else 12.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Zero Background Recording • Privacy First",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(if (isElderly) 64.dp else 56.dp)
                            .clip(CircleShape)
                            .background(RakshaTeal.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = RakshaTealLight,
                            modifier = Modifier.size(if (isElderly) 38.dp else 32.dp)
                        )
                    }
                }
            }
        }

        // Prominent "Scan Anything" Primary Action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onOpenScanHub,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isElderly) 66.dp else 56.dp)
                    .testTag("scan_anything_main_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(if (isElderly) 28.dp else 22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = AppStrings.get("scan_anything_button", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isElderly) 20.sp else 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Grid (5 Key Vectors)
        Text(
            text = "Instant Safety Scans",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderly) 20.sp else 16.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionTile(
                    title = AppStrings.get("quick_action_link", lang),
                    desc = "Phishing & KYC URLs",
                    icon = Icons.Default.Link,
                    color = Color(0xFF0284C7),
                    isElderly = isElderly,
                    modifier = Modifier.weight(1f).testTag("quick_action_link"),
                    onClick = onOpenLinkScan
                )
                QuickActionTile(
                    title = AppStrings.get("quick_action_qr", lang),
                    desc = "Inspect UPI & Links",
                    icon = Icons.Default.QrCodeScanner,
                    color = Color(0xFF059669),
                    isElderly = isElderly,
                    modifier = Modifier.weight(1f).testTag("quick_action_qr"),
                    onClick = onOpenQrScan
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionTile(
                    title = AppStrings.get("quick_action_msg", lang),
                    desc = "SMS & Screenshots",
                    icon = Icons.Default.Sms,
                    color = Color(0xFF7C3AED),
                    isElderly = isElderly,
                    modifier = Modifier.weight(1f).testTag("quick_action_msg"),
                    onClick = onOpenMessageCheck
                )
                QuickActionTile(
                    title = AppStrings.get("quick_action_audio", lang),
                    desc = "Voice Clones & Calls",
                    icon = Icons.Default.Mic,
                    color = Color(0xFFD97706),
                    isElderly = isElderly,
                    modifier = Modifier.weight(1f).testTag("quick_action_audio"),
                    onClick = onOpenAudioCheck
                )
            }

            // Pre-payment safety tile spanning full width
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPaymentSafety() }
                    .testTag("quick_action_payment_safety"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(if (isElderly) 18.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isElderly) 46.dp else 38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.get("quick_action_pay", lang),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderly) 18.sp else 15.sp
                            )
                        )
                        Text(
                            text = "Pause before paying • Cooldown & scam verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency "I May Have Been Scammed" Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onOpenEmergency() }
                .testTag("emergency_button"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = RiskCriticalContainer)
        ) {
            Row(
                modifier = Modifier.padding(if (isElderly) 20.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isElderly) 48.dp else 40.dp)
                        .clip(CircleShape)
                        .background(RiskCriticalRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.get("emergency_button", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isElderly) 18.sp else 15.sp
                        ),
                        color = RiskCriticalRed
                    )
                    Text(
                        text = AppStrings.get("emergency_subtitle", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = RiskCriticalRed)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Safety Tips Carousel
        Text(
            text = AppStrings.get("safety_tips_title", lang),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderly) 20.sp else 16.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tips) { (headline, detail) ->
                Card(
                    modifier = Modifier.width(260.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Risk Events
        Text(
            text = AppStrings.get("recent_scams_title", lang),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderly) 20.sp else 16.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (events.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "No risk events recorded yet. Use 'Scan Anything' to inspect links or calls.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                events.take(4).forEach { event ->
                    val level = RiskLevel.valueOf(event.riskLevel)
                    val color = getRiskColor(level)
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(event.createdAt))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (level == RiskLevel.SAFE) Icons.Default.Shield else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = event.scamCategory,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${event.riskScore}/100",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = color
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.contentPreview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trusted Contact Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppStrings.get("trusted_contact_card_title", lang),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val contact = contacts.firstOrNull()
                if (contact != null) {
                    Text(
                        text = "Active Guardian: ${contact.name} (${contact.relationship})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Will be notified if critical scam is flagged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "No trusted family member added. Setup in Family Mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    isElderly: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(if (isElderly) 18.dp else 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isElderly) 42.dp else 34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(if (isElderly) 24.dp else 18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isElderly) 16.sp else 14.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isElderly) 13.sp else 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
