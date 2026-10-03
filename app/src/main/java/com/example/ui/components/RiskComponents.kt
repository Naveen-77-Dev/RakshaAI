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
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.engine.ScamCategory
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
fun getAdaptiveRiskContainerColor(level: RiskLevel): Color {
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.SurfaceDark
    return if (isDark) {
        when (level) {
            RiskLevel.SAFE -> Color(0xFF064E3B).copy(alpha = 0.55f)
            RiskLevel.CAUTION -> Color(0xFF451A03).copy(alpha = 0.55f)
            RiskLevel.HIGH_RISK -> Color(0xFF431407).copy(alpha = 0.55f)
            RiskLevel.CRITICAL -> Color(0xFF450A0A).copy(alpha = 0.55f)
            RiskLevel.UNKNOWN -> Color(0xFF1E293B).copy(alpha = 0.55f)
        }
    } else {
        when (level) {
            RiskLevel.SAFE -> RiskSafeContainer.copy(alpha = 0.5f)
            RiskLevel.CAUTION -> RiskCautionContainer.copy(alpha = 0.5f)
            RiskLevel.HIGH_RISK -> RiskHighContainer.copy(alpha = 0.5f)
            RiskLevel.CRITICAL -> RiskCriticalContainer.copy(alpha = 0.5f)
            RiskLevel.UNKNOWN -> Color(0xFFE2E8F0)
        }
    }
}

@Composable
fun RiskMeter(
    score: Int,
    level: RiskLevel,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    val clampedRisk = score.coerceIn(0, 100)
    val safePercentage = 100 - clampedRisk

    val progressAnimated by animateFloatAsState(
        targetValue = clampedRisk / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "riskProgress"
    )

    val riskColor = getRiskColor(level)
    val safeColor = RiskSafeGreen

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = getAdaptiveRiskContainerColor(level)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(riskColor))
    ) {
        Column(
            modifier = Modifier.padding(if (isElderlyMode) 20.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bold High-Contrast Safety Verdict Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = riskColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (level.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = level.verdictTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            fontSize = if (isElderlyMode) 19.sp else 16.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verdict Headline & Risk Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isElderlyMode) 44.dp else 36.dp)
                            .clip(CircleShape)
                            .background(riskColor),
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
                                fontSize = if (isElderlyMode) 19.sp else 16.sp
                            ),
                            color = riskColor
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

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage and Score Tag
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = riskColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(riskColor))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$clampedRisk%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isElderlyMode) 22.sp else 18.sp
                            ),
                            color = riskColor
                        )
                        Text(
                            text = "RISK SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = riskColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-part Slider Bar: Risk Portion + Remaining Safe Portion
            // Fills proportionally with the risk percentage, remaining portion displays remaining safety
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                // Two-part segmented track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isElderlyMode) 20.dp else 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Part 1: Risk filled portion
                        if (progressAnimated > 0.005f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = progressAnimated)
                                    .clip(
                                        if (progressAnimated >= 0.99f) RoundedCornerShape(8.dp)
                                        else RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                    )
                                    .background(riskColor)
                            )
                        }

                        // Part 2: Remaining Safe portion
                        if (progressAnimated < 0.995f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clip(
                                        if (progressAnimated <= 0.01f) RoundedCornerShape(8.dp)
                                        else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                    )
                                    .background(safeColor.copy(alpha = 0.45f))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Two parts breakdown: Risk % vs Remaining Safe %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Risk: $clampedRisk% (Score $clampedRisk/100)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderlyMode) 14.sp else 12.sp
                            ),
                            color = riskColor
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(safeColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remaining Safe: $safePercentage%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderlyMode) 14.sp else 12.sp
                            ),
                            color = safeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scale Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0% (Safe)", style = MaterialTheme.typography.labelSmall, color = RiskSafeGreen)
                Text("45% (High Risk)", style = MaterialTheme.typography.labelSmall, color = RiskHighOrange)
                Text("70%+ (Critical)", style = MaterialTheme.typography.labelSmall, color = RiskCriticalRed)
            }
        }
    }
}

@Composable
fun ScamCategoryBadge(category: ScamCategory, modifier: Modifier = Modifier) {
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                category.displayName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        icon = {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
    )
}

@Composable
fun ConfidenceLabel(confidence: Float, modifier: Modifier = Modifier) {
    val pct = (confidence * 100).toInt()
    Text(
        text = "Model Confidence: $pct%",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ScamReasonCard(
    signal: com.example.engine.RiskSignal,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    val tint = when (signal.severity) {
        com.example.engine.SignalSeverity.HIGH -> RiskCriticalRed
        com.example.engine.SignalSeverity.MEDIUM -> RiskCautionAmber
        com.example.engine.SignalSeverity.LOW -> RiskSafeGreen
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(if (isElderlyMode) 18.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = signal.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isElderlyMode) 16.sp else 13.5.sp
                    ),
                    color = tint
                )
                Text(
                    text = signal.explanation,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isElderlyMode) 15.sp else 12.5.sp,
                        lineHeight = if (isElderlyMode) 20.sp else 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ScamReasonCard(
    point: String,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(if (isElderlyMode) 18.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = RiskCriticalRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = point,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (isElderlyMode) 17.sp else 13.5.sp,
                    lineHeight = if (isElderlyMode) 22.sp else 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(if (isElderlyMode) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isElderlyMode) 16.sp else 13.5.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun DisclaimerPanel(
    disclaimerText: String,
    modifier: Modifier = Modifier
) {
    val text = disclaimerText
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
