package com.example.engine

import java.net.URI
import java.util.Locale

object UrlAnalyzer {

    private val SUSPICIOUS_TLDS = setOf(
        "xyz", "top", "cc", "ru", "cn", "tk", "live", "icu", "link", "club", "work",
        "buzz", "rest", "surf", "fit", "gq", "ml", "cf", "ga"
    )

    private val SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "cutt.ly", "rb.gy", "goo.gl", "ow.ly", "buff.ly"
    )

    private val HIGH_PROFILE_BRANDS = mapOf(
        "sbi" to "State Bank of India",
        "yono" to "SBI YONO Banking",
        "hdfc" to "HDFC Bank",
        "icici" to "ICICI Bank",
        "axis" to "Axis Bank",
        "pnb" to "Punjab National Bank",
        "paytm" to "Paytm Wallet / Payments",
        "phonepe" to "PhonePe",
        "gpay" to "Google Pay",
        "indiapost" to "India Post Courier Service",
        "bluedart" to "Blue Dart Express",
        "delhivery" to "Delhivery Logistics",
        "incometax" to "Income Tax e-Filing",
        "epfo" to "EPFO Provident Fund",
        "uidai" to "UIDAI Aadhaar Portal",
        "parivahan" to "Parivahan Challan Portal",
        "amazon" to "Amazon India",
        "flipkart" to "Flipkart Shopping",
        "jio" to "Reliance Jio",
        "airtel" to "Airtel Telecommunications"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "kyc", "verify", "verification", "refund", "reward", "urgent", "account-blocked",
        "blocked", "update-pan", "pan-card", "claim", "prize", "parcel", "loan",
        "cashback", "lottery", "electricity-cut", "bill-due", "apk-download", "activate"
    )

    fun analyze(rawUrl: String): RiskAssessment {
        val trimmed = rawUrl.trim()
        val signals = mutableListOf<RiskSignal>()
        val actions = mutableListOf<String>()
        var score = 0
        var category = ScamCategory.NONE
        val lower = trimmed.lowercase(Locale.ROOT)

        val uri = try {
            val toParse = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else trimmed
            URI(toParse)
        } catch (_: Exception) {
            null
        }

        val host = uri?.host?.lowercase(Locale.ROOT) ?: ""
        val path = (uri?.rawPath ?: "") + (uri?.rawQuery ?: "")

        // 1. HTTPS inspection
        if (trimmed.startsWith("http://")) {
            score += 25
            signals.add(
                RiskSignal(
                    name = "Insecure HTTP Protocol",
                    severity = SignalSeverity.HIGH,
                    explanation = "The connection is not encrypted. Legitimate banks and services require HTTPS."
                )
            )
        } else if (trimmed.startsWith("https://")) {
            signals.add(
                RiskSignal(
                    name = "HTTPS Present (Not Proof of Safety)",
                    severity = SignalSeverity.LOW,
                    explanation = "Modern scam websites frequently use free HTTPS certificates. HTTPS only encrypts the connection, it does not guarantee legitimacy."
                )
            )
        }

        // 2. IP Address as hostname
        if (host.matches(Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$"""))) {
            score += 45
            category = ScamCategory.PHISHING
            signals.add(
                RiskSignal(
                    name = "Direct IP Address Host",
                    severity = SignalSeverity.HIGH,
                    explanation = "Legitimate institutions use registered domain names, not raw numerical IP addresses."
                )
            )
        }

        // 3. Punycode / Homoglyph indicators
        if (host.contains("xn--") || host.any { it.code > 127 }) {
            score += 40
            category = ScamCategory.PHISHING
            signals.add(
                RiskSignal(
                    name = "Punycode / Homoglyph Deception",
                    severity = SignalSeverity.HIGH,
                    explanation = "Contains special or lookalike characters designed to spoof authentic bank domains."
                )
            )
        }

        // 4. URL Shortener check
        if (SHORTENERS.any { host.endsWith(it) }) {
            score += 25
            signals.add(
                RiskSignal(
                    name = "Masked Short Link",
                    severity = SignalSeverity.MEDIUM,
                    explanation = "This link uses a shortening service ($host) which hides the real destination URL."
                )
            )
            actions.add("Do not click shortened links received from unknown senders")
        }

        // 5. Suspicious TLD check
        val tld = host.substringAfterLast(".", "")
        if (SUSPICIOUS_TLDS.contains(tld)) {
            score += 30
            category = ScamCategory.PHISHING
            signals.add(
                RiskSignal(
                    name = "High-Risk Domain Extension (.$tld)",
                    severity = SignalSeverity.HIGH,
                    explanation = "Top-level domain .$tld has a statistically elevated association with fraudulent campaigns."
                )
            )
        }

        // 6. Brand Impersonation
        for ((keyword, brandName) in HIGH_PROFILE_BRANDS) {
            if (host.contains(keyword)) {
                // Check if it's the legitimate official domain
                val isOfficial = when (keyword) {
                    "sbi" -> host.endsWith("onlinesbi.sbi") || host.endsWith("sbi.co.in")
                    "yono" -> host.endsWith("sbionline.sbi") || host.endsWith("sbi.co.in")
                    "hdfc" -> host.endsWith("hdfcbank.com")
                    "icici" -> host.endsWith("icicibank.com")
                    "axis" -> host.endsWith("axisbank.com")
                    "paytm" -> host.endsWith("paytm.com")
                    "phonepe" -> host.endsWith("phonepe.com")
                    "indiapost" -> host.endsWith("indiapost.gov.in")
                    "incometax" -> host.endsWith("incometax.gov.in")
                    "epfo" -> host.endsWith("epfindia.gov.in")
                    "uidai" -> host.endsWith("uidai.gov.in")
                    "parivahan" -> host.endsWith("parivahan.gov.in")
                    "amazon" -> host.endsWith("amazon.in") || host.endsWith("amazon.com")
                    "flipkart" -> host.endsWith("flipkart.com")
                    else -> false
                }

                if (!isOfficial) {
                    score += 45
                    category = if (keyword in listOf("sbi", "yono", "hdfc", "icici", "axis", "paytm")) {
                        ScamCategory.KYC_SCAM
                    } else if (keyword in listOf("indiapost", "bluedart", "delhivery")) {
                        ScamCategory.COURIER_SCAM
                    } else {
                        ScamCategory.IMPERSONATION
                    }
                    signals.add(
                        RiskSignal(
                            name = "Brand Impersonation ($brandName)",
                            severity = SignalSeverity.HIGH,
                            explanation = "The domain name references '$brandName' but is NOT hosted on their official verified portal."
                        )
                    )
                }
            }
        }

        // 7. Suspicious Path keywords
        val foundPathKeywords = SUSPICIOUS_KEYWORDS.filter { path.lowercase(Locale.ROOT).contains(it) || host.contains(it) }
        if (foundPathKeywords.isNotEmpty()) {
            score += 20 + (foundPathKeywords.size * 5)
            if (category == ScamCategory.NONE) {
                category = ScamCategory.PHISHING
            }
            signals.add(
                RiskSignal(
                    name = "High-Risk Path Patterns (${foundPathKeywords.joinToString(", ")})",
                    severity = SignalSeverity.HIGH,
                    explanation = "Contains urgency or credential-harvesting triggers common in Indian financial frauds."
                )
            )
        }

        // 8. Multiple Subdomain nesting
        val dotCount = host.count { it == '.' }
        if (dotCount > 3) {
            score += 15
            signals.add(
                RiskSignal(
                    name = "Excessive Subdomain Stacking",
                    severity = SignalSeverity.MEDIUM,
                    explanation = "Host structure ($host) has deep subdomain nesting, often used to conceal fraudulent servers."
                )
            )
        }

        // 9. Payment Handle in Link
        if (lower.contains("upi://") || lower.contains("pa=") || lower.contains("@ok") || lower.contains("@icici")) {
            score += 20
            signals.add(
                RiskSignal(
                    name = "Embedded UPI Payment Intent",
                    severity = SignalSeverity.MEDIUM,
                    explanation = "This link directly references a UPI payment string or recipient identifier."
                )
            )
        }

        // Default safety signal if clean
        if (signals.isEmpty() || (signals.size == 1 && signals[0].severity == SignalSeverity.LOW)) {
            signals.add(
                RiskSignal(
                    name = "Standard Domain Pattern",
                    severity = SignalSeverity.LOW,
                    explanation = "No known phishing keywords, suspicious TLDs, or homoglyph spoofing detected."
                )
            )
        }

        score = score.coerceIn(0, 100)
        val level = RiskLevel.fromScore(score)

        if (level == RiskLevel.CRITICAL || level == RiskLevel.HIGH_RISK) {
            actions.add("Do NOT enter any passwords, ATM PINs, OTPs, or PAN card numbers")
            actions.add("Do NOT download any APK or install remote assistance tools")
            actions.add("Report this link to cybercrime.gov.in or national helpline 1930")
        } else if (level == RiskLevel.CAUTION) {
            actions.add("Confirm destination website directly by typing official URL into browser")
            actions.add("Verify with your organization or bank before entering credentials")
        } else {
            actions.add("Link appears normal, but always verify before sending funds")
        }

        return RiskAssessment(
            riskScore = score,
            riskLevel = level,
            scamCategory = if (level == RiskLevel.SAFE) ScamCategory.NONE else category,
            confidence = if (score > 60 || score < 15) 0.92f else 0.78f,
            signals = signals,
            recommendedActions = actions,
            highlightedPhrases = foundPathKeywords,
            inputPayload = trimmed
        )
    }
}
