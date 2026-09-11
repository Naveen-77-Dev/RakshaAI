package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}

object AppStrings {
    private val en = mapOf(
        "app_title" to "RakshaAI",
        "tagline" to "Pause. Verify. Protect.",
        "sub_tagline" to "Stay safer from scam calls, suspicious links, and risky payment requests.",
        "shield_status_protected" to "Raksha Active Protection",
        "shield_status_subtitle" to "Real-time scam & fraud detection enabled",
        "scan_anything_button" to "Scan Anything",
        "quick_action_link" to "Scan Link",
        "quick_action_qr" to "Scan QR",
        "quick_action_msg" to "Check Message",
        "quick_action_audio" to "Analyze Audio",
        "quick_action_pay" to "Before You Pay",
        "emergency_button" to "I may have been scammed",
        "emergency_subtitle" to "Emergency response, helpline 1930 & incident reporting",
        "recent_scams_title" to "Recent Risk Events",
        "safety_tips_title" to "Safety Tips for India",
        "trusted_contact_card_title" to "Family Safety Network",
        "risk_safe" to "Low Risk",
        "risk_caution" to "Caution Advised",
        "risk_high" to "High Risk Detected",
        "risk_critical" to "Critical Threat Alert",
        "risk_unknown" to "Analysis Inconclusive",
        "disclaimer_text" to "RakshaAI provides an automated risk assessment and cannot guarantee that content is safe or fraudulent. Verify important requests through an independent trusted channel.",
        "upi_pin_warning" to "Receiving money does not require entering your UPI PIN. Never enter your PIN to receive a payment.",
        "voice_disclaimer" to "Voice analysis is probabilistic. Signs are consistent with possible synthetic or manipulated audio. Verify the person through a known number or video call before sending money.",
        "elderly_mode_label" to "Elderly-Friendly Mode",
        "standard_mode_label" to "Standard Mode",
        "family_mode_label" to "Family Protection Mode",
        "helpline_1930_label" to "National Cyber Helpline (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in"
    )

    private val hi = mapOf(
        "app_title" to "रक्षा AI",
        "tagline" to "ठहरें। जांचें। सुरक्षित रहें।",
        "sub_tagline" to "नकली कॉल, संदिग्ध लिंक और धोखाधड़ी वाले भुगतानों से सुरक्षित रहें।",
        "shield_status_protected" to "रक्षा सक्रिय सुरक्षा",
        "shield_status_subtitle" to "स्कैम और फ्रॉड की रियल-टाइम पहचान चालू है",
        "scan_anything_button" to "कुछ भी स्कैन करें",
        "quick_action_link" to "लिंक जांचें",
        "quick_action_qr" to "QR स्कैन करें",
        "quick_action_msg" to "संदेश जांचें",
        "quick_action_audio" to "ऑडियो जांचें",
        "quick_action_pay" to "भुगतान से पहले",
        "emergency_button" to "शायद मेरे साथ धोखाधड़ी हुई है",
        "emergency_subtitle" to "आपातकालीन सहायता, हेल्पलाइन 1930 और रिपोर्ट दर्ज करें",
        "recent_scams_title" to "हाल के जोखिम इवेंट्स",
        "safety_tips_title" to "सुरक्षा सुझाव",
        "trusted_contact_card_title" to "पारिवारिक सुरक्षा नेटवर्क",
        "risk_safe" to "कम जोखिम",
        "risk_caution" to "सावधानी आवश्यक",
        "risk_high" to "उच्च जोखिम पाया गया",
        "risk_critical" to "गंभीर धोखाधड़ी चेतावनी",
        "risk_unknown" to "जांच अधूरी",
        "disclaimer_text" to "रक्षा AI एक स्वचालित जोखिम विश्लेषण प्रदान करता है। यह 100% गारंटी नहीं दे सकता। किसी भी संदिग्ध मांग की पुष्टि स्वतंत्र और सुरक्षित माध्यम से करें।",
        "upi_pin_warning" to "पैसे प्राप्त करने के लिए कभी भी अपना UPI पिन दर्ज न करें। पैसे लेने के लिए पिन की जरूरत नहीं होती।",
        "voice_disclaimer" to "आवाज़ का विश्लेषण संभाव्य है। संकेत बताते हैं कि यह AI द्वारा तैयार की गई आवाज़ हो सकती है। पैसे भेजने से पहले व्यक्ति को पुराने नंबर पर कॉल करके पुष्टि करें।",
        "elderly_mode_label" to "वरिष्ठ नागरिक मोड (सरल)",
        "standard_mode_label" to "मानक सुरक्षा मोड",
        "family_mode_label" to "परिवार सुरक्षा मोड",
        "helpline_1930_label" to "राष्ट्रीय साइबर हेल्पलाइन (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in"
    )

    private val kn = mapOf(
        "app_title" to "ರಕ್ಷಾ AI",
        "tagline" to "ನಿಲ್ಲಿಸಿ. ಪರಿಶೀಲಿಸಿ. ರಕ್ಷಿಸಿ.",
        "sub_tagline" to "ನಕಲಿ ಕರೆಗಳು, ಅಪಾಯಕಾರಿ ಲಿಂಕ್‌ಗಳು ಮತ್ತು ಹಣ ವರ್ಗಾವಣೆ ವಂಚನೆಗಳಿಂದ ರಕ್ಷಣೆ ಪಡೆಯಿರಿ.",
        "shield_status_protected" to "ರಕ್ಷಾ ಸಕ್ರಿಯ ರಕ್ಷಣೆ",
        "shield_status_subtitle" to "ನೈಜ ಸಮಯದಲ್ಲಿ ವಂಚನೆ ಪತ್ತೆ ಸಕ್ರಿಯವಾಗಿದೆ",
        "scan_anything_button" to "ಏನನ್ನಾದರೂ ಸ್ಕ್ಯಾನ್ ಮಾಡಿ",
        "quick_action_link" to "ಲಿಂಕ್ ಪರಿಶೀಲಿಸಿ",
        "quick_action_qr" to "QR ಸ್ಕ್ಯಾನ್",
        "quick_action_msg" to "ಸಂದೇಶ ಪರಿಶೀಲಿಸಿ",
        "quick_action_audio" to "ಧ್ವನಿ ಪರಿಶೀಲಿಸಿ",
        "quick_action_pay" to "ಹಣ ಪಾವತಿಸುವ ಮುನ್ನ",
        "emergency_button" to "ನನಗೆ ವಂಚನೆಯಾಗಿರಬಹುದು",
        "emergency_subtitle" to "ತುರ್ತು ನೆರವು, 1930 ಸಹಾಯವಾಣಿ ಮತ್ತು ದೂರು ದಾಖಲಿಸಿ",
        "recent_scams_title" to "ಇತ್ತೀಚಿನ ಅಪಾಯಗಳು",
        "safety_tips_title" to "ಸುರಕ್ಷತಾ ಸಲಹೆಗಳು",
        "trusted_contact_card_title" to "ಕುಟುಂಬ ರಕ್ಷಣಾ ನೆಟ್‌ವರ್ಕ್",
        "risk_safe" to "ಕಡಿಮೆ ಅಪಾಯ",
        "risk_caution" to "ಎಚ್ಚರಿಕೆ ಅಗತ್ಯ",
        "risk_high" to "ಹೆಚ್ಚಿನ ಅಪಾಯ ಪತ್ತೆಯಾಗಿದೆ",
        "risk_critical" to "ಗಂಭೀರ ವಂಚನೆ ಎಚ್ಚರಿಕೆ",
        "risk_unknown" to "ಸ್ಪಷ್ಟವಾಗಿಲ್ಲ",
        "disclaimer_text" to "ರಕ್ಷಾ AI ಸ್ವಯಂಚಾಲಿತ ವಿಶ್ಲೇಷಣೆಯನ್ನು ನೀಡುತ್ತದೆ. ಯಾವುದೇ ತುರ್ತು ಪಾವತಿಗೆ ಮುಂಚೆ ಅಧಿಕೃತ ಚಾನಲ್‌ಗಳಿಂದ ಪರಿಶೀಲಿಸಿ.",
        "upi_pin_warning" to "ಹಣ ಪಡೆಯಲು ನಿಮ್ಮ UPI PIN ನಮೂದಿಸುವ ಅಗತ್ಯವಿಲ್ಲ. ಹಣ ಸ್ವೀಕರಿಸಲು PIN ಹಾಕಬೇಡಿ.",
        "voice_disclaimer" to "ಧ್ವನಿ ವಿಶ್ಲೇಷಣೆ ಸಂಭವನೀಯವಾಗಿದೆ. ಇದು AI ಸೃಷ್ಟಿತ ಧ್ವನಿಯಾಗಿರಬಹುದು. ಹಣ ಕಳುಹಿಸುವ ಮುನ್ನ ಪರಿಚಿತ ಸಂಖ್ಯೆಗೆ ಕರೆ ಮಾಡಿ.",
        "elderly_mode_label" to "ಹಿರಿಯ ನಾಗರಿಕರ ಮೋಡ್",
        "standard_mode_label" to "ಸಾಮಾನ್ಯ ಮೋಡ್",
        "family_mode_label" to "ಕುಟುಂಬ ರಕ್ಷಣೆ ಮೋಡ್",
        "helpline_1930_label" to "ರಾಷ್ಟ್ರೀಯ ಸೈಬರ್ ಸಹಾಯವಾಣಿ (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in"
    )

    fun get(key: String, lang: AppLanguage = AppLanguage.ENGLISH): String {
        val dict = when (lang) {
            AppLanguage.HINDI -> hi
            AppLanguage.KANNADA -> kn
            AppLanguage.ENGLISH -> en
        }
        return dict[key] ?: en[key] ?: key
    }
}
