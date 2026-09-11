package com.example.engine

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class UpiPayload(
    val payeeVpa: String = "",
    val payeeName: String = "",
    val amount: String = "",
    val currency: String = "INR",
    val transactionNote: String = "",
    val merchantCode: String = "",
    val isPaymentRequest: Boolean = false
)

object QrAnalyzer {

    fun parseUpi(payload: String): UpiPayload? {
        val trimmed = payload.trim()
        if (trimmed.startsWith("upi://pay", ignoreCase = true) || trimmed.contains("pa=") || trimmed.contains("@ok") || trimmed.contains("@icici") || trimmed.contains("@ybl") || trimmed.contains("@paytm") || trimmed.contains("@axisbank") || trimmed.contains("@sbi") || trimmed.contains("@barodampay")) {
            val queryString = if (trimmed.contains("?")) trimmed.substringAfter("?") else trimmed
            var vpa = ""
            var name = ""
            var amount = ""
            var currency = "INR"
            var note = ""
            var mc = ""

            val pairs = if (queryString.contains("&")) queryString.split("&") else listOf(queryString)
            for (param in pairs) {
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = try {
                        URLDecoder.decode(parts[1].trim(), StandardCharsets.UTF_8.name())
                    } catch (_: Exception) {
                        parts[1].trim()
                    }
                    when (key.lowercase()) {
                        "pa" -> vpa = value
                        "pn" -> name = value
                        "am" -> amount = value
                        "cu" -> currency = value
                        "tn" -> note = value
                        "mc" -> mc = value
                    }
                } else if (param.contains("@") && !param.contains(" ")) {
                    vpa = param.trim()
                }
            }

            if (vpa.isNotEmpty() || trimmed.startsWith("upi://pay", ignoreCase = true)) {
                return UpiPayload(
                    payeeVpa = vpa,
                    payeeName = name,
                    amount = amount,
                    currency = currency,
                    transactionNote = note,
                    merchantCode = mc,
                    isPaymentRequest = true
                )
            }
        }
        return null
    }

    fun analyze(qrContent: String): Pair<RiskAssessment, UpiPayload?> {
        val trimmed = qrContent.trim()
        val upi = parseUpi(trimmed)

        if (upi != null) {
            val signals = mutableListOf<RiskSignal>()
            val actions = mutableListOf<String>()
            var score = 5 // Normal base baseline for recognized UPI format
            val lowerNote = upi.transactionNote.lowercase()
            val lowerName = upi.payeeName.lowercase()
            val lowerVpa = upi.payeeVpa.lowercase()

            val deceptiveTriggers = listOf(
                "refund", "cashback", "reward", "reversal", "bonus", "winner",
                "lottery", "olx", "army", "courier", "electric", "bill", "urgent",
                "pin", "receive", "accept", "credit", "free", "gift"
            )
            val matches = deceptiveTriggers.filter {
                lowerNote.contains(it) || lowerName.contains(it) || lowerVpa.contains(it)
            }

            if (matches.isNotEmpty()) {
                score += 65
                signals.add(
                    RiskSignal(
                        name = "Scam Deception Signature (${matches.joinToString(", ")})",
                        severity = SignalSeverity.HIGH,
                        explanation = "DANGEROUS SCAM TRIGGER: Scammers send payment QR codes pretending you will receive a refund or cashback. Approving this will DEBIT your account."
                    )
                )
            }

            // Check suspicious payment amount
            val amountNum = upi.amount.toDoubleOrNull() ?: 0.0
            if (amountNum >= 5000.0) {
                score += 25
                signals.add(
                    RiskSignal(
                        name = "Substantial Payment Amount (₹${upi.amount})",
                        severity = SignalSeverity.MEDIUM,
                        explanation = "High transaction amount encoded in QR code. Ensure the merchant or payee is verified."
                    )
                )
            }

            // Check if VPA looks like a suspicious handle
            if (lowerVpa.contains("refund") || lowerVpa.contains("helpdesk") || lowerVpa.contains("support") || lowerVpa.contains("officer")) {
                score += 20
                signals.add(
                    RiskSignal(
                        name = "Impersonation in VPA handle ($lowerVpa)",
                        severity = SignalSeverity.HIGH,
                        explanation = "Payee VPA contains words like 'refund' or 'officer' commonly used in impersonation scams."
                    )
                )
            }

            // Check if clean genuine merchant QR
            if (matches.isEmpty() && amountNum < 2000.0) {
                signals.add(
                    RiskSignal(
                        name = "Valid Merchant / Peer QR Signature",
                        severity = SignalSeverity.LOW,
                        explanation = "Recognized genuine UPI structure with valid payee address (${upi.payeeVpa.ifEmpty { "Standard VPA" }}). No malicious keywords found."
                    )
                )
            }

            // Always add the essential educational reminder
            signals.add(
                RiskSignal(
                    name = "UPI Golden Safety Rule",
                    severity = SignalSeverity.LOW,
                    explanation = "Entering your UPI PIN ALWAYS debits money from your account. Receiving money NEVER requires entering a UPI PIN."
                )
            )

            score = score.coerceIn(0, 100)
            val level = RiskLevel.fromScore(score)

            if (level.isSafe) {
                actions.add("Safe to proceed: Payee is ${upi.payeeName.ifEmpty { upi.payeeVpa }}")
                actions.add("Confirm amount before entering your secure UPI PIN")
            } else {
                actions.add("DO NOT ENTER YOUR UPI PIN - This will deduct money from your account")
                actions.add("Cancel this transaction immediately and report the sender")
                actions.add("Never scan a QR code if someone claims they are sending you money")
            }

            val assessment = RiskAssessment(
                riskScore = score,
                riskLevel = level,
                scamCategory = if (score >= 45) ScamCategory.UPI_SCAM else ScamCategory.NONE,
                confidence = 0.95f,
                signals = signals,
                recommendedActions = actions,
                highlightedPhrases = matches,
                inputPayload = trimmed
            )
            return Pair(assessment, upi)
        }

        // Check if QR is a URL
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.contains(".com") || trimmed.contains(".in") ||
            trimmed.contains(".cc") || trimmed.contains(".xyz") ||
            trimmed.contains(".top") || trimmed.contains(".ru")
        ) {
            val urlAssessment = UrlAnalyzer.analyze(trimmed)
            return Pair(urlAssessment, null)
        }

        // Plain text payload
        val msgAssessment = MessageAnalyzer.analyze(trimmed)
        return Pair(msgAssessment, null)
    }
}
