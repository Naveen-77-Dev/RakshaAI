package com.example.engine

import java.util.Locale

object MessageAnalyzer {

    data class PatternRule(
        val category: ScamCategory,
        val keywords: List<String>,
        val scoreWeight: Int,
        val signalName: String,
        val explanation: String,
        val severity: SignalSeverity = SignalSeverity.HIGH
    )

    private val RULES = listOf(
        // Remote access software
        PatternRule(
            category = ScamCategory.REMOTE_ACCESS_SCAM,
            keywords = listOf("anydesk", "teamviewer", "quicksupport", "rustdesk", "screen share", "install apk", "download app from link"),
            scoreWeight = 45,
            signalName = "Remote Screen Access Tool Demand",
            explanation = "Scammers instruct victims to install remote desktop tools (AnyDesk/TeamViewer) to gain complete access to banking apps and OTPs."
        ),
        // OTP and PIN harvesting
        PatternRule(
            category = ScamCategory.UPI_SCAM,
            keywords = listOf("otp", "one time password", "cvv", "atm pin", "upi pin", "mpin", "netbanking password"),
            scoreWeight = 45,
            signalName = "Sensitive Credential / OTP Request",
            explanation = "Legitimate banks, government officers, and payment apps never ask for your OTP, PIN, or CVV under any circumstance."
        ),
        // Digital arrest / Police / CBI impersonation
        PatternRule(
            category = ScamCategory.IMPERSONATION,
            keywords = listOf("digital arrest", "crime branch", "cbi officer", "cyber cell", "customs narcotics", "parcel intercepted", "arrest warrant", "delhi police", "mumbai police", "money laundering case"),
            scoreWeight = 50,
            signalName = "Digital Arrest & Police Coercion",
            explanation = "Indian law enforcement and court authorities NEVER conduct 'digital arrest' or demand funds via video/audio calls."
        ),
        // Bank / SIM KYC expiration
        PatternRule(
            category = ScamCategory.KYC_SCAM,
            keywords = listOf("kyc suspended", "yono blocked", "pan update", "sim deactivated", "aadhaar link", "account suspended", "debit card blocked", "bank kyc"),
            scoreWeight = 40,
            signalName = "Urgent KYC Expiry Threat",
            explanation = "False claim that your bank account or SIM card will be deactivated unless you immediately click or update details."
        ),
        // Courier parcel withheld
        PatternRule(
            category = ScamCategory.COURIER_SCAM,
            keywords = listOf("indiapost", "parcel delivery failed", "address incorrect", "pay re-delivery fee", "customs duty", "package waiting", "delivery scheduled"),
            scoreWeight = 35,
            signalName = "Fake Courier / Parcel Address Scam",
            explanation = "Impersonates postal or courier logistics demanding small payments (e.g. ₹5 or ₹25) via a fraudulent link that drains bank accounts."
        ),
        // Part-time task / Job scams
        PatternRule(
            category = ScamCategory.JOB_SCAM,
            keywords = listOf("part time job", "work from home", "daily earn", "like youtube videos", "liking youtube", "telegram task", "rate hotels", "rating google", "earn 3000 to 5000", "daily payout", "first ₹500 task"),
            scoreWeight = 65,
            signalName = "Telegram / Task Job Fraud",
            explanation = "Promises easy money for reviewing videos/hotels, then coerces victims to deposit money into prepaid investment wallets."
        ),
        // Electricity cutoff threat
        PatternRule(
            category = ScamCategory.OTHER,
            keywords = listOf("electricity power will be disconnected", "electricity bill unpaid", "contact electricity officer", "power cutoff at 9:30 pm"),
            scoreWeight = 45,
            signalName = "Electricity Power Disconnection Threat",
            explanation = "Panic tactic threatening instant blackout unless you call a mobile number or click an unofficial payment link."
        ),
        // Lottery / Kaun Banega Crorepati
        PatternRule(
            category = ScamCategory.INVESTMENT_SCAM,
            keywords = listOf("kbc lottery", "won 25 lakh", "lucky draw", "claim prize money", "deposit tax to receive prize"),
            scoreWeight = 45,
            signalName = "Lottery / Prize Advance Fee Fraud",
            explanation = "Promises massive cash prizes and asks the victim to pay processing fees or taxes upfront."
        ),
        // Extreme urgency & fear
        PatternRule(
            category = ScamCategory.OTHER,
            keywords = listOf("immediately", "within 24 hours", "urgent notice", "final reminder", "action required immediately", "don't disconnect call"),
            scoreWeight = 25,
            signalName = "Psychological Urgency & Fear Induction",
            explanation = "Manufactures artificial panic to prevent victims from pausing to verify or consulting family members.",
            severity = SignalSeverity.MEDIUM
        )
    )

    fun analyze(text: String): RiskAssessment {
        val trimmed = text.trim()
        val lower = trimmed.lowercase(Locale.ROOT)
        val signals = mutableListOf<RiskSignal>()
        val actions = mutableListOf<String>()
        val highlighted = mutableListOf<String>()
        var score = 0
        var topCategory = ScamCategory.NONE
        var highestWeight = 0

        for (rule in RULES) {
            val matchedKeywords = rule.keywords.filter { lower.contains(it) }
            if (matchedKeywords.isNotEmpty()) {
                score += rule.scoreWeight
                highlighted.addAll(matchedKeywords)
                signals.add(
                    RiskSignal(
                        name = rule.signalName,
                        severity = rule.severity,
                        explanation = rule.explanation
                    )
                )
                if (rule.scoreWeight > highestWeight) {
                    highestWeight = rule.scoreWeight
                    topCategory = rule.category
                }
            }
        }

        // Check if message contains suspicious links or phone numbers
        if (lower.contains("http://") || lower.contains("https://") || lower.contains(".cc/") || lower.contains(".apk")) {
            score += 25
            signals.add(
                RiskSignal(
                    name = "Embedded Link or APK Download",
                    severity = SignalSeverity.HIGH,
                    explanation = "Unsolicited text contains external web destinations or Android application packages."
                )
            )
        }

        // Check for genuine informational bank credit alert
        if (lower.contains("credited with inr") || lower.contains("account credited with rs") || lower.contains("salary credited")) {
            if (signals.isEmpty() || score < 25) {
                score = 5
                topCategory = ScamCategory.NONE
                signals.clear()
                signals.add(
                    RiskSignal(
                        name = "Informational Financial Credit Alert",
                        severity = SignalSeverity.LOW,
                        explanation = "Standard bank notification indicating funds received. Does not request any action, link click, or PIN."
                    )
                )
            }
        }

        score = score.coerceIn(0, 100)
        val level = RiskLevel.fromScore(score)

        if (level == RiskLevel.CRITICAL || level == RiskLevel.HIGH_RISK) {
            actions.add("Never share OTPs, PINs, or screen access with anyone claiming to be authority/bank")
            actions.add("Do not click any embedded links or call numbers provided inside the message")
            actions.add("Block the sender and report to 1930 / cybercrime.gov.in")
            actions.add("Warn family members using the copyable advisory below")
        } else if (level == RiskLevel.CAUTION) {
            actions.add("Check official bank app or official helpline number directly")
            actions.add("Do not forward this message without verification")
        } else {
            actions.add("No immediate scam indicators found; maintain standard digital hygiene")
        }

        return RiskAssessment(
            riskScore = score,
            riskLevel = level,
            scamCategory = if (level == RiskLevel.SAFE) ScamCategory.NONE else topCategory,
            confidence = if (score > 50 || score <= 10) 0.94f else 0.82f,
            signals = signals,
            recommendedActions = actions,
            highlightedPhrases = highlighted.distinct(),
            rawExtractedText = trimmed,
            inputPayload = trimmed
        )
    }

    fun generateFamilyAdvisory(assessment: RiskAssessment, messagePreview: String): String {
        return """
🚨 *RakshaAI Family Safety Alert* 🚨
Caution: A suspicious message was identified with *${assessment.riskLevel.displayName}* (${assessment.riskScore}/100).
Category: ${assessment.scamCategory.displayName}

Snippet:
"${messagePreview.take(120)}..."

⚠️ *Important Safety Rule:*
1. Never enter your UPI PIN or share OTPs to receive funds.
2. Police, CBI, and banks NEVER do digital arrests or demand immediate money transfers.
3. Pause, verify independently, and consult family before taking action.
        """.trimIndent()
    }
}
