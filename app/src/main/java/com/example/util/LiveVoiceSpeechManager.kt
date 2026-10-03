package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

sealed class VoiceInputState {
    object Idle : VoiceInputState()
    object ReadyToSpeak : VoiceInputState()
    data class Listening(val rmsDb: Float = 0f) : VoiceInputState()
    data class PartialHypothesis(val text: String) : VoiceInputState()
    data class FinalResult(val spokenText: String) : VoiceInputState()
    data class Error(val message: String, val canFallback: Boolean = true) : VoiceInputState()
}

class LiveVoiceSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListeningFlow(): Flow<VoiceInputState> = callbackFlow {
        if (!isRecognitionAvailable()) {
            trySend(VoiceInputState.Error("Speech Recognition service not available on this device", canFallback = true))
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(VoiceInputState.ReadyToSpeak)
            }

            override fun onBeginningOfSpeech() {
                trySend(VoiceInputState.Listening(0f))
            }

            override fun onRmsChanged(rmsdB: Float) {
                trySend(VoiceInputState.Listening(rmsdB))
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // Done speaking
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error in speech recognition"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record Audio permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during recognition"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly into the microphone."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected within time limit"
                    else -> "Speech recognition error ($error)"
                }
                trySend(VoiceInputState.Error(errorMsg, canFallback = true))
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull()?.trim().orEmpty()
                if (spoken.isNotEmpty()) {
                    trySend(VoiceInputState.FinalResult(spoken))
                } else {
                    trySend(VoiceInputState.Error("No speech transcribed", canFallback = true))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    trySend(VoiceInputState.PartialHypothesis(text))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        recognizer.setRecognitionListener(listener)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            trySend(VoiceInputState.Error("Failed to start speech listener: ${e.message}", canFallback = true))
        }

        awaitClose {
            try {
                recognizer.stopListening()
                recognizer.cancel()
                recognizer.destroy()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
            speechRecognizer = null
        }
    }.flowOn(Dispatchers.Main)

    fun stop() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
