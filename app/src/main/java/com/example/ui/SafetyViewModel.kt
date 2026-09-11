package com.example.ui

import android.app.Application
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
