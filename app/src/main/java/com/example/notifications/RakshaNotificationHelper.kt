package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.RiskAssessment
import com.example.engine.RiskLevel

object RakshaNotificationHelper {

    const val CHANNEL_SCAM_ALERTS = "raksha_scam_alerts"
    const val CHANNEL_URL_INTERCEPT = "raksha_url_intercept"
    const val CHANNEL_AUTONOMOUS_SERVICE = "raksha_autonomous_service"

    private const val NOTIFICATION_ID_BASE = 1000
    const val NOTIFICATION_ID_SERVICE = 9999

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Scam SMS / RCS / SIM alerts channel (Maximum Importance for Heads-up Alert & Lock Screen display)
            val scamChannel = NotificationChannel(
                CHANNEL_SCAM_ALERTS,
                "RakshaAI Scam & Threat Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time automated alerts when deceptive SMS, RCS, or extortion messages are detected in the background"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(scamChannel)

            // URL protection channel
            val urlChannel = NotificationChannel(
                CHANNEL_URL_INTERCEPT,
                "RakshaAI Link Shield & Web Intercept",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive alerts when fraudulent URLs or phishing portals are intercepted before opening"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(urlChannel)

            // 24/7 Autonomous Background Service channel
            val serviceChannel = NotificationChannel(
                CHANNEL_AUTONOMOUS_SERVICE,
                "RakshaAI 24/7 Autonomous Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing status indicating automated background monitoring for SIM, SMS and RCS messages"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun buildAutonomousServiceNotification(context: Context): android.app.Notification {
        initNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_AUTONOMOUS_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🛡️ RakshaAI Autonomous Shield Active")
            .setContentText("Automatically analyzing incoming SIM, SMS & RCS messages in real time.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "RakshaAI 24/7 Background Sentinel is active.\n" +
                    "Incoming SMS, carrier alerts, and RCS messages are automatically analyzed without needing to open the app or unlock your device."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun postScamSmsAlert(
        context: Context,
        sender: String,
        messageBody: String,
        assessment: RiskAssessment,
        channelType: String = "SMS"
    ) {
        postScamMessageAlert(context, sender, messageBody, assessment, channelType)
    }

    fun postScamMessageAlert(
        context: Context,
        sender: String,
        messageBody: String,
        assessment: RiskAssessment,
        channelType: String = "SMS"
    ) {
        initNotificationChannels(context)

        val notificationId = NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 10000).toInt()

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_ROUTE", "scan_analysis")
            putExtra("EXTRA_ANALYSIS_TYPE", "MESSAGE")
            putExtra("EXTRA_PAYLOAD", messageBody)
            putExtra("EXTRA_SENDER", sender)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Call 1930 Helpline
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:1930")
        }
        val dialPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1,
            dialIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val topSignal = assessment.signals.firstOrNull()?.name ?: "Message Security Evaluation"
        val topExplanation = assessment.signals.firstOrNull()?.explanation
            ?: "Evaluated by on-device RakshaAI forensic neural rules."

        val containsPhishingLink = assessment.signals.any { it.name.contains("Phishing", ignoreCase = true) || it.name.contains("Link", ignoreCase = true) || it.name.contains("URL", ignoreCase = true) }
        val (accentColor, titlePrefix) = when (assessment.riskLevel) {
            RiskLevel.CRITICAL -> Pair(
                0xFFEF4444.toInt(),
                if (containsPhishingLink) "🔴 [PHISHING / SCAM ALERT]" else "🔴 [SCAM / FRAUD] $channelType DETECTED"
            )
            RiskLevel.HIGH_RISK -> Pair(
                0xFFEA580C.toInt(),
                if (containsPhishingLink) "🔴 [PHISHING THREAT]" else "🔴 [HIGH THREAT] DANGEROUS $channelType"
            )
            RiskLevel.CAUTION -> Pair(0xFFF59E0B.toInt(), "🟡 [UNKNOWN / PROMO] $channelType NOTICE")
            RiskLevel.SAFE -> Pair(0xFF10B981.toInt(), "🟢 [TRUSTED & SAFE] $channelType VERIFIED")
            RiskLevel.UNKNOWN -> Pair(0xFFF59E0B.toInt(), "🟡 [UNVERIFIED] $channelType ALERT")
        }

        val title = if (assessment.riskLevel == RiskLevel.SAFE) {
            "$titlePrefix: From $sender"
        } else {
            "$titlePrefix: ${assessment.scamCategory.displayName}"
        }
        val shortContent = when (assessment.riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH_RISK -> "🔴 DANGER: From $sender • Threat Score: ${assessment.riskScore}/100. $topSignal."
            RiskLevel.CAUTION, RiskLevel.UNKNOWN -> "🟡 CAUTION: From $sender • Score: ${assessment.riskScore}/100. $topSignal."
            RiskLevel.SAFE -> "🟢 VERIFIED: From $sender • Safe & legitimate message. No fraud signals detected."
        }

        val bigText = buildString {
            append("Origin: $channelType (SIM / Carrier Network)\n")
            append("Classification: ${when(assessment.riskLevel) {
                RiskLevel.CRITICAL -> "🔴 FRAUD / EXTORTION SCAM"
                RiskLevel.HIGH_RISK -> "🔴 HIGH THREAT"
                RiskLevel.CAUTION -> "🟡 UNKNOWN / PROMOTIONAL"
                RiskLevel.SAFE -> "🟢 TRUSTED & VERIFIED SAFE"
                RiskLevel.UNKNOWN -> "🟡 UNVERIFIED SENDER"
            }}\n")
            append("Risk Score: ${assessment.riskScore}/100 (${assessment.riskLevel})\n")
            append("Sender: $sender\n\n")
            if (assessment.riskLevel == RiskLevel.SAFE) {
                append("✅ Security Status: Clean on-device scan. No malicious links, credential traps, or coercion signatures found.\n")
            } else {
                append("Detected Threat: $topSignal\n")
                append("$topExplanation\n\n")
                append("🛡️ Recommended Action:\n")
                assessment.recommendedActions.take(2).forEach { action ->
                    append("• $action\n")
                }
                append("⚠️ Do NOT click embedded links or share bank OTP / UPI PIN.")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_SCAM_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setColor(accentColor)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            .setVibrate(if (assessment.riskScore >= 40) longArrayOf(0, 500, 200, 500) else longArrayOf(0, 150))
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "View Analysis",
                mainPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_call,
                "Helpline 1930",
                dialPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted yet on Android 13+
        }
    }

    fun postFraudUrlAlert(
        context: Context,
        url: String,
        assessment: RiskAssessment,
        source: String = "Link Shield"
    ) {
        initNotificationChannels(context)

        val notificationId = NOTIFICATION_ID_BASE + 20000 + (System.currentTimeMillis() % 10000).toInt()

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_ROUTE", "scan_analysis")
            putExtra("EXTRA_ANALYSIS_TYPE", "URL")
            putExtra("EXTRA_PAYLOAD", url)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Call 1930 Helpline
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:1930")
        }
        val dialPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1,
            dialIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val (accentColor, titlePrefix) = when (assessment.riskLevel) {
            RiskLevel.CRITICAL -> Pair(0xFFEF4444.toInt(), "🔴 [CRITICAL PHISHING ALERT]")
            RiskLevel.HIGH_RISK -> Pair(0xFFEA580C.toInt(), "🔴 [DECEPTIVE LINK BLOCKED]")
            RiskLevel.CAUTION -> Pair(0xFFF59E0B.toInt(), "🟡 [SUSPICIOUS LINK NOTICE]")
            RiskLevel.SAFE -> Pair(0xFF10B981.toInt(), "🟢 [VERIFIED SAFE LINK]")
            RiskLevel.UNKNOWN -> Pair(0xFFF59E0B.toInt(), "🟡 [UNVERIFIED LINK ALERT]")
        }

        val topSignal = assessment.signals.firstOrNull()?.name ?: "Deceptive Phishing Link"
        val topExplanation = assessment.signals.firstOrNull()?.explanation
            ?: "Detected malicious characteristics mimicking official platforms or credential harvesting traps."

        val title = if (assessment.riskLevel == RiskLevel.SAFE) {
            "$titlePrefix: Link Cleared"
        } else {
            "$titlePrefix: ${assessment.scamCategory.displayName}"
        }

        val shortContent = when (assessment.riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH_RISK -> "🔴 DANGER: Phishing Link • Score: ${assessment.riskScore}/100. $topSignal."
            RiskLevel.CAUTION, RiskLevel.UNKNOWN -> "🟡 CAUTION: Suspicious Link • Score: ${assessment.riskScore}/100. $topSignal."
            RiskLevel.SAFE -> "🟢 VERIFIED: Legitimate safe link. No phishing signals detected."
        }

        val bigText = buildString {
            append("Source: $source (Background Web Shield)\n")
            append("Target: $url\n")
            append("Threat Level: ${assessment.riskLevel} (${assessment.riskScore}/100)\n")
            append("Category: ${assessment.scamCategory.displayName}\n\n")
            if (assessment.riskLevel == RiskLevel.SAFE) {
                append("✅ Security Status: Verified official domain. Safe to open.\n")
            } else {
                append("Detected Danger: $topSignal\n")
                append("$topExplanation\n\n")
                append("🛡️ Recommended Protection:\n")
                assessment.recommendedActions.take(2).forEach { action ->
                    append("• $action\n")
                }
                append("⚠️ Do NOT enter bank passwords, credit cards, or UPI PIN on this website.")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_URL_INTERCEPT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setColor(accentColor)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            .setVibrate(if (assessment.riskScore >= 40) longArrayOf(0, 500, 200, 500) else longArrayOf(0, 150))
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Inspect Threat",
                mainPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_call,
                "Helpline 1930",
                dialPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission handling
        }
    }

    fun postPhishingLinkAlert(
        context: Context,
        url: String,
        source: String = "Web Intercept",
        assessment: RiskAssessment
    ) {
        postFraudUrlAlert(context, url, assessment, source)
    }
}
