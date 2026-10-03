package com.example

import com.example.data.SafetyRepository
import com.example.engine.AudioAnalyzer
import com.example.engine.MessageAnalyzer
import com.example.engine.PaymentContext
import com.example.engine.PaymentRiskAnalyzer
import com.example.engine.QrAnalyzer
import com.example.engine.RiskLevel
import com.example.engine.ScamCategory
import com.example.engine.UrlAnalyzer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RakshaRiskEngineTest {

    @Test
    fun testPhishingUrlDetection() {
        val phishingUrl = "https://sbi-yono-update-pan.cc/verify"
        val assessment = UrlAnalyzer.analyze(phishingUrl)

        assertTrue("Expected score >= 70 for SBI impersonation with .cc TLD", assessment.riskScore >= 70)
        assertEquals(RiskLevel.CRITICAL, assessment.riskLevel)
        assertEquals(ScamCategory.KYC_SCAM, assessment.scamCategory)
        assertTrue("Signals must contain brand impersonation", assessment.signals.any { it.name.contains("Impersonation") })
    }

    @Test
    fun testSafeOfficialUrl() {
        val safeUrl = "https://onlinesbi.sbi/portal"
        val assessment = UrlAnalyzer.analyze(safeUrl)

        assertTrue("Safe score should be low", assessment.riskScore < 20)
        assertEquals(RiskLevel.SAFE, assessment.riskLevel)
    }

    @Test
    fun testUpiQrParsingAndScamDebitWarning() {
        val scamQr = "upi://pay?pa=cashback.desk.refund@icici&pn=Electric_Refund&am=2500&cu=INR&tn=Refund_Reversal"
        val (assessment, upi) = QrAnalyzer.analyze(scamQr)

        assertNotNull(upi)
        assertEquals("cashback.desk.refund@icici", upi?.payeeVpa)
        assertEquals("2500", upi?.amount)
        assertTrue(upi?.isPaymentRequest == true)

        assertTrue("Expected high risk for deceptive refund note", assessment.riskScore >= 60)
        assertTrue(
            "Signals must contain Debit Trigger warning",
            assessment.signals.any { it.name.contains("UPI Payment Debit Trigger") }
        )
    }

    @Test
    fun testDigitalArrestMessageClassification() {
        val msg = "This is Inspector Sharma from Delhi Crime Branch. A parcel with narcotics and fake passports was seized in your name. You are under Digital Arrest. Transfer ₹1,50,000 security deposit immediately."
        val assessment = MessageAnalyzer.analyze(msg)

        assertTrue("Digital arrest should have score >= 75", assessment.riskScore >= 75)
        assertEquals(ScamCategory.IMPERSONATION, assessment.scamCategory)
        assertTrue("Signals must detect Digital Arrest", assessment.signals.any { it.name.contains("Digital Arrest") })
    }

    @Test
    fun testTelegramJobScamClassification() {
        val msg = "Part time job offer: Earn ₹3,000 to ₹5,000 daily by simply liking YouTube videos and rating Google hotels! No experience required. Daily payout to your UPI."
        val assessment = MessageAnalyzer.analyze(msg)

        assertTrue(assessment.riskScore >= 50)
        assertEquals(ScamCategory.JOB_SCAM, assessment.scamCategory)
    }

    @Test
    fun testSyntheticVoiceScamForensics() = runBlocking {
        val result = AudioAnalyzer.analyzeAudioClip("clone_son_accident_emergency_hospital_call.wav", 15)

        assertEquals("High", result.voiceAuthenticityRisk)
        assertEquals("Critical", result.scamIntentRisk)
        assertTrue(result.assessment.riskScore >= 80)
        assertEquals(ScamCategory.AI_VOICE_SCAM, result.assessment.scamCategory)
        assertTrue(result.assessment.disclaimer.contains("Voice analysis is probabilistic"))
    }

    @Test
    fun testPrePaymentSafetyCooldownTrigger() {
        val context = PaymentContext(
            amount = 25000.0,
            recipientName = "Bail Officer",
            upiId = "officer.bail@axis",
            paymentReason = "Emergency bail",
            isNewRecipient = true,
            hasUrgencyOrFear = true,
            requestedOtpOrPinOrScreenShare = true,
            claimedImpersonationAuthority = true
        )
        val assessment = PaymentRiskAnalyzer.evaluate(context)

        assertTrue("Expected critical score for multi-signal fraud payment", assessment.riskScore >= 75)
        assertEquals(RiskLevel.CRITICAL, assessment.riskLevel)
        assertTrue("Must recommend cooldown pause", assessment.recommendedActions.any { it.contains("PAUSE") })
    }

    @Test
    fun testScamSmsWithPhishingLinkForensics() {
        val scamSms = "ALERT: Dear Customer, your SBI account is blocked. Update PAN immediately at https://sbi-yono-update-pan.cc/verify"
        val assessment = MessageAnalyzer.analyze(scamSms)
        assertTrue("Scam SMS should trigger risk score >= 50", assessment.riskScore >= 50)
        assertTrue(assessment.riskLevel != RiskLevel.SAFE)

        val urlAssessment = UrlAnalyzer.analyze("https://sbi-yono-update-pan.cc/verify")
        assertTrue("Phishing URL must score >= 70", urlAssessment.riskScore >= 70)
        assertEquals(RiskLevel.CRITICAL, urlAssessment.riskLevel)
        assertTrue(urlAssessment.signals.any { it.name.contains("Impersonation") })
    }

    @Test
    fun testRcsWorkFromHomeScamDetection() {
        val rcsMessage = "[RCS Business Chat] Congratulations! You are shortlisted for Part-Time Review Work. Earn ₹4,500/day. Deposit initial registration ₹1,500 via UPI or register at https://task-earnings-telegram.vip/bonus"
        val assessment = MessageAnalyzer.analyze(rcsMessage)

        assertTrue("RCS job scam should score >= 50", assessment.riskScore >= 50)
        assertTrue(assessment.riskLevel != RiskLevel.SAFE)
        assertTrue(assessment.scamCategory == ScamCategory.JOB_SCAM || assessment.scamCategory == ScamCategory.PHISHING || assessment.signals.isNotEmpty())
    }

    @Test
    fun testSimToSimElectricityCutoffExtortion() {
        val simMessage = "URGENT ELECTRICITY ALERT: Your power will be disconnected at 9:30 PM tonight due to pending bill update. Immediately call electricity officer at 9845011223 or settle bill at https://discom-bill-pay.xyz/urgent"
        val assessment = MessageAnalyzer.analyze(simMessage)

        assertTrue("Extortion message should score >= 50", assessment.riskScore >= 50)
        assertTrue(assessment.riskLevel != RiskLevel.SAFE)
        assertTrue(assessment.signals.any { it.name.contains("Urgency") || it.name.contains("Utility") || it.name.contains("Authority") })
    }
}
