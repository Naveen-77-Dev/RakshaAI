package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneOrEmail: String,
    val language: String = "en",
    val protectionMode: String = "STANDARD", // STANDARD, ELDERLY, FAMILY
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trusted_contacts")
data class TrustedContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val name: String,
    val phone: String,
    val relationship: String,
    val alertPreferences: String = "CRITICAL_ONLY", // ALL, CRITICAL_ONLY, PAYMENT_ONLY
    val consentStatus: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "analysis_events")
data class AnalysisEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val type: String, // URL, QR, MESSAGE, AUDIO, PAYMENT, SCREENSHOT
    val contentPreview: String,
    val riskScore: Int,
    val riskLevel: String, // SAFE, CAUTION, HIGH_RISK, CRITICAL, UNKNOWN
    val scamCategory: String,
    val confidence: Float,
    val signalsJson: String,
    val recommendedActionsJson: String,
    val userActionTaken: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val status: String = "REPORTED", // REPORTED, INVESTIGATING, ESCALATED_1930
    val amount: Double,
    val paymentMethod: String,
    val transactionReference: String,
    val timestamp: Long,
    val notes: String,
    val evidenceReferences: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scam_patterns")
data class ScamPatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val phrasePatterns: String,
    val domainPatterns: String,
    val enabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
