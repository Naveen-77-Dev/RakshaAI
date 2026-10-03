package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.service.RakshaAutonomousProtectionService

/**
 * Boot and System Startup Receiver.
 *
 * Automatically resumes 24/7 background scam and extortion message analysis
 * immediately when the phone finishes booting or restarts.
 * Ensures protection is always active without manual opening of the mobile app.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Device lifecycle event received: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                val prefs = context.getSharedPreferences("raksha_prefs", Context.MODE_PRIVATE)
                val isAutonomousGuardEnabled = prefs.getBoolean("pref_auto_sms_guard", true)

                if (isAutonomousGuardEnabled) {
                    Log.d(TAG, "Auto-starting 24/7 autonomous protection service on device boot")
                    RakshaAutonomousProtectionService.start(context)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
