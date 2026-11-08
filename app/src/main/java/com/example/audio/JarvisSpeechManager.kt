package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private var isTtsInitialized = false
  private var speechRecognizer: SpeechRecognizer? = null

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _audioRmsDb = MutableStateFlow(0f)
  val audioRmsDb: StateFlow<Float> = _audioRmsDb.asStateFlow()

  var onSpeechRecognized: ((String) -> Unit)? = null
  var onSpeechError: ((String) -> Unit)? = null

  init {
    tts = TextToSpeech(context.applicationContext, this)
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      tts?.let { engine ->
        val result = engine.setLanguage(Locale.US)
        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
          engine.setPitch(0.96f) // Slightly deeper, sophisticated Jarvis tone
          engine.setSpeechRate(1.04f)
          isTtsInitialized = true
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
          }

          override fun onError(utteranceId: String?, errorCode: Int) {
            _isSpeaking.value = false
          }
        })
      }
    }
  }

  fun speak(text: String, flush: Boolean = true) {
    if (!isTtsInitialized || tts == null) return
    val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
    val params = Bundle()
    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_${System.currentTimeMillis()}")
    tts?.speak(text, queueMode, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
  }

  fun stopSpeaking() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun startListening() {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      onSpeechError?.invoke("Speech recognition is not available on this device.")
      return
    }

    stopSpeaking()

    if (speechRecognizer == null) {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
          }

          override fun onBeginningOfSpeech() {
            _isListening.value = true
          }

          override fun onRmsChanged(rmsdB: Float) {
            // Normalize RMS dB to 0.0 - 1.0 range
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioRmsDb.value = normalized
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _isListening.value = false
            _audioRmsDb.value = 0f
          }

          override fun onError(error: Int) {
            _isListening.value = false
            _audioRmsDb.value = 0f
            val message = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Client recognition error"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
              SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
              SpeechRecognizer.ERROR_SERVER -> "Server error"
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input received"
              else -> "Speech recognition error ($error)"
            }
            onSpeechError?.invoke(message)
          }

          override fun onResults(results: Bundle?) {
            _isListening.value = false
            _audioRmsDb.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              val recognizedText = matches[0]
              onSpeechRecognized?.invoke(recognizedText)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {}

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    try {
      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (e: Exception) {
      _isListening.value = false
      onSpeechError?.invoke("Failed to start voice recognition: ${e.message}")
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (_: Exception) {}
    _isListening.value = false
    _audioRmsDb.value = 0f
  }

  fun destroy() {
    try {
      tts?.stop()
      tts?.shutdown()
      speechRecognizer?.destroy()
    } catch (_: Exception) {}
  }
}
