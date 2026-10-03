package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SafetyRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AnalysisEventEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.TrustedContactEntity
import com.example.data.local.entity.UserEntity
import com.example.engine.AudioAnalyzer
import com.example.engine.AudioForensicResult
import com.example.engine.MessageAnalyzer
import com.example.engine.PaymentContext
import com.example.engine.PaymentRiskAnalyzer
import com.example.engine.QrAnalyzer
import com.example.engine.RiskAssessment
import com.example.engine.UpiPayload
import com.example.engine.UrlAnalyzer
import com.example.localization.AppLanguage
import com.example.notifications.RakshaNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AnalysisStage {
    data object Idle : AnalysisStage()
    data class Progress(val step: String, val progress: Float) : AnalysisStage()
    data class Completed(
        val assessment: RiskAssessment,
        val upiPayload: UpiPayload? = null,
        val audioForensic: AudioForensicResult? = null,
        val inputType: String = "URL"
    ) : AnalysisStage()
}

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = SafetyRepository(db)

    val user: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeContacts: StateFlow<List<TrustedContactEntity>> = repository.activeContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<AnalysisEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIncidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _protectionMode = MutableStateFlow("STANDARD") // STANDARD, ELDERLY, FAMILY
    val protectionMode: StateFlow<String> = _protectionMode.asStateFlow()

    private val prefs = application.getSharedPreferences("raksha_prefs", Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow<Boolean?>(
        if (prefs.contains("pref_dark_theme")) prefs.getBoolean("pref_dark_theme", false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    private val _isBackgroundSmsGuardActive = MutableStateFlow(
        prefs.getBoolean("pref_auto_sms_guard", true)
    )
    val isBackgroundSmsGuardActive: StateFlow<Boolean> = _isBackgroundSmsGuardActive.asStateFlow()

    private val _delayedSimulationCountdown = MutableStateFlow<Int?>(null)
    val delayedSimulationCountdown: StateFlow<Int?> = _delayedSimulationCountdown.asStateFlow()

    init {
        // Automatically start autonomous 24/7 background protection service if enabled
        if (_isBackgroundSmsGuardActive.value) {
            com.example.service.RakshaAutonomousProtectionService.start(application)
        }
    }

    private val _isUrlGuardActive = MutableStateFlow(
        prefs.getBoolean("pref_url_guard", true)
    )
    val isUrlGuardActive: StateFlow<Boolean> = _isUrlGuardActive.asStateFlow()

    private val _activeFraudInterceptUrl = MutableStateFlow<String?>(null)
    val activeFraudInterceptUrl: StateFlow<String?> = _activeFraudInterceptUrl.asStateFlow()

    private val _lastInterceptedSmsAlert = MutableStateFlow<RiskAssessment?>(null)
    val lastInterceptedSmsAlert: StateFlow<RiskAssessment?> = _lastInterceptedSmsAlert.asStateFlow()

    private val _lastInterceptedMessageEvent = MutableStateFlow<InterceptedMessageEvent?>(null)
    val lastInterceptedMessageEvent: StateFlow<InterceptedMessageEvent?> = _lastInterceptedMessageEvent.asStateFlow()

    private val _pendingNavigationRoute = MutableStateFlow<String?>(null)
    val pendingNavigationRoute: StateFlow<String?> = _pendingNavigationRoute.asStateFlow()

    fun setPendingNavigation(route: String?) {
        _pendingNavigationRoute.value = route
    }

    fun clearPendingNavigation() {
        _pendingNavigationRoute.value = null
    }

    fun toggleBackgroundSmsGuard() {
        val next = !_isBackgroundSmsGuardActive.value
        _isBackgroundSmsGuardActive.value = next
        prefs.edit().putBoolean("pref_auto_sms_guard", next).apply()
        if (next) {
            com.example.service.RakshaAutonomousProtectionService.start(getApplication())
        } else {
            com.example.service.RakshaAutonomousProtectionService.stop(getApplication())
        }
    }

    fun triggerDelayedBackgroundSimulation(
        seconds: Int = 5,
        customSender: String = "+91 98450 11223 (SIM Alert)",
        customBody: String = "URGENT ELECTRICITY ALERT: Power will be disconnected at 9:30 PM tonight due to unpaid bill update. Pay immediately at https://discom-bill-pay.xyz/urgent or call 9845011223",
        channelType: String = "SIM-to-SIM"
    ) {
        viewModelScope.launch {
            for (i in seconds downTo 1) {
                _delayedSimulationCountdown.value = i
                delay(1000)
            }
            _delayedSimulationCountdown.value = null
            simulateIncomingScamMessage(
                customSender = customSender,
                customBody = customBody,
                channelType = channelType
            )
        }
    }

    fun triggerDelayedPhishingUrlSimulation(
        seconds: Int = 5,
        customUrl: String = "https://sbi-card-reward-points.xyz/claim-now"
    ) {
        viewModelScope.launch {
            for (i in seconds downTo 1) {
                _delayedSimulationCountdown.value = i
                delay(1000)
            }
            _delayedSimulationCountdown.value = null
            simulatePhishingUrlAlert(customUrl)
        }
    }

    fun simulatePhishingUrlAlert(
        customUrl: String = "https://sbi-card-reward-points.xyz/claim-now"
    ) {
        viewModelScope.launch {
            val assessment = com.example.engine.UrlAnalyzer.analyze(customUrl)
            repository.recordAnalysis("URL", customUrl, assessment, userAction = "SIMULATED_PHISHING_ALERT")
            RakshaNotificationHelper.postFraudUrlAlert(
                context = getApplication(),
                url = customUrl,
                assessment = assessment,
                source = "Simulated Background Link Shield"
            )
            _activeFraudInterceptUrl.value = customUrl
        }
    }

    fun toggleUrlGuard() {
        val next = !_isUrlGuardActive.value
        _isUrlGuardActive.value = next
        prefs.edit().putBoolean("pref_url_guard", next).apply()
    }

    fun triggerUrlIntercept(url: String) {
        _activeFraudInterceptUrl.value = url
    }

    fun dismissUrlIntercept() {
        _activeFraudInterceptUrl.value = null
    }

    fun dismissSmsAlert() {
        _lastInterceptedSmsAlert.value = null
        _lastInterceptedMessageEvent.value = null
    }

    fun simulateIncomingScamMessage(
        customSender: String = "Telegram HR (RCS Chat)",
        customBody: String = "[RCS Verified Invite] Congratulations! You are shortlisted for Part-Time Review Work. Earn ₹4,500/day. Deposit initial registration ₹1,500 via UPI or register at https://task-earnings-telegram.vip/bonus",
        channelType: String = "RCS Chat"
    ) {
        viewModelScope.launch {
            val assessment = com.example.receiver.SmsReceiver.simulateIncomingMessage(
                context = getApplication(),
                sender = customSender,
                body = customBody,
                channelType = channelType
            )
            val event = InterceptedMessageEvent(assessment, customSender, customBody, channelType)
            _lastInterceptedMessageEvent.value = event
            _lastInterceptedSmsAlert.value = assessment
        }
    }

    fun simulateIncomingScamSms(
        customSender: String = "+91 98210 44321",
        customBody: String = "ALERT: Dear Customer, your SBI YONO account will be blocked today due to pending PAN KYC. Update immediately at https://sbi-yono-update-pan.cc/verify or call 9821044321"
    ) {
        simulateIncomingScamMessage(
            customSender = customSender,
            customBody = customBody,
            channelType = "SMS"
        )
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        val next = !isCurrentlyDark
        _isDarkTheme.value = next
        prefs.edit().putBoolean("pref_dark_theme", next).apply()
    }

    fun setDarkTheme(dark: Boolean?) {
        _isDarkTheme.value = dark
        if (dark != null) {
            prefs.edit().putBoolean("pref_dark_theme", dark).apply()
        } else {
            prefs.edit().remove("pref_dark_theme").apply()
        }
    }

    private val _analysisStage = MutableStateFlow<AnalysisStage>(AnalysisStage.Idle)
    val analysisStage: StateFlow<AnalysisStage> = _analysisStage.asStateFlow()

    private val _guidedTourStep = MutableStateFlow<Int?>(null)
    val guidedTourStep: StateFlow<Int?> = _guidedTourStep.asStateFlow()

    private val _lastSimulatedFamilyAlert = MutableStateFlow<String?>(null)
    val lastSimulatedFamilyAlert: StateFlow<String?> = _lastSimulatedFamilyAlert.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized(application)
            user.collect { u ->
                if (u != null) {
                    _currentLanguage.value = AppLanguage.fromCode(u.language)
                    _protectionMode.value = u.protectionMode
                }
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            repository.updateLanguage(lang.code)
        }
    }

    fun setProtectionMode(mode: String) {
        _protectionMode.value = mode
        viewModelScope.launch {
            repository.updateProtectionMode(mode)
        }
    }

    fun isElderlyMode(): Boolean = _protectionMode.value == "ELDERLY"

    fun resetAnalysis() {
        _analysisStage.value = AnalysisStage.Idle
    }

    fun startUrlAnalysis(url: String) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { UrlAnalyzer.analyze(url) },
                onFinalize = { assessment ->
                    repository.recordAnalysis("URL", url, assessment)
                    if (assessment.riskScore >= 35) {
                        RakshaNotificationHelper.postFraudUrlAlert(
                            context = getApplication(),
                            url = url,
                            assessment = assessment,
                            source = "On-Device URL Inspector"
                        )
                    }
                    AnalysisStage.Completed(assessment = assessment, inputType = "URL")
                }
            )
        }
    }

    fun startQrAnalysis(qrContent: String) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { QrAnalyzer.analyze(qrContent) },
                onFinalize = { (assessment, upi) ->
                    repository.recordAnalysis("QR", qrContent, assessment)
                    if (assessment.riskScore >= 35 && qrContent.startsWith("http", ignoreCase = true)) {
                        RakshaNotificationHelper.postFraudUrlAlert(
                            context = getApplication(),
                            url = qrContent,
                            assessment = assessment,
                            source = "QR Code Link Shield"
                        )
                    }
                    AnalysisStage.Completed(assessment = assessment, upiPayload = upi, inputType = "QR")
                }
            )
        }
    }

    fun startMessageAnalysis(message: String) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { MessageAnalyzer.analyze(message) },
                onFinalize = { assessment ->
                    repository.recordAnalysis("MESSAGE", message, assessment)
                    AnalysisStage.Completed(assessment = assessment, inputType = "MESSAGE")
                }
            )
        }
    }

    fun startAudioAnalysis(audioPathOrName: String, durationSec: Int = 15) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { AudioAnalyzer.analyzeAudioClip(audioPathOrName, durationSec) },
                onFinalize = { forensic ->
                    repository.recordAnalysis("AUDIO", audioPathOrName, forensic.assessment)
                    AnalysisStage.Completed(assessment = forensic.assessment, audioForensic = forensic, inputType = "AUDIO")
                }
            )
        }
    }

    fun startLiveVoiceAnalysis(spokenText: String, durationSec: Int = 0) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { AudioAnalyzer.analyzeLiveVoiceAndSpeech(spokenText, durationSec) },
                onFinalize = { forensic ->
                    repository.recordAnalysis("AUDIO", spokenText.take(60), forensic.assessment)
                    AnalysisStage.Completed(assessment = forensic.assessment, audioForensic = forensic, inputType = "AUDIO")
                }
            )
        }
    }

    fun startTranscriptAnalysis(transcript: String) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { AudioAnalyzer.analyzeTranscript(transcript) },
                onFinalize = { forensic ->
                    repository.recordAnalysis("AUDIO", transcript, forensic.assessment)
                    AnalysisStage.Completed(assessment = forensic.assessment, audioForensic = forensic, inputType = "AUDIO")
                }
            )
        }
    }

    fun startPaymentEvaluation(context: PaymentContext) {
        viewModelScope.launch {
            runMultiStageAnalysis(
                onCompute = { PaymentRiskAnalyzer.evaluate(context) },
                onFinalize = { assessment ->
                    repository.recordAnalysis("PAYMENT", "To ${context.recipientName} (₹${context.amount})", assessment)
                    AnalysisStage.Completed(assessment = assessment, inputType = "PAYMENT")
                }
            )
        }
    }

    private suspend fun <T> runMultiStageAnalysis(
        onCompute: suspend () -> T,
        onFinalize: suspend (T) -> AnalysisStage.Completed
    ) {
        _analysisStage.value = AnalysisStage.Progress("Reading input content & metadata...", 0.20f)
        delay(400)
        _analysisStage.value = AnalysisStage.Progress("Checking threat intelligence & known scam patterns...", 0.45f)
        delay(450)
        _analysisStage.value = AnalysisStage.Progress("Analyzing forensic features & deceptive indicators...", 0.70f)
        delay(400)
        _analysisStage.value = AnalysisStage.Progress("Comparing risk signals & preparing safe recommendations...", 0.90f)
        val computed = onCompute()
        delay(350)
        val result = onFinalize(computed)
        _analysisStage.value = result
    }

    fun sendTrustedContactAlert(reason: String) {
        val contactName = activeContacts.value.firstOrNull()?.name ?: "Emergency Contact"
        val alert = "Simulated SMS Alert sent to $contactName: 'RakshaAI Alert: High Risk Event ($reason) flagged. Please contact family member.'"
        _lastSimulatedFamilyAlert.value = alert
    }

    fun clearFamilyAlert() {
        _lastSimulatedFamilyAlert.value = null
    }

    fun setGuidedTourStep(step: Int?) {
        _guidedTourStep.value = step
    }
}

data class InterceptedMessageEvent(
    val assessment: RiskAssessment,
    val sender: String,
    val body: String,
    val channelType: String = "SMS" // "SMS", "RCS Chat", "MMS", "SIM Message"
)
