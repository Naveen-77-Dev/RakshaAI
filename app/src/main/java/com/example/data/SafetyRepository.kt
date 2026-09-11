package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AnalysisEventEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.ScamPatternEntity
import com.example.data.local.entity.TrustedContactEntity
import com.example.data.local.entity.UserEntity
import com.example.engine.RiskAssessment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SafetyRepository(private val db: AppDatabase) {

    val allEvents: Flow<List<AnalysisEventEntity>> = db.analysisEventDao().getAllEvents()
    val highRiskEvents: Flow<List<AnalysisEventEntity>> = db.analysisEventDao().getHighRiskEvents()
    val activeContacts: Flow<List<TrustedContactEntity>> = db.trustedContactDao().getActiveContacts()
    val userFlow: Flow<UserEntity?> = db.userDao().getUserFlow()
    val allIncidents: Flow<List<IncidentEntity>> = db.incidentDao().getAllIncidents()

    suspend fun getEventsByType(type: String): Flow<List<AnalysisEventEntity>> {
        return db.analysisEventDao().getEventsByType(type)
    }

    suspend fun recordAnalysis(
        type: String,
        inputPreview: String,
        assessment: RiskAssessment,
        userAction: String = "VERIFIED"
    ): Long = withContext(Dispatchers.IO) {
        val signalsArray = JSONArray()
        assessment.signals.forEach { sig ->
            val obj = JSONObject()
            obj.put("name", sig.name)
            obj.put("severity", sig.severity.name)
            obj.put("explanation", sig.explanation)
            signalsArray.put(obj)
        }

        val recsArray = JSONArray()
        assessment.recommendedActions.forEach { recsArray.put(it) }

        val entity = AnalysisEventEntity(
            userId = 1,
            type = type,
            contentPreview = maskPii(inputPreview.take(180)),
            riskScore = assessment.riskScore,
            riskLevel = assessment.riskLevel.name,
            scamCategory = assessment.scamCategory.displayName,
            confidence = assessment.confidence,
            signalsJson = signalsArray.toString(),
            recommendedActionsJson = recsArray.toString(),
            userActionTaken = userAction,
            createdAt = System.currentTimeMillis()
        )
        db.analysisEventDao().insertEvent(entity)
    }

    suspend fun deleteEvent(id: Long) = withContext(Dispatchers.IO) {
        db.analysisEventDao().softDelete(id)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.analysisEventDao().clearAll()
        db.incidentDao().clearAll()
    }

    suspend fun saveIncident(
        amount: Double,
        paymentMethod: String,
        transactionReference: String,
        timestamp: Long,
        notes: String,
        evidence: String
    ): Long = withContext(Dispatchers.IO) {
        val entity = IncidentEntity(
            userId = 1,
            status = "ESCALATED_1930",
            amount = amount,
            paymentMethod = paymentMethod,
            transactionReference = transactionReference,
            timestamp = timestamp,
            notes = notes,
            evidenceReferences = evidence,
            createdAt = System.currentTimeMillis()
        )
        db.incidentDao().insertIncident(entity)
    }

    suspend fun addTrustedContact(
        name: String,
        phone: String,
        relationship: String,
        alertPreference: String = "CRITICAL_ONLY"
    ) = withContext(Dispatchers.IO) {
        val contact = TrustedContactEntity(
            userId = 1,
            name = name,
            phone = phone,
            relationship = relationship,
            alertPreferences = alertPreference,
            consentStatus = true
        )
        db.trustedContactDao().insertContact(contact)
    }

    suspend fun removeTrustedContact(id: Long) = withContext(Dispatchers.IO) {
        db.trustedContactDao().deleteContact(id)
    }

    suspend fun updateProtectionMode(mode: String) = withContext(Dispatchers.IO) {
        db.userDao().updateProtectionMode(1, mode)
    }

    suspend fun updateLanguage(lang: String) = withContext(Dispatchers.IO) {
        db.userDao().updateLanguage(1, lang)
    }

    suspend fun ensureInitialized(context: Context) = withContext(Dispatchers.IO) {
        val existingUser = db.userDao().getUser()
        if (existingUser == null) {
            db.userDao().insertOrUpdate(
                UserEntity(
                    id = 1,
                    name = "Vulnerable User",
                    phoneOrEmail = "user@rakshaai.safety",
                    language = "en",
                    protectionMode = "STANDARD"
                )
            )

            // Seed a default trusted contact
            db.trustedContactDao().insertContact(
                TrustedContactEntity(
                    userId = 1,
                    name = "Aarav (Son)",
                    phone = "+91 98765 43210",
                    relationship = "Family / Son",
                    alertPreferences = "CRITICAL_ONLY",
                    consentStatus = true
                )
            )

            // Initialized user and default emergency contact.
            // No fake demo events seeded. All records reflect real device analysis activity.
        }
    }

    private suspend fun seedDemoEvents() {
        val now = System.currentTimeMillis()
        val e1 = AnalysisEventEntity(
            userId = 1,
            type = "URL",
            contentPreview = "https://sbi-yono-update-pan.cc/verify",
            riskScore = 88,
            riskLevel = "CRITICAL",
            scamCategory = "Phishing Link",
            confidence = 0.94f,
            signalsJson = """[{"name":"Deceptive Domain","severity":"HIGH","explanation":"Domain sbi-yono-update-pan.cc mimics State Bank of India"},{"name":"Urgent PAN Update","severity":"HIGH","explanation":"Fake urgency to harvest banking credentials"}]""",
            recommendedActionsJson = """["Do not click link","Block SMS sender","Report to cybercrime.gov.in"]""",
            userActionTaken = "BLOCKED",
            createdAt = now - 3600000 * 2
        )
        val e2 = AnalysisEventEntity(
            userId = 1,
            type = "AUDIO",
            contentPreview = "Emergency call claiming son in police custody asking ₹25,000",
            riskScore = 92,
            riskLevel = "CRITICAL",
            scamCategory = "AI Voice Clone Scam",
            confidence = 0.89f,
            signalsJson = """[{"name":"Acoustic Prosody Artifacts","severity":"HIGH","explanation":"Abnormal pitch flatness and synthetic spectral pauses detected"},{"name":"Extreme Secrecy Demand","severity":"HIGH","explanation":"Caller demanded not informing spouse"}]""",
            recommendedActionsJson = """["Hang up immediately","Call son on known real phone number","Alert family"]""",
            userActionTaken = "ESCALATED",
            createdAt = now - 3600000 * 5
        )
        val e3 = AnalysisEventEntity(
            userId = 1,
            type = "QR",
            contentPreview = "upi://pay?pa=refunddesk.fast@icici&pn=Electric_Refund&am=1&tn=Reverse_Pay",
            riskScore = 78,
            riskLevel = "CRITICAL",
            scamCategory = "UPI Payment Fraud",
            confidence = 0.92f,
            signalsJson = """[{"name":"Reverse Charge Scam","severity":"HIGH","explanation":"Claiming to refund money but requesting UPI PIN debit"}]""",
            recommendedActionsJson = """["Do not enter UPI PIN","Receiving money NEVER needs a PIN"]""",
            userActionTaken = "CANCELLED",
            createdAt = now - 3600000 * 24
        )
        db.analysisEventDao().insertEvent(e1)
        db.analysisEventDao().insertEvent(e2)
        db.analysisEventDao().insertEvent(e3)
    }

    fun maskPii(text: String): String {
        // Mask 10-digit phone numbers
        var result = text.replace(Regex("""\b([6-9]\d{4})(\d{5})\b""")) { m ->
            "XXXXX " + m.groupValues[2]
        }
        // Mask UPI IDs: e.g. "username@bank" -> "us***@bank"
        result = result.replace(Regex("""\b([a-zA-Z0-9.\-_]{2})[a-zA-Z0-9.\-_]+(@[a-zA-Z]{2,})\b""")) { m ->
            "${m.groupValues[1]}***${m.groupValues[2]}"
        }
        return result
    }

    fun generateIncidentReportJson(incident: IncidentEntity, events: List<AnalysisEventEntity>): String {
        val root = JSONObject()
        root.put("reportTitle", "RakshaAI Incident Evidence Dossier")
        root.put("portalTarget", "National Cyber Crime Reporting Portal (cybercrime.gov.in) / Helpline 1930")
        root.put("incidentId", "RAKSHA-${incident.id}-${System.currentTimeMillis() % 10000}")
        root.put("timestamp", incident.createdAt)
        root.put("disputedAmountInr", incident.amount)
        root.put("paymentChannel", incident.paymentMethod)
        root.put("utrTransactionReference", incident.transactionReference)
        root.put("userNotes", maskPii(incident.notes))

        val evidenceArray = JSONArray()
        events.take(5).forEach { ev ->
            val item = JSONObject()
            item.put("type", ev.type)
            item.put("category", ev.scamCategory)
            item.put("riskScore", ev.riskScore)
            item.put("maskedPreview", ev.contentPreview)
            evidenceArray.put(item)
        }
        root.put("preservedForensicSignals", evidenceArray)
        root.put("disclaimer", "Preserved locally on user device. No remote raw recordings stored without consent.")
        return root.toString(2)
    }
}
