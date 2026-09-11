package com.example.engine

data class DemoScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String, // URL, QR, MESSAGE, AUDIO, PAYMENT
    val inputContent: String,
    val expectedCategory: ScamCategory,
    val expectedScoreRange: IntRange,
    val explanation: String,
    val recommendedActions: List<String>,
    val guidedStepIndex: Int? = null // For 90-second guided demo
)

object DemoScenarios {

    val SCENARIOS = listOf(
        DemoScenario(
            id = "sbi_kyc",
            title = "1. Fake Bank KYC Link",
            subtitle = "SMS with phishing URL impersonating SBI YONO",
            type = "URL",
            inputContent = "https://sbi-yono-update-pan.cc/verify",
            expectedCategory = ScamCategory.KYC_SCAM,
            expectedScoreRange = 80..100,
            explanation = "Fraudulent domain sbi-yono-update-pan.cc impersonates State Bank of India with suspicious .cc TLD and panic-inducing PAN update path.",
            recommendedActions = listOf(
                "Do not open link or enter banking credentials",
                "Official SBI portal is always onlinesbi.sbi",
                "Report to 1930 / cybercrime.gov.in"
            ),
            guidedStepIndex = 1
        ),
        DemoScenario(
            id = "courier_parcel",
            title = "2. Fake India Post Parcel Fee",
            subtitle = "Customs address fee phishing message",
            type = "URL",
            inputContent = "https://indiapost-parcels-update.xyz/pay",
            expectedCategory = ScamCategory.COURIER_SCAM,
            expectedScoreRange = 75..95,
            explanation = "Phishing attack spoofing India Post. Requests small fee (₹25) to trick victims into entering card details and OTPs on a cloned gateway.",
            recommendedActions = listOf(
                "India Post never sends parcel address update requests via .xyz domains",
                "Track official consignments only on indiapost.gov.in",
                "Block the sender number immediately"
            ),
            guidedStepIndex = 2
        ),
        DemoScenario(
            id = "ai_voice_emergency",
            title = "3. AI Voice Family Emergency",
            subtitle = "Cloned audio simulating accident extortion",
            type = "AUDIO",
            inputContent = "clone_son_accident_emergency_hospital_call.wav",
            expectedCategory = ScamCategory.AI_VOICE_SCAM,
            expectedScoreRange = 85..98,
            explanation = "Acoustic signatures display synthetic prosody artifacts, unnatural pitch flatness, and extreme manufactured secrecy ('Don't tell mom').",
            recommendedActions = listOf(
                "Hang up immediately — do not transfer funds",
                "Call your family member on their known real telephone number",
                "Ask a private family question that cannot be gleaned from social media"
            ),
            guidedStepIndex = 3
        ),
        DemoScenario(
            id = "digital_arrest",
            title = "4. Fake Police / Digital Arrest Threat",
            subtitle = "WhatsApp coercion alleging seized narcotics",
            type = "MESSAGE",
            inputContent = "This is Inspector Sharma from Delhi Crime Branch. A courier parcel with 5 passports and narcotics was intercepted in your name. You are under Digital Arrest. Transfer ₹1,50,000 security bail money immediately or local SWAT team will raid your house in 1 hour. Do not disconnect call.",
            expectedCategory = ScamCategory.IMPERSONATION,
            expectedScoreRange = 85..100,
            explanation = "Classic Digital Arrest coercion. Indian police and judicial agencies NEVER conduct arrests or demand bail settlements over video/voice calls.",
            recommendedActions = listOf(
                "Law enforcement never places anyone under 'digital arrest'",
                "Disconnect the call immediately and notify cyber helpline 1930",
                "Do not transfer any funds or share personal documents"
            ),
            guidedStepIndex = 4
        ),
        DemoScenario(
            id = "job_scam",
            title = "5. Fake Work-From-Home / Task Scam",
            subtitle = "Telegram daily earning promise",
            type = "MESSAGE",
            inputContent = "Part time job offer: Earn ₹3,000 to ₹5,000 daily by simply liking YouTube videos and rating Google hotels! No experience required. Instant daily payout to your UPI. Download app from link and join our Telegram group to claim your first ₹500 welcome task now.",
            expectedCategory = ScamCategory.JOB_SCAM,
            expectedScoreRange = 70..90,
            explanation = "Task-based Telegram fraud promising high daily income, which later traps victims into paying thousands in 'prepaid recharge fees'.",
            recommendedActions = listOf(
                "Legitimate employers never pay for liking YouTube videos or ask for advance deposits",
                "Do not join unofficial Telegram groups for financial tasks",
                "Block and report the sender"
            ),
            guidedStepIndex = 5
        ),
        DemoScenario(
            id = "kbc_lottery",
            title = "6. Fake Lottery / KBC Investment Reward",
            subtitle = "Advance tax payment fee scam",
            type = "MESSAGE",
            inputContent = "Congratulations! Your mobile SIM number has won ₹25,00,000 in KBC Kaun Banega Crorepati Lucky Draw 2026. To claim your prize amount in bank, deposit government tax processing fee of ₹12,500 to Manager SBI account immediately.",
            expectedCategory = ScamCategory.INVESTMENT_SCAM,
            expectedScoreRange = 75..95,
            explanation = "Advance fee fraud exploiting KBC lottery brand to trick victims into paying bogus taxes for non-existent prizes.",
            recommendedActions = listOf(
                "KBC and lottery programs do not select random phone numbers for prizes",
                "Never pay any 'processing fee' or 'tax' upfront to receive a prize"
            ),
            guidedStepIndex = 6
        ),
        DemoScenario(
            id = "safe_bank_sms",
            title = "7. Genuine Informational Bank Alert",
            subtitle = "Legitimate monthly salary credit notice",
            type = "MESSAGE",
            inputContent = "Your A/C ending in 4102 has been credited with INR 45,000.00 on 10-Sep-2026 by NEFT/Salary from TCS Ltd. Available Bal: INR 78,410.20 - HDFC Bank.",
            expectedCategory = ScamCategory.NONE,
            expectedScoreRange = 0..15,
            explanation = "Standard informational bank alert. Contains no suspicious links, requests no credentials or OTPs, and uses official nomenclature.",
            recommendedActions = listOf(
                "Informational notification — no action required",
                "Legitimate confirmation of funds credited"
            ),
            guidedStepIndex = 7
        ),
        DemoScenario(
            id = "safe_upi_payment",
            title = "8. Safe UPI Payment to Neighborhood Store",
            subtitle = "Known local grocery merchant payment",
            type = "PAYMENT",
            inputContent = "ramesh.groceries@okhdfcbank",
            expectedCategory = ScamCategory.NONE,
            expectedScoreRange = 0..15,
            explanation = "Standard low-value grocery transaction with known merchant name, verified handle, and absence of urgency or remote access demands.",
            recommendedActions = listOf(
                "Safe transaction parameters verified",
                "Ensure merchant name on terminal matches Ramesh Kirana Store"
            ),
            guidedStepIndex = 8
        )
    )

    fun getScenarioById(id: String): DemoScenario? = SCENARIOS.find { it.id == id }
}
