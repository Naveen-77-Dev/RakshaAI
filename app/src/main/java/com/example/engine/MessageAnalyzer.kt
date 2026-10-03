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
        // Fake Credit / Advance Payment / Claim reward lure (CRITICAL)
        PatternRule(
            category = ScamCategory.UPI_SCAM,
            keywords = listOf(
                "received today as advance", "advance payment", "click to confirm and claim",
                "click to claim", "confirm and claim", "click to receive", "click to accept payment",
                "received as advance", "advance received", "claim payment", "claim your cashback",
                "claim reward", "confirm credit", "click here to claim", "received today as",
                "sent you advance", "advance token", "approve credit", "claim ₹", "claim rs"
            ),
            scoreWeight = 60,
            signalName = "Fake Advance Credit & Claim-Link Phishing Trap",
            explanation = "Scammers send fabricated credit alerts instructing you to 'click to confirm' or 'claim' incoming money. In reality, clicking initiates a phishing debit or forces you to authorize a UPI PIN withdrawal."
        ),

        // Remote access software
        PatternRule(
            category = ScamCategory.REMOTE_ACCESS_SCAM,
            keywords = listOf("anydesk", "teamviewer", "quicksupport", "rustdesk", "screen share", "install apk", "download app from link", "install support app", "screenshare"),
            scoreWeight = 50,
            signalName = "Remote Screen Access Tool Demand",
            explanation = "Scammers instruct victims to install remote desktop tools (AnyDesk/TeamViewer/RustDesk) to gain complete access to banking apps and OTPs."
        ),

        // OTP and PIN harvesting
        PatternRule(
            category = ScamCategory.UPI_SCAM,
            keywords = listOf("otp", "one time password", "cvv", "atm pin", "upi pin", "mpin", "netbanking password", "enter pin to receive", "share otp"),
            scoreWeight = 50,
            signalName = "Sensitive Credential / OTP Request",
            explanation = "Legitimate banks, merchants, and payment apps NEVER ask for your OTP, UPI PIN, or CVV to receive payments or verify transactions."
        ),

        // Digital arrest / Police / CBI impersonation
        PatternRule(
            category = ScamCategory.IMPERSONATION,
            keywords = listOf("digital arrest", "crime branch", "cbi officer", "cyber cell", "customs narcotics", "parcel intercepted", "arrest warrant", "delhi police", "mumbai police", "money laundering case", "ed summons", "court notice"),
            scoreWeight = 55,
            signalName = "Digital Arrest & Police Coercion",
            explanation = "Indian law enforcement and court authorities NEVER conduct 'digital arrest' or demand funds via video/audio calls or messaging apps."
        ),

        // Bank / SIM KYC expiration
        PatternRule(
            category = ScamCategory.KYC_SCAM,
            keywords = listOf("kyc suspended", "yono blocked", "pan update", "sim deactivated", "aadhaar link", "account suspended", "debit card blocked", "bank kyc", "kyc expired", "update pan immediately", "sim blocked"),
            scoreWeight = 45,
            signalName = "Urgent KYC Expiry Threat",
            explanation = "False claim that your bank account or SIM card will be deactivated unless you immediately click an unofficial link or update sensitive credentials."
        ),

        // Courier parcel withheld
        PatternRule(
            category = ScamCategory.COURIER_SCAM,
            keywords = listOf("indiapost", "parcel delivery failed", "address incorrect", "pay re-delivery fee", "customs duty", "package waiting", "delivery scheduled", "wrong address update"),
            scoreWeight = 40,
            signalName = "Fake Courier / Parcel Address Scam",
            explanation = "Impersonates postal or courier logistics demanding small payments (e.g. ₹5 or ₹25) via a fraudulent link that secretly drains bank accounts."
        ),

        // Part-time task / Job scams
        PatternRule(
            category = ScamCategory.JOB_SCAM,
            keywords = listOf(
                "part time job", "part time", "part-time", "work from home", "daily earn", "like youtube videos",
                "liking youtube", "telegram task", "rate hotels", "rating google", "earn 3000 to 5000",
                "daily payout", "first ₹500 task", "prepaid task", "review work", "video rating",
                "rating work", "earn ₹", "earn rs", "deposit initial", "registration fee", "shortlisted for"
            ),
            scoreWeight = 50,
            signalName = "Part-Time Task & Ponzi Job Lure",
            explanation = "Promises easy money for reviewing Google maps, liking YouTube videos, or following Telegram channels, later coercing users into high-risk investment deposits."
        ),

        // Lottery / Kaun Banega Crorepati
        PatternRule(
            category = ScamCategory.INVESTMENT_SCAM,
            keywords = listOf("kbc lottery", "won 25 lakh", "lucky draw", "claim prize money", "deposit tax to receive prize", "congratulations you won", "selected for lottery", "bumper prize"),
            scoreWeight = 50,
            signalName = "Lottery / Prize Advance Fee Fraud",
            explanation = "Promises massive cash prizes and asks the victim to pay processing fees or taxes upfront before releasing fictitious funds."
        ),

        // Loan pre-approval & quick disbursement scam
        PatternRule(
            category = ScamCategory.INVESTMENT_SCAM,
            keywords = listOf("pre-approved loan", "loan of rs 5,00,000", "no cibil required", "instant loan without docs", "pay processing fee for loan"),
            scoreWeight = 40,
            signalName = "Fraudulent Instant Loan Lure",
            explanation = "Deceptive unverified instant loans designed to extort processing fees or harvest private contacts and gallery permissions."
        ),

        // Extreme urgency & fear
        PatternRule(
            category = ScamCategory.OTHER,
            keywords = listOf("immediately", "within 24 hours", "urgent notice", "final reminder", "action required immediately", "don't disconnect call", "imp alert", "important alert", "final warning"),
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

        // Check for URL shorteners and high-risk domains commonly used in SMS scams
        val hasShortener = lower.contains("bit.ly/") || lower.contains("tinyurl.com/") ||
                lower.contains("t.co/") || lower.contains("is.gd/") || lower.contains("cutt.ly/") ||
                lower.contains("rb.gy/") || lower.contains("wa.me/") || lower.contains("t.me/") ||
                lower.contains(".cc/") || lower.contains(".xyz") || lower.contains(".top") ||
                lower.contains(".club") || lower.contains(".apk")

        if (hasShortener) {
            score += 40
            highlighted.add(if (lower.contains("bit.ly")) "bit.ly" else "shortened link")
            signals.add(
                RiskSignal(
                    name = "Masked / Shortened Phishing Link",
                    severity = SignalSeverity.HIGH,
                    explanation = "Scammers use URL shorteners (like bit.ly, tinyurl, or t.me) to obscure the fraudulent destination domain and bypass SMS spam filters."
                )
            )
            if (topCategory == ScamCategory.NONE) {
                topCategory = ScamCategory.PHISHING
            }
        } else if (lower.contains("http://") || lower.contains("https://") || lower.contains("www.")) {
            score += 25
            signals.add(
                RiskSignal(
                    name = "Embedded External Hyperlink",
                    severity = SignalSeverity.HIGH,
                    explanation = "Unsolicited text contains external web destinations asking the recipient to navigate away from trusted apps."
                )
            )
            if (topCategory == ScamCategory.NONE) {
                topCategory = ScamCategory.PHISHING
            }
        }

        // Account number masking pattern (e.g. A/C 9110303575 or A/C XXXX) combined with urgency or payment claim
        val hasAccountRef = lower.contains("a/c") || lower.contains("ac no") || lower.contains("account no")
        val hasClaimAction = lower.contains("click") || lower.contains("claim") || lower.contains("confirm") || lower.contains("verify")
        val mentionsMoney = lower.contains("rs.") || lower.contains("rs ") || lower.contains("inr") || lower.contains("₹")

        if (hasAccountRef && hasClaimAction && mentionsMoney) {
            score += 35
            signals.add(
                RiskSignal(
                    name = "Fake Financial Credit / Claim Hook",
                    severity = SignalSeverity.HIGH,
                    explanation = "Legitimate bank transfers NEVER require you to click a link to claim or confirm incoming funds. Real deposits credit directly into your balance."
                )
            )
            if (topCategory == ScamCategory.NONE) {
                topCategory = ScamCategory.UPI_SCAM
            }
        }

        // Check for genuine informational bank credit alert (ONLY if there are NO links and NO call-to-action to click/claim)
        val isPureInformationalCredit = (lower.contains("credited with inr") || lower.contains("account credited with rs") || lower.contains("salary credited")) &&
                !hasClaimAction && !lower.contains("http") && !hasShortener

        if (isPureInformationalCredit) {
            score = 5
            topCategory = ScamCategory.NONE
            signals.clear()
            signals.add(
                RiskSignal(
                    name = "Informational Financial Credit Alert",
                    severity = SignalSeverity.LOW,
                    explanation = "Standard bank notification indicating funds received. Does not contain suspicious links, shorteners, or requests to click to claim."
                )
            )
        }

        score = score.coerceIn(0, 100)
        val level = RiskLevel.fromScore(score)

        if (level == RiskLevel.CRITICAL || level == RiskLevel.HIGH_RISK) {
            actions.add("DO NOT CLICK ANY LINKS (e.g. bit.ly). Real money deposits never require clicking links or entering passwords.")
            actions.add("Never enter your UPI PIN to 'receive' or 'claim' money — entering your PIN only sends money out of your account.")
            actions.add("Block the sender number immediately and report to National Cybercrime Helpline 1930 / cybercrime.gov.in.")
            actions.add("Warn your family members against this advance payment / claim link lure.")
        } else if (level == RiskLevel.CAUTION) {
            actions.add("Check your official bank app balance directly without clicking any links.")
            actions.add("Do not forward or share this message before verifying.")
        } else {
            actions.add("No immediate scam indicators found; maintain standard digital hygiene.")
        }

        return RiskAssessment(
            riskScore = score,
            riskLevel = level,
            scamCategory = if (level == RiskLevel.SAFE) ScamCategory.NONE else topCategory,
            confidence = if (score > 40 || score <= 10) 0.95f else 0.85f,
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
1. Never click links (e.g. bit.ly) claiming to give or confirm advance payments.
2. Never enter your UPI PIN to receive funds. Entering PIN always debits your account.
3. Pause, verify independently via your official banking app, and consult family.
        """.trimIndent()
    }
}

