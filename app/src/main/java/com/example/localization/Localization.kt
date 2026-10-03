package com.example.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val scriptSymbol: String, // Short badge: "En", "हि", "తె", "ಕ"
    val shortCodeLabel: String // Compact pill text: "En", "हि", "తె", "ಕ"
) {
    ENGLISH("en", "English", "English", "En", "En"),
    HINDI("hi", "Hindi", "हिन्दी", "हि", "हि"),
    TELUGU("te", "Telugu", "తెలుగు", "తె", "తె"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", "ಕ", "ಕ");

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
        "shield_status_protected" to "Raksha Shield Active",
        "shield_status_subtitle" to "Real-time scam, fraud & voice clone detection active",
        "scan_anything_button" to "Scan Anything Now",
        "quick_action_link" to "Scan Link",
        "quick_action_qr" to "Scan QR",
        "quick_action_msg" to "Check Message",
        "quick_action_audio" to "Voice & Calls",
        "quick_action_pay" to "Before You Pay",
        "emergency_button" to "I may have been scammed",
        "emergency_subtitle" to "Emergency helpline 1930, bank freeze steps & reporting",
        "recent_scams_title" to "Recent Safety Scans",
        "safety_tips_title" to "Essential Safety Guides",
        "trusted_contact_card_title" to "Family Guardian Network",
        "risk_safe" to "Safe & Verified",
        "risk_caution" to "Caution Advised",
        "risk_high" to "High Risk Detected",
        "risk_critical" to "Critical Threat Alert",
        "risk_unknown" to "Analysis Inconclusive",
        "disclaimer_text" to "RakshaAI provides an automated risk assessment and cannot guarantee that content is safe or fraudulent. Verify important requests through an independent trusted channel.",
        "upi_pin_warning" to "Receiving money does not require entering your UPI PIN. Never enter your PIN to receive a payment.",
        "voice_disclaimer" to "Voice analysis is probabilistic. Signs are consistent with possible synthetic or manipulated audio. Verify the person through a known number or video call before sending money.",
        "elderly_mode_label" to "Senior Citizen Mode",
        "standard_mode_label" to "Standard Mode",
        "family_mode_label" to "Family Protection Mode",
        "helpline_1930_label" to "National Cyber Helpline (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in",
        "change_language" to "Change Language"
    )

    private val hi = mapOf(
        "app_title" to "रक्षा AI",
        "tagline" to "ठहरें। जांचें। सुरक्षित रहें।",
        "sub_tagline" to "नकली कॉल, संदिग्ध लिंक और धोखाधड़ी वाले भुगतानों से सुरक्षित रहें।",
        "shield_status_protected" to "रक्षा सुरक्षा कवच सक्रिय",
        "shield_status_subtitle" to "स्कैम, फ्रॉड और वॉयस क्लोन की रियल-टाइम पहचान चालू है",
        "scan_anything_button" to "कुछ भी स्कैन करें",
        "quick_action_link" to "लिंक जांचें",
        "quick_action_qr" to "QR स्कैन करें",
        "quick_action_msg" to "संदेश जांचें",
        "quick_action_audio" to "वॉयस और कॉल",
        "quick_action_pay" to "भुगतान से पहले",
        "emergency_button" to "शायद मेरे साथ धोखाधड़ी हुई है",
        "emergency_subtitle" to "आपातकालीन सहायता, हेल्पलाइन 1930 और रिपोर्ट दर्ज करें",
        "recent_scams_title" to "हाल के सुरक्षा स्कैन",
        "safety_tips_title" to "सुरक्षा सुझाव",
        "trusted_contact_card_title" to "पारिवारिक सुरक्षा नेटवर्क",
        "risk_safe" to "सुरक्षित व सत्यापित",
        "risk_caution" to "सावधानी आवश्यक",
        "risk_high" to "उच्च जोखिम पाया गया",
        "risk_critical" to "गंभीर धोखाधड़ी चेतावनी",
        "risk_unknown" to "जांच अधूरी",
        "disclaimer_text" to "रक्षा AI एक स्वचालित जोखिम विश्लेषण प्रदान करता है। किसी भी संदिग्ध मांग की पुष्टि स्वतंत्र माध्यम से करें।",
        "upi_pin_warning" to "पैसे प्राप्त करने के लिए कभी भी अपना UPI पिन दर्ज न करें। पैसे लेने के लिए पिन की जरूरत नहीं होती।",
        "voice_disclaimer" to "आवाज़ का विश्लेषण संभाव्य है। पैसे भेजने से पहले व्यक्ति को पुराने नंबर पर कॉल करके पुष्टि करें।",
        "elderly_mode_label" to "वरिष्ठ नागरिक मोड (सरल)",
        "standard_mode_label" to "मानक सुरक्षा मोड",
        "family_mode_label" to "परिवार सुरक्षा मोड",
        "helpline_1930_label" to "राष्ट्रीय साइबर हेल्पलाइन (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in",
        "change_language" to "भाषा बदलें"
    )

    private val te = mapOf(
        "app_title" to "రక్ష AI",
        "tagline" to "ఆగండి. సరిచూడండి. రక్షించుకోండి.",
        "sub_tagline" to "నకిలీ కాల్స్, అనుమానాస్పద లింకులు మరియు మోసపూరిత చెల్లింపుల నుండి రక్షణ పొందండి.",
        "shield_status_protected" to "రక్ష కవచం యాక్టివ్",
        "shield_status_subtitle" to "స్కామ్, ఫ్రాడ్ & వాయిస్ క్లోన్ శోధన సక్రియంగా ఉంది",
        "scan_anything_button" to "ఏదైనా స్కాన్ చేయండి",
        "quick_action_link" to "లింక్ తనిఖీ",
        "quick_action_qr" to "QR స్కాన్",
        "quick_action_msg" to "మెసేజ్ తనిఖీ",
        "quick_action_audio" to "వాయిస్ & కాల్స్",
        "quick_action_pay" to "చెల్లించే ముందు",
        "emergency_button" to "నేను మోసపోయానా?",
        "emergency_subtitle" to "సైబర్ హెల్ప్‌లైన్ 1930, బ్యాంక్ నిలుపుదల సూచనలు & ఫిర్యాదు",
        "recent_scams_title" to "ఇటీవలి రక్షణ స్కాన్‌లు",
        "safety_tips_title" to "ముఖ్యమైన భద్రతా చిట్కాలు",
        "trusted_contact_card_title" to "కుటుంబ రక్షణ నెట్‌వర్క్",
        "risk_safe" to "సురక్షితం",
        "risk_caution" to "జాగ్రత్త అవసరం",
        "risk_high" to "అధిక ప్రమాదం గుర్తించబడింది",
        "risk_critical" to "తీవ్రమైన మోసం హెచ్చరిక",
        "risk_unknown" to "స్పష్టత లేదు",
        "disclaimer_text" to "రక్ష AI ఆటోమేటెడ్ రిస్క్ విశ్లేషణను అందిస్తుంది. ఏదైనా ఆర్థిక లావాదేవీల ముందు నేరుగా సరిచూసుకోండి.",
        "upi_pin_warning" to "డబ్బులు స్వీకరించడానికి మీ UPI PIN నమోదు చేయవలసిన అవసరం లేదు. PIN ఎవరితోనూ పంచుకోవద్దు.",
        "voice_disclaimer" to "వాయిస్ విశ్లేషణ అంచనా ఆధారితం. డబ్బులు పంపే ముందు అసలు ఫోన్ నంబర్‌కు నేరుగా కాల్ చేయండి.",
        "elderly_mode_label" to "సీనియర్ సిటిజన్ మోడ్",
        "standard_mode_label" to "సాధారణ రక్షణ మోడ్",
        "family_mode_label" to "కుటుంబ రక్షణ మోడ్",
        "helpline_1930_label" to "జాతీయ సైబర్ హెల్ప్‌లైన్ (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in",
        "change_language" to "భాష మార్చండి"
    )

    private val kn = mapOf(
        "app_title" to "ರಕ್ಷಾ AI",
        "tagline" to "ನಿಲ್ಲಿಸಿ. ಪರಿಶೀಲಿಸಿ. ರಕ್ಷಿಸಿ.",
        "sub_tagline" to "ನಕಲಿ ಕರೆಗಳು, ಅಪಾಯಕಾರಿ ಲಿಂಕ್‌ಗಳು ಮತ್ತು ಹಣ ವರ್ಗಾವಣೆ ವಂಚನೆಗಳಿಂದ ರಕ್ಷಣೆ ಪಡೆಯಿರಿ.",
        "shield_status_protected" to "ರಕ್ಷಾ ಸಕ್ರಿಯ ರಕ್ಷಣೆ",
        "shield_status_subtitle" to "ನೈಜ ಸಮಯದಲ್ಲಿ ವಂಚನೆ ಮತ್ತು ವಾಯ್ಸ್ ಕ್ಲೋನ್ ಪತ್ತೆ ಸಕ್ರಿಯವಾಗಿದೆ",
        "scan_anything_button" to "ಏನನ್ನಾದರೂ ಸ್ಕ್ಯಾನ್ ಮಾಡಿ",
        "quick_action_link" to "ಲಿಂಕ್ ಪರಿಶೀಲಿಸಿ",
        "quick_action_qr" to "QR ಸ್ಕ್ಯಾನ್",
        "quick_action_msg" to "ಸಂದೇಶ ಪರಿಶೀಲಿಸಿ",
        "quick_action_audio" to "ಧ್ವನಿ & ಕರೆಗಳು",
        "quick_action_pay" to "ಹಣ ಪಾವತಿಸುವ ಮುನ್ನ",
        "emergency_button" to "ನನಗೆ ವಂಚನೆಯಾಗಿರಬಹುದು",
        "emergency_subtitle" to "ತುರ್ತು ನೆರವು, 1930 ಸಹಾಯವಾಣಿ ಮತ್ತು ದೂರು ದಾಖಲಿಸಿ",
        "recent_scams_title" to "ಇತ್ತೀಚಿನ ಸ್ಕ್ಯಾನ್‌ಗಳು",
        "safety_tips_title" to "ಸುರಕ್ಷತಾ ಸಲಹೆಗಳು",
        "trusted_contact_card_title" to "ಕುಟುಂಬ ರಕ್ಷಣಾ ನೆಟ್‌ವರ್ಕ್",
        "risk_safe" to "ಸುರಕ್ಷಿತ",
        "risk_caution" to "ಎಚ್ಚರಿಕೆ ಅಗತ್ಯ",
        "risk_high" to "ಹೆಚ್ಚಿನ ಅಪಾಯ ಪತ್ತೆಯಾಗಿದೆ",
        "risk_critical" to "ಗಂಭೀರ ವಂಚನೆ ಎಚ್ಚರಿಕೆ",
        "risk_unknown" to "ಸ್ಪಷ್ಟವಾಗಿಲ್ಲ",
        "disclaimer_text" to "ರಕ್ಷಾ AI ಸ್ವಯಂಚಾಲಿತ ವಿಶ್ಲೇಷಣೆಯನ್ನು ನೀಡುತ್ತದೆ. ಯಾವುದೇ ತುರ್ತು ಪಾವತಿಗೆ ಮುಂಚೆ ಅಧಿಕೃತ ಚಾನಲ್‌ಗಳಿಂದ ಪರಿಶೀಲಿಸಿ.",
        "upi_pin_warning" to "ಹಣ ಪಡೆಯಲು ನಿಮ್ಮ UPI PIN ನಮೂದಿಸುವ ಅಗತ್ಯವಿಲ್ಲ. ಹಣ ಸ್ವೀಕರಿಸಲು PIN ಹಾಕಬೇಡಿ.",
        "voice_disclaimer" to "ಧ್ವನಿ ವಿಶ್ಲೇಷಣೆ ಸಂಭವನೀಯವಾಗಿದೆ. ಹಣ ಕಳುಹಿಸುವ ಮುನ್ನ ಪರಿಚಿತ ಸಂಖ್ಯೆಗೆ ಕರೆ ಮಾಡಿ.",
        "elderly_mode_label" to "ಹಿರಿಯ ನಾಗರಿಕರ ಮೋಡ್",
        "standard_mode_label" to "ಸಾಮಾನ್ಯ ಮೋಡ್",
        "family_mode_label" to "ಕುಟುಂಬ ರಕ್ಷಣೆ ಮೋಡ್",
        "helpline_1930_label" to "ರಾಷ್ಟ್ರೀಯ ಸೈಬರ್ ಸಹಾಯವಾಣಿ (1930)",
        "portal_cybercrime_label" to "cybercrime.gov.in",
        "change_language" to "ಭಾಷೆ ಬದಲಾಯಿಸಿ"
    )

    fun get(key: String, lang: AppLanguage = AppLanguage.ENGLISH): String {
        val dict = when (lang) {
            AppLanguage.HINDI -> hi
            AppLanguage.TELUGU -> te
            AppLanguage.KANNADA -> kn
            AppLanguage.ENGLISH -> en
        }
        return dict[key] ?: en[key] ?: key
    }
}
