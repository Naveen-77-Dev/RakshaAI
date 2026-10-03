package com.example.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.receiver.SmsReceiver

/**
 * Real-Time Background Interceptor for RCS (Rich Communication Services),
 * MMS, and SIM-to-SIM Chat messages delivered via Google Messages,
 * Samsung Messages, carrier RCS hubs, and OEM messaging clients.
 */
class RakshaRcsMessageListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "RakshaRcsService"

        // Recognized Android Messaging Apps carrying RCS & SIM chat feeds
        val KNOWN_CARRIER_MESSAGING_PACKAGES = setOf(
            "com.google.android.apps.messaging", // Google Messages (Primary official RCS / Chat)
            "com.samsung.android.messaging",     // Samsung Messages (RCS & SIM)
            "com.android.mms",                   // Stock AOSP SIM SMS/MMS
            "com.oneplus.mms",                   // OnePlus Messages
            "com.coloros.mms",                   // Oppo / Realme Messages
            "com.xiaomi.midrop",                 // Xiaomi SIM Chat
            "com.verizon.messaging.vzmsgs",      // Verizon Message+
            "com.motorola.messaging",            // Moto Messaging
            "com.truecaller"                     // Truecaller SMS/Chat
        )

        // Cache of recently analyzed messages to avoid redundant notifications
        private val recentMessageHashes = LinkedHashMap<String, Long>()
        private const val DEDUPLICATION_WINDOW_MS = 60_000L // 1 minute

        fun isNotificationAccessGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return flat != null && flat.contains(packageName)
        }

        fun openNotificationAccessSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return

        // 1. Never intercept our own safety warnings
        if (pkgName == packageName) return

        // 2. Check if Auto Guard is active in user preferences
        val prefs = getSharedPreferences("raksha_settings", Context.MODE_PRIVATE)
        val isGuardActive = prefs.getBoolean("pref_auto_sms_guard", true)
        if (!isGuardActive) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // 3. Determine if this is an RCS or SIM messaging notification
        val isMessagingApp = pkgName in KNOWN_CARRIER_MESSAGING_PACKAGES
        val isMessageCategory = notification.category == Notification.CATEGORY_MESSAGE
        if (!isMessagingApp && !isMessageCategory) return

        // 4. Extract sender name or phone number
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: "SIM / RCS Sender"

        // 5. Extract message text (handling MessagingStyle for RCS threads)
        var messageText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        // Try extracting newest message from MessagingStyle if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val messagesParcelable = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messagesParcelable != null && messagesParcelable.isNotEmpty()) {
                val lastBundle = messagesParcelable.lastOrNull() as? Bundle
                val extractedText = lastBundle?.getCharSequence("text")?.toString()
                if (!extractedText.isNullOrBlank()) {
                    messageText = extractedText
                }
            }
        }

        if (messageText.isBlank()) return

        // 6. Deduplication filter (Messaging apps often update ongoing notifications)
        val dedupKey = "$pkgName|$sender|${messageText.hashCode()}"
        val now = System.currentTimeMillis()
        synchronized(recentMessageHashes) {
            val lastSeen = recentMessageHashes[dedupKey]
            if (lastSeen != null && (now - lastSeen) < DEDUPLICATION_WINDOW_MS) {
                return // already processed
            }
            recentMessageHashes[dedupKey] = now
            if (recentMessageHashes.size > 200) {
                val oldestKey = recentMessageHashes.keys.first()
                recentMessageHashes.remove(oldestKey)
            }
        }

        // 7. Resolve channel descriptor
        val channelType = when {
            pkgName == "com.google.android.apps.messaging" -> "RCS Chat"
            pkgName == "com.samsung.android.messaging" -> "Samsung RCS"
            pkgName.contains("mms") -> "MMS / SIM"
            else -> "SIM Chat"
        }

        Log.d(TAG, "Intercepted incoming [$channelType] message from $sender via $pkgName")

        // 8. Analyze and warn immediately
        SmsReceiver.processIncomingMessage(
            context = applicationContext,
            sender = sender,
            body = messageText,
            channelType = channelType
        )
    }
}
