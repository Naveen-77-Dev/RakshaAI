package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RiskLevel
import com.example.engine.RiskSignal
import com.example.engine.ScamCategory
import com.example.engine.SignalSeverity
import com.example.ui.theme.RiskCautionAmber
import com.example.ui.theme.RiskCautionContainer
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskHighContainer
import com.example.ui.theme.RiskHighOrange
import com.example.ui.theme.RiskSafeContainer
import com.example.ui.theme.RiskSafeGreen

fun getRiskColor(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE -> RiskSafeGreen
    RiskLevel.CAUTION -> RiskCautionAmber
    RiskLevel.HIGH_RISK -> RiskHighOrange
    RiskLevel.CRITICAL -> RiskCriticalRed
    RiskLevel.UNKNOWN -> Color.Gray
}

fun getRiskContainerColor(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE -> RiskSafeContainer
    RiskLevel.CAUTION -> RiskCautionContainer
    RiskLevel.HIGH_RISK -> RiskHighContainer
    RiskLevel.CRITICAL -> RiskCriticalContainer
    RiskLevel.UNKNOWN -> Color(0xFFE2E8F0)
}

@Composable
fun RiskMeter(
    score: Int,
    level: RiskLevel,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    val progressAnimated by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "riskProgress"
    )
    val color = getRiskColor(level)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = getRiskContainerColor(level).copy(alpha = 0.45f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color))
    ) {
        Column(
            modifier = Modifier.padding(if (isElderlyMode) 20.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bold High-Contrast Safety Verdict Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (level.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = level.verdictTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            fontSize = if (isElderlyMode) 18.sp else 15.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isElderlyMode) 44.dp else 36.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (level.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isElderlyMode) 28.dp else 20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (level.isSafe) "SAFE TO PROCEED" else "UNSAFE / POTENTIAL FRAUD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderlyMode) 20.sp else 16.sp
                            ),
                            color = color
                        )
                        Text(
                            text = when (level) {
                                RiskLevel.SAFE -> "Clean signature: No malicious or deceptive patterns found."
                                RiskLevel.CAUTION -> "Caution required: Verify payee identity and amount before confirming."
                                RiskLevel.HIGH_RISK -> "NOT SAFE: Strong scam and deception indicators detected!"
                                RiskLevel.CRITICAL -> "DANGEROUS: Active scam pattern detected! Do not proceed."
                                RiskLevel.UNKNOWN -> "Analysis incomplete: Proceed with strict caution."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isElderlyMode) 14.sp else 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color))
                ) {
                    Text(
                        text = "$score / 100",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isElderlyMode) 18.sp else 14.sp
                        ),
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isElderlyMode) 14.dp else 10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = color,
                trackColor = Color.LightGray.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0 (Safe)", style = MaterialTheme.typography.labelSmall, color = RiskSafeGreen)
                Text("45 (High Risk)", style = MaterialTheme.typography.labelSmall, color = RiskHighOrange)
                Text("70+ (Critical)", style = MaterialTheme.typography.labelSmall, color = RiskCriticalRed)
            }
        }
    }
}

@Composable
fun ScamCategoryBadge(category: ScamCategory, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ConfidenceLabel(confidence: Float, modifier: Modifier = Modifier) {
    val percentage = (confidence * 100).toInt()
    Text(
        text = "$percentage% Model Confidence",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
fun ScamReasonCard(
    signal: RiskSignal,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    val sigColor = when (signal.severity) {
        SignalSeverity.HIGH -> RiskCriticalRed
        SignalSeverity.MEDIUM -> RiskCautionAmber
        SignalSeverity.LOW -> RiskSafeGreen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(if (isElderlyMode) 16.dp else 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(sigColor)
                    .padding(top = 6.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = signal.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isElderlyMode) 16.sp else 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = signal.explanation,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isElderlyMode) 15.sp else 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecommendedActionCard(
    action: String,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(if (isElderlyMode) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isElderlyMode) 22.dp else 18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = if (isElderlyMode) 16.sp else 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DisclaimerPanel(
    disclaimerText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = disclaimerText,
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
