package com.example.engine

enum class RiskLevel(
    val displayName: String,
    val verdictTitle: String,
    val isSafe: Boolean
) {
    SAFE("SAFE", "VERDICT: SAFE (Low Risk)", true),
    CAUTION("CAUTION", "VERDICT: PROCEED WITH CAUTION", false),
    HIGH_RISK("NOT SAFE", "VERDICT: NOT SAFE (High Fraud Risk)", false),
    CRITICAL("DANGEROUS SCAM", "VERDICT: NOT SAFE (Critical Threat)", false),
    UNKNOWN("INCONCLUSIVE", "VERDICT: INCONCLUSIVE", false);

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score < 25 -> SAFE
            score < 45 -> CAUTION
            score < 70 -> HIGH_RISK
            else -> CRITICAL
        }
    }
}

enum class ScamCategory(val displayName: String, val code: String) {
    NONE("No Scam Detected", "NONE"),
    PHISHING("Phishing Link", "PHISHING"),
    UPI_SCAM("UPI Payment Fraud", "UPI_SCAM"),
    IMPERSONATION("Authority / Bank Impersonation", "IMPERSONATION"),
    AI_VOICE_SCAM("AI Voice Clone Scam", "AI_VOICE_SCAM"),
    KYC_SCAM("Fake Bank / SIM KYC", "KYC_SCAM"),
    JOB_SCAM("Work-From-Home / Task Fraud", "JOB_SCAM"),
    LOAN_SCAM("Instant Loan Extortion", "LOAN_SCAM"),
    COURIER_SCAM("Fake Courier / Customs Fee", "COURIER_SCAM"),
    INVESTMENT_SCAM("Lottery / High Return Scam", "INVESTMENT_SCAM"),
    REMOTE_ACCESS_SCAM("Remote Screen-Sharing Scam", "REMOTE_ACCESS_SCAM"),
    OTHER("Suspicious Activity", "OTHER")
}

enum class SignalSeverity {
    LOW, MEDIUM, HIGH
}

data class RiskSignal(
    val name: String,
    val severity: SignalSeverity,
    val explanation: String
)

data class RiskAssessment(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val scamCategory: ScamCategory,
    val confidence: Float,
    val signals: List<RiskSignal>,
    val recommendedActions: List<String>,
    val requiresHumanVerification: Boolean = true,
    val disclaimer: String = "RakshaAI provides an automated risk assessment and cannot guarantee that content is safe or fraudulent. Verify important requests through an independent trusted channel.",
    val highlightedPhrases: List<String> = emptyList(),
    val voiceAuthenticityLevel: String? = null,
    val conversationIntentRisk: String? = null,
    val rawExtractedText: String? = null,
    val inputPayload: String = ""
)
