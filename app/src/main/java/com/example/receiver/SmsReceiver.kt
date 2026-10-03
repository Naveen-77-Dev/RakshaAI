package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Telephony
import android.util.Log
import com.example.data.SafetyRepository
import com.example.engine.MessageAnalyzer
import com.example.engine.RiskAssessment
import com.example.engine.RiskLevel
import com.example.engine.UrlAnalyzer
import com.example.notifications.RakshaNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RakshaAI:SmsReceiverWakeLock")
        wakeLock?.acquire(15_000L)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    if (!messages.isNullOrEmpty()) {
                        val grouped = mutableMapOf<String, StringBuilder>()
                        for (sms in messages) {
                            val sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: "Unknown Sender"
                            val body = sms.displayMessageBody ?: sms.messageBody ?: ""
                            grouped.getOrPut(sender) { StringBuilder() }.append(body)
                        }

                        for ((sender, bodyBuilder) in grouped) {
                            val fullBody = bodyBuilder.toString()
                            if (fullBody.isNotBlank()) {
                                processIncomingMessage(context, sender, fullBody, channelType = "SMS")
                            }
                        }
                    }
                } else if (action == Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION) {
                    Log.d("SmsReceiver", "WAP_PUSH MMS message received on device")
                    processIncomingMessage(
                        context = context,
                        sender = "SIM Carrier MMS Push",
                        body = "MMS WAP Push payload received via SIM network",
                        channelType = "MMS"
                    )
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing SMS broadcast", e)
            } finally {
                wakeLock?.let {
                    if (it.isHeld) it.release()
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_ANALYZED = "com.example.ACTION_SMS_ANALYZED"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_RISK_SCORE = "extra_risk_score"
        const val EXTRA_RISK_LEVEL = "extra_risk_level"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_CHANNEL = "extra_channel"

        private val recentMessageDeduplication = LinkedHashMap<String, Long>()
        private const val DEDUP_WINDOW_MS = 60_000L // 1 minute

        private val URL_REGEX = Regex("""(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9\-.]+\.(?:xyz|top|cc|ru|link|club|buzz|site|info)[^\s]*)""", RegexOption.IGNORE_CASE)

        /**
         * Analyzes incoming message text (SMS, RCS chat, MMS, or direct SIM peer message),
         * combines URL forensics, stores the event into Room database, and triggers
         * proactive system notifications if risk is identified.
         */
        fun processIncomingMessage(
            context: Context,
            sender: String,
            body: String,
            channelType: String = "SMS"
        ): RiskAssessment {
            Log.d("SmsReceiver", "Processing incoming [$channelType] message from $sender: ${body.take(50)}")

            val dedupKey = "$sender|${body.hashCode()}"
            val now = System.currentTimeMillis()
            synchronized(recentMessageDeduplication) {
                val lastSeen = recentMessageDeduplication[dedupKey]
                if (lastSeen != null && (now - lastSeen) < DEDUP_WINDOW_MS) {
                    Log.d("SmsReceiver", "Skipping duplicate processing for $sender: already processed recently")
                    return MessageAnalyzer.analyze(body)
                }
                recentMessageDeduplication[dedupKey] = now
                if (recentMessageDeduplication.size > 200) {
                    val oldest = recentMessageDeduplication.keys.first()
                    recentMessageDeduplication.remove(oldest)
                }
            }

            // 1. On-device content NLP forensics
            var assessment = MessageAnalyzer.analyze(body)

            // 2. Embedded URL inspection
            val foundUrls = URL_REGEX.findAll(body).map { it.value }.toList()
            if (foundUrls.isNotEmpty()) {
                var maxUrlRisk = 0
                val urlSignals = mutableListOf<com.example.engine.RiskSignal>()
                for (rawUrl in foundUrls) {
                    val urlAssessment = UrlAnalyzer.analyze(rawUrl)
                    if (urlAssessment.riskScore > maxUrlRisk) {
                        maxUrlRisk = urlAssessment.riskScore
                        urlSignals.addAll(urlAssessment.signals)
                    }
                }

                if (maxUrlRisk > assessment.riskScore) {
                    val combinedSignals = (assessment.signals + urlSignals).distinctBy { it.name }
                    val elevatedScore = maxOf(assessment.riskScore, maxUrlRisk)
                    assessment = assessment.copy(
                        riskScore = elevatedScore,
                        riskLevel = RiskLevel.fromScore(elevatedScore),
                        signals = combinedSignals,
                        scamCategory = if (assessment.scamCategory == com.example.engine.ScamCategory.NONE) {
                            com.example.engine.ScamCategory.PHISHING
                        } else assessment.scamCategory
                    )
                }
            }

            // 3. Asynchronously record into local Room database
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = SafetyRepository.getInstance(context)
                    repo.recordAnalysis(
                        type = "MESSAGE",
                        inputPreview = "[$channelType] From: $sender | $body",
                        assessment = assessment,
                        userAction = if (assessment.riskScore >= 35) "FLAGGED_IN_BACKGROUND" else "VERIFIED_SAFE"
                    )
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error saving background message analysis", e)
                }
            }

            // 4. Proactive user alert: Truecaller-style instant color classification (Red: Fraud, Green: Trusted, Amber: Unknown)
            RakshaNotificationHelper.postScamMessageAlert(
                context = context,
                sender = sender,
                messageBody = body,
                assessment = assessment,
                channelType = channelType
            )

            // 5. Broadcast to in-app listeners for real-time UI refresh
            val broadcastIntent = Intent(ACTION_SMS_ANALYZED).apply {
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_RISK_SCORE, assessment.riskScore)
                putExtra(EXTRA_RISK_LEVEL, assessment.riskLevel.name)
                putExtra(EXTRA_CATEGORY, assessment.scamCategory.displayName)
                putExtra(EXTRA_CHANNEL, channelType)
                setPackage(context.packageName)
            }
            context.sendBroadcast(broadcastIntent)

            return assessment
        }

        /**
         * Test harness utility for evaluating background SMS interception and reporting
         * without physical SIM carrier signals (e.g. in browser emulator or reviewer tests).
         */
        fun simulateIncomingSms(
            context: Context,
            sender: String = "+91 98210 44321",
            body: String
        ): RiskAssessment {
            return processIncomingMessage(context, sender, body, channelType = "SMS")
        }

        fun simulateIncomingMessage(
            context: Context,
            sender: String,
            body: String,
            channelType: String = "RCS Chat"
        ): RiskAssessment {
            return processIncomingMessage(context, sender, body, channelType = channelType)
        }
    }
}
