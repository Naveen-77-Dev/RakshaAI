package com.example.ui.screens.urlguard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.SafetyRepository
import com.example.engine.UrlAnalyzer
import com.example.notifications.RakshaNotificationHelper
import com.example.ui.theme.RakshaAITheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UrlGuardActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        private val URL_REGEX = Regex("""(https?://[^\s]+)""", RegexOption.IGNORE_CASE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rawText = intent.dataString
            ?: intent.getStringExtra(EXTRA_URL)
            ?: intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: "https://unknown"

        val incomingUrl = URL_REGEX.find(rawText)?.value ?: rawText

        // Asynchronously analyze and record the intercepted link in the Evidence Locker
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val assessment = UrlAnalyzer.analyze(incomingUrl)
                val repo = SafetyRepository.getInstance(applicationContext)
                repo.recordAnalysis(
                    type = "URL",
                    inputPreview = incomingUrl,
                    assessment = assessment,
                    userAction = if (assessment.riskScore >= 35) "INTERCEPTED_BY_SHIELD" else "SAFE_CLEARED"
                )

                // Trigger immediate local warning notification for phishing / high-risk links
                if (assessment.riskScore >= 35) {
                    RakshaNotificationHelper.postFraudUrlAlert(
                        context = applicationContext,
                        url = incomingUrl,
                        assessment = assessment,
                        source = "Browser / App Link Intercept"
                    )
                }
            } catch (_: Exception) {}
        }

        setContent {
            RakshaAITheme {
                UrlGuardScreen(
                    urlToInspect = incomingUrl,
                    onClose = {
                        finish()
                    },
                    onOpenExternalBrowser = { urlToOpen ->
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen)).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            startActivity(browserIntent)
                        } catch (_: Exception) {}
                        finish()
                    }
                )
            }
        }
    }
}
