package com.example.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Telephony
import android.util.Log
import com.example.notifications.RakshaNotificationHelper
import com.example.receiver.SmsReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 24/7 Autonomous Background Protection Service.
 *
 * Runs automatically without requiring the user to open the mobile app or keep the device unlocked.
 * Automatically monitors:
 * 1. Cellular SIM SMS & MMS via direct ContentObserver on the telephony content provider.
 * 2. Incoming Telephony broadcast intents (SMS_RECEIVED, WAP_PUSH).
 * 3. Keeps background watchdog alive across device standby / doze.
 */
class RakshaAutonomousProtectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var contentObserver: SmsContentObserver? = null
    private var dynamicSmsReceiver: BroadcastReceiver? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Starting RakshaAI 24/7 Autonomous Protection Service")

        // 1. Foreground notification to guarantee zero background termination by Android OS
        RakshaNotificationHelper.initNotificationChannels(this)
        val notification = RakshaNotificationHelper.buildAutonomousServiceNotification(this)
        try {
            startForeground(RakshaNotificationHelper.NOTIFICATION_ID_SERVICE, notification)
        } catch (e: Exception) {
            Log.e(TAG, "startForeground error", e)
        }

        // 2. Register Telephony ContentObserver for direct SMS Inbox monitoring
        registerSmsObserver()

        // 3. Register high-priority dynamic broadcast receiver
        registerDynamicReceiver()

        isServiceRunning = true
    }

    private fun registerSmsObserver() {
        try {
            val handler = Handler(Looper.getMainLooper())
            contentObserver = SmsContentObserver(handler)
            contentResolver.registerContentObserver(
                Uri.parse("content://sms"),
                true,
                contentObserver!!
            )
            Log.d(TAG, "Telephony SMS ContentObserver successfully registered for autonomous scanning")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register Telephony SMS ContentObserver", e)
        }
    }

    private fun registerDynamicReceiver() {
        try {
            dynamicSmsReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (context == null || intent == null) return
                    val action = intent.action ?: return

                    if (action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
                        val grouped = mutableMapOf<String, StringBuilder>()
                        for (sms in messages) {
                            val sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: "Unknown Sender"
                            val body = sms.displayMessageBody ?: sms.messageBody ?: ""
                            grouped.getOrPut(sender) { StringBuilder() }.append(body)
                        }
                        for ((sender, builder) in grouped) {
                            val text = builder.toString()
                            if (text.isNotBlank()) {
                                SmsReceiver.processIncomingMessage(context, sender, text, "SMS")
                            }
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
                priority = 999
            }
            registerReceiver(dynamicSmsReceiver, filter)
            Log.d(TAG, "Dynamic SMS Receiver active")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register dynamic SMS receiver", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "RakshaAutonomousProtectionService onStartCommand (Sticky)")
        isServiceRunning = true
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "RakshaAutonomousProtectionService onDestroy")
        isServiceRunning = false
        try {
            contentObserver?.let { contentResolver.unregisterContentObserver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering ContentObserver", e)
        }
        try {
            dynamicSmsReceiver?.let { unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering dynamic receiver", e)
        }
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        serviceScope.cancel()
    }

    /**
     * Inspects newly inserted messages directly in the SMS provider.
     * Fires automatically in the background even if another app received the broadcast first.
     */
    private inner class SmsContentObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            inspectLatestMessage()
        }

        private fun inspectLatestMessage() {
            serviceScope.launch {
                acquireCpuWakeLock(5000L)
                try {
                    val cursor = contentResolver.query(
                        Uri.parse("content://sms/inbox"),
                        arrayOf("_id", "address", "body", "date"),
                        null,
                        null,
                        "date DESC"
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val addressIndex = it.getColumnIndex("address")
                            val bodyIndex = it.getColumnIndex("body")
                            val dateIndex = it.getColumnIndex("date")

                            val address = if (addressIndex >= 0) it.getString(addressIndex) ?: "Unknown" else "Unknown"
                            val body = if (bodyIndex >= 0) it.getString(bodyIndex) ?: "" else ""
                            val date = if (dateIndex >= 0) it.getLong(dateIndex) else 0L

                            // Only inspect messages received within the last 2 minutes
                            val now = System.currentTimeMillis()
                            if (body.isNotBlank() && (now - date) < 120_000L) {
                                Log.d(TAG, "Auto-detected newly arrived SMS in Inbox from $address: ${body.take(40)}")
                                SmsReceiver.processIncomingMessage(
                                    context = applicationContext,
                                    sender = address,
                                    body = body,
                                    channelType = "Auto-SIM Inbox"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error inspecting SMS inbox via ContentObserver", e)
                }
            }
        }
    }

    private fun acquireCpuWakeLock(durationMs: Long) {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RakshaAI:AutoGuardWakeLock")
            wakeLock?.acquire(durationMs)
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock acquisition error", e)
        }
    }

    companion object {
        private const val TAG = "RakshaAutoGuard"
        @Volatile
        var isServiceRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, RakshaAutonomousProtectionService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start RakshaAutonomousProtectionService", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RakshaAutonomousProtectionService::class.java)
            try {
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop RakshaAutonomousProtectionService", e)
            }
        }
    }
}
