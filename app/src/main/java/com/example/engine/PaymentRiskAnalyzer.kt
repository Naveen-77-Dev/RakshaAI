package com.example.engine

data class PaymentContext(
    val amount: Double,
    val recipientName: String,
    val upiId: String,
    val paymentReason: String,
    val isNewRecipient: Boolean,
    val hasUrgencyOrFear: Boolean,
    val requestedOtpOrPinOrScreenShare: Boolean,
    val claimedImpersonationAuthority: Boolean, // family, police, bank, courier
    val isReceivingMoneyMisconception: Boolean = false // user thinks they are entering PIN to receive
)

object PaymentRiskAnalyzer {

    fun evaluate(context: PaymentContext): RiskAssessment {
        val signals = mutableListOf<RiskSignal>()
        val actions = mutableListOf<String>()
        var score = 0
        var category = ScamCategory.NONE

        if (context.isReceivingMoneyMisconception) {
            score += 65
            category = ScamCategory.UPI_SCAM
            signals.add(
                RiskSignal(
                    name = "Fatal PIN Reversal Misconception",
                    severity = SignalSeverity.HIGH,
                    explanation = "Entering your UPI PIN ALWAYS transfers money OUT of your bank account. Receiving money NEVER requires entering your PIN."
                )
            )
        }

        if (context.requestedOtpOrPinOrScreenShare) {
            score += 45
            category = ScamCategory.REMOTE_ACCESS_SCAM
            signals.add(
                RiskSignal(
                    name = "Remote Access / Credential Demand",
                    severity = SignalSeverity.HIGH,
                    explanation = "Recipient or caller requested screen sharing (AnyDesk) or OTP/PIN verification during this transaction."
                )
            )
        }

        if (context.claimedImpersonationAuthority) {
            score += 35
            category = ScamCategory.IMPERSONATION
            signals.add(
                RiskSignal(
                    name = "Authority / Impersonation Pretext",
                    severity = SignalSeverity.HIGH,
                    explanation = "The payment was initiated following claims of being police (digital arrest), customs courier, bank manager, or relative in crisis."
                )
            )
        }

        if (context.hasUrgencyOrFear) {
            score += 25
            signals.add(
                RiskSignal(
                    name = "Coercive Time Pressure",
                    severity = SignalSeverity.MEDIUM,
                    explanation = "Strict time deadline created ('pay immediately or face arrest/blocking'). Scammers manufacture haste to prevent independent reflection."
                )
            )
        }

        if (context.isNewRecipient) {
            score += 15
            signals.add(
                RiskSignal(
                    name = "First-Time Unknown Recipient",
                    severity = SignalSeverity.LOW,
                    explanation = "You have never successfully sent funds to this UPI handle (${context.upiId.ifEmpty { "new VPA" }}) before."
                )
            )
        }

        if (context.amount > 10000.0) {
            score += 15
            signals.add(
                RiskSignal(
                    name = "High Transaction Value (₹${context.amount})",
                    severity = SignalSeverity.MEDIUM,
                    explanation = "Substantial financial transfer requested. We recommend testing with ₹1 first or verifying over known voice/video call."
                )
            )
        }

        if (signals.isEmpty()) {
            score = 10
            signals.add(
                RiskSignal(
                    name = "Standard Transfer Profile",
                    severity = SignalSeverity.LOW,
                    explanation = "No overt coercion, screen-sharing, or authority impersonation reported for this payment."
                )
            )
        }

        score = score.coerceIn(0, 100)
        val level = RiskLevel.fromScore(score)

        if (level == RiskLevel.CRITICAL || level == RiskLevel.HIGH_RISK) {
            actions.add("PAUSE: Mandatory 5-minute cooldown recommended before taking any action")
            actions.add("Call your trusted family member to review this request together")
            actions.add("Do NOT proceed with UPI payment or enter your UPI PIN")
            actions.add("Contact cybercrime helpline 1930 if threatened with arrest or legal action")
        } else if (level == RiskLevel.CAUTION) {
            actions.add("Verify recipient identity independently via known contact number")
            actions.add("Send ₹1 trial payment first if this is a business or private purchase")
        } else {
            actions.add("Ensure the recipient name matches your intended beneficiary")
        }

        return RiskAssessment(
            riskScore = score,
            riskLevel = level,
            scamCategory = if (level == RiskLevel.SAFE) ScamCategory.NONE else (if (category == ScamCategory.NONE) ScamCategory.UPI_SCAM else category),
            confidence = 0.93f,
            signals = signals,
            recommendedActions = actions,
            inputPayload = "Payment of ₹${context.amount} to ${context.recipientName} (${context.upiId})"
        )
    }
}
