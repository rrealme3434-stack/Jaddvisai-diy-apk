package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.JarvisSoundFx
import com.example.audio.JarvisSpeechManager
import com.example.data.ArcReactorState
import com.example.data.CommandType
import com.example.data.DiyDevice
import com.example.data.DiyPinout
import com.example.data.JarvisMessage
import com.example.data.JarvisRepository
import com.example.data.ProtocolRoutine
import com.example.data.SystemTelemetry
import com.example.telemetry.DeviceTelemetryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class HudTab {
  CORE_HUD,
  TERMINAL,
  DIY_HUB,
  PROTOCOLS,
  TELEMETRY
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = JarvisRepository()
  val telemetryManager = DeviceTelemetryManager(application)
  val speechManager = JarvisSpeechManager(application)

  val messages: StateFlow<List<JarvisMessage>> = repository.messages
  val diyDevices: StateFlow<List<DiyDevice>> = repository.diyDevices
  val protocols: StateFlow<List<ProtocolRoutine>> = repository.protocols
  val pinouts: StateFlow<List<DiyPinout>> = repository.pinouts
  val telemetry: StateFlow<SystemTelemetry> = telemetryManager.telemetry
  val isFlashlightOn: StateFlow<Boolean> = telemetryManager.isFlashlightOn

  private val _activeTab = MutableStateFlow(HudTab.CORE_HUD)
  val activeTab: StateFlow<HudTab> = _activeTab.asStateFlow()

  private val _soundFxEnabled = MutableStateFlow(true)
  val soundFxEnabled: StateFlow<Boolean> = _soundFxEnabled.asStateFlow()

  private val _statusText = MutableStateFlow("ALL SYSTEMS OPERATIONAL")
  val statusText: StateFlow<String> = _statusText.asStateFlow()

  private val _isAlertMode = MutableStateFlow(false)
  val isAlertMode: StateFlow<Boolean> = _isAlertMode.asStateFlow()

  val reactorState: StateFlow<ArcReactorState> = combine(
    speechManager.isListening,
    speechManager.isSpeaking,
    _isAlertMode,
    speechManager.audioRmsDb,
    _statusText
  ) { isListening, isSpeaking, isAlert, rms, status ->
    val reactorStatus = when {
      isAlert -> "ALERT / LOCKDOWN"
      isListening -> "LISTENING..."
      isSpeaking -> "VOCALIZING..."
      else -> status
    }
    ArcReactorState(
      status = reactorStatus,
      energyPercent = if (isAlert) 100.0f else 99.4f,
      isListening = isListening,
      isSpeaking = isSpeaking,
      isAlert = isAlert,
      audioRms = rms
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    ArcReactorState()
  )

  init {
    speechManager.onSpeechRecognized = { recognizedText ->
      processUserQuery(recognizedText)
    }

    speechManager.onSpeechError = { errorMsg ->
      _statusText.value = "VOICE INPUT ERROR"
      repository.addMessage(
        JarvisMessage(
          id = UUID.randomUUID().toString(),
          text = "Voice interface note: $errorMsg. You may also type your commands into the terminal, sir.",
          isFromUser = false
        )
      )
    }

    // Periodic telemetry update
    viewModelScope.launch {
      while (true) {
        telemetryManager.refreshTelemetry()
        delay(8000)
      }
    }
  }

  fun setActiveTab(tab: HudTab) {
    JarvisSoundFx.playClick()
    _activeTab.value = tab
  }

  fun toggleSoundFx() {
    val current = _soundFxEnabled.value
    _soundFxEnabled.value = !current
    JarvisSoundFx.soundEnabled = !current
  }

  fun startVoiceListening() {
    JarvisSoundFx.playCommandAcknowledged()
    _statusText.value = "LISTENING..."
    speechManager.startListening()
  }

  fun stopVoiceListening() {
    speechManager.stopListening()
    _statusText.value = "STANDBY"
  }

  fun triggerReactorPulse() {
    JarvisSoundFx.playReactorCharge()
    telemetryManager.triggerHapticFeedback(80)
    _statusText.value = "REACTOR RE-CHARGED"
    viewModelScope.launch {
      delay(2000)
      _statusText.value = "ALL SYSTEMS OPERATIONAL"
    }
  }

  fun clearTerminal() {
    repository.clearMessages()
  }

  fun toggleDevice(deviceId: String) {
    viewModelScope.launch {
      val (success, feedback) = repository.toggleDevice(deviceId)
      JarvisSoundFx.playCommandAcknowledged()
      telemetryManager.triggerHapticFeedback(50)
      repository.addMessage(
        JarvisMessage(
          id = UUID.randomUUID().toString(),
          text = feedback,
          isFromUser = false,
          commandType = CommandType.DIY_IOT
        )
      )
    }
  }

  fun togglePinState(pinNumber: Int, currentState: Boolean) {
    repository.updatePinDigitalState(pinNumber, !currentState)
    JarvisSoundFx.playClick()
    telemetryManager.triggerHapticFeedback(40)
  }

  fun setPinPwm(pinNumber: Int, value: Float) {
    repository.updatePinAnalogValue(pinNumber, value)
  }

  fun addCustomDevice(device: DiyDevice) {
    repository.addDiyDevice(device)
    JarvisSoundFx.playCommandAcknowledged()
    speakResponse("New DIY hardware node ${device.name} added to the grid, sir.")
  }

  fun removeDevice(deviceId: String) {
    repository.removeDiyDevice(deviceId)
    JarvisSoundFx.playClick()
  }

  fun toggleFlashlight() {
    val newState = telemetryManager.toggleFlashlight()
    JarvisSoundFx.playCommandAcknowledged()
    telemetryManager.triggerHapticFeedback(60)
    val stateStr = if (telemetryManager.isFlashlightOn.value) "Tactical torch engaged." else "Tactical torch disengaged."
    speakResponse(stateStr)
  }

  fun executeProtocol(protocol: ProtocolRoutine) {
    viewModelScope.launch {
      _statusText.value = "EXECUTING ${protocol.codeName}"
      repository.markProtocolExecuted(protocol.id)
      telemetryManager.triggerHapticFeedback(120)

      if (protocol.id == "lockdown") {
        _isAlertMode.value = true
      } else {
        _isAlertMode.value = false
      }

      repository.addMessage(
        JarvisMessage(
          id = UUID.randomUUID().toString(),
          text = "Execute: ${protocol.title} [${protocol.codeName}]",
          isFromUser = true,
          commandType = CommandType.PROTOCOL
        )
      )

      for (action in protocol.actions) {
        when (action.actionType) {
          com.example.data.ActionType.SOUND_FX -> {
            when (action.parameters) {
              "REACTOR_CHARGE" -> JarvisSoundFx.playReactorCharge()
              "ALERT" -> JarvisSoundFx.playAlert()
              else -> JarvisSoundFx.playCommandAcknowledged()
            }
          }
          com.example.data.ActionType.VIBRATE -> {
            val ms = action.parameters.toLongOrNull() ?: 100L
            telemetryManager.triggerHapticFeedback(ms)
          }
          com.example.data.ActionType.FLASHLIGHT_STROBE -> {
            val count = action.parameters.toIntOrNull() ?: 2
            repeat(count) {
              telemetryManager.toggleFlashlight(true)
              delay(150)
              telemetryManager.toggleFlashlight(false)
              delay(150)
            }
          }
          com.example.data.ActionType.SPEAK -> {
            speakResponse(action.parameters)
            repository.addMessage(
              JarvisMessage(
                id = UUID.randomUUID().toString(),
                text = action.parameters,
                isFromUser = false,
                commandType = CommandType.PROTOCOL
              )
            )
          }
          com.example.data.ActionType.TRIGGER_DEVICE -> {
            // trigger first device
            diyDevices.value.firstOrNull()?.let { dev ->
              repository.toggleDevice(dev.id)
            }
          }
        }
        delay(250)
      }

      delay(3000)
      _statusText.value = "ALL SYSTEMS OPERATIONAL"
    }
  }

  fun addCustomProtocol(title: String, codeName: String, trigger: String, description: String, speakText: String) {
    val routine = ProtocolRoutine(
      id = "custom_${System.currentTimeMillis()}",
      title = title,
      codeName = codeName.ifBlank { "MK-CUSTOM" },
      description = description,
      voiceTrigger = trigger,
      actions = listOf(
        com.example.data.ProtocolAction(com.example.data.ActionType.SOUND_FX, "ACKNOWLEDGE"),
        com.example.data.ProtocolAction(com.example.data.ActionType.SPEAK, speakText)
      )
    )
    repository.addProtocol(routine)
    JarvisSoundFx.playCommandAcknowledged()
    speakResponse("Custom protocol $title has been compiled and added to your command protocol register, sir.")
  }

  fun processUserQuery(query: String) {
    val clean = query.trim()
    if (clean.isBlank()) return

    // Add user message
    repository.addMessage(
      JarvisMessage(
        id = UUID.randomUUID().toString(),
        text = clean,
        isFromUser = true
      )
    )

    JarvisSoundFx.playCommandAcknowledged()
    telemetryManager.triggerHapticFeedback(40)

    val lower = clean.lowercase()

    // 1. Check Protocols
    val matchedProtocol = protocols.value.find { proto ->
      lower.contains(proto.voiceTrigger.lowercase()) || lower.contains(proto.title.lowercase())
    }
    if (matchedProtocol != null) {
      executeProtocol(matchedProtocol)
      return
    }

    // 2. Flashlight / Torch
    if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("light on") || lower.contains("light off")) {
      val turnOn = !lower.contains("off")
      telemetryManager.toggleFlashlight(turnOn)
      val reply = if (turnOn) "Tactical illumination engaged, sir." else "Tactical illumination extinguished, sir."
      speakResponse(reply)
      repository.addMessage(
        JarvisMessage(
          id = UUID.randomUUID().toString(),
          text = reply,
          isFromUser = false,
          commandType = CommandType.FLASHLIGHT
        )
      )
      return
    }

    // 3. Diagnostics & Telemetry
    if (lower.contains("status") || lower.contains("diagnostic") || lower.contains("battery") || lower.contains("system")) {
      val t = telemetryManager.refreshTelemetry()
      val reply = "System diagnostics summary: Battery is at ${t.batteryPercent}% (${t.chargeSource}). RAM usage is at ${t.ramUsagePercent}% (${t.usedRamMb}MB of ${t.totalRamMb}MB). Network is ${t.networkType}. Arc Core output is at ${t.arcReactorOutput}%. All circuits functioning normally, sir."
      speakResponse("All systems operational, sir. Battery at ${t.batteryPercent}%, RAM utilization at ${t.ramUsagePercent}%.")
      repository.addMessage(
        JarvisMessage(
          id = UUID.randomUUID().toString(),
          text = reply,
          isFromUser = false,
          commandType = CommandType.SYSTEM_CHECK,
          suggestedActions = listOf("View Full Telemetry", "Run Security Sweep", "Toggle Flashlight")
        )
      )
      return
    }

    // 4. DIY IoT Device Controls
    val matchedDevice = diyDevices.value.find { dev ->
      lower.contains(dev.name.lowercase()) ||
        (lower.contains("esp32") && dev.deviceType == com.example.data.DiyDeviceType.ESP32) ||
        (lower.contains("gate") && dev.deviceType == com.example.data.DiyDeviceType.RASPBERRY_PI) ||
        (lower.contains("fan") && dev.deviceType == com.example.data.DiyDeviceType.ARDUINO)
    }
    if (matchedDevice != null && (lower.contains("turn") || lower.contains("toggle") || lower.contains("switch") || lower.contains("trigger"))) {
      toggleDevice(matchedDevice.id)
      val reply = "Transmitting relay toggle instruction to ${matchedDevice.name} at ${matchedDevice.ipAddressOrUrl}, sir."
      speakResponse(reply)
      return
    }

    // 5. Calculations
    if (lower.startsWith("calculate") || lower.startsWith("what is ") && (lower.contains("+") || lower.contains("-") || lower.contains("*") || lower.contains("/") || lower.contains("percent"))) {
      val calcResult = evaluateSimpleMath(lower)
      if (calcResult != null) {
        val reply = "According to my calculations, sir: $calcResult"
        speakResponse(reply)
        repository.addMessage(
          JarvisMessage(
            id = UUID.randomUUID().toString(),
            text = reply,
            isFromUser = false,
            commandType = CommandType.CALCULATION
          )
        )
        return
      }
    }

    // 6. Conversational Jarvis Persona Responses
    val response = generateJarvisResponse(clean)
    speakResponse(response)
    repository.addMessage(
      JarvisMessage(
        id = UUID.randomUUID().toString(),
        text = response,
        isFromUser = false,
        commandType = CommandType.GENERAL,
        suggestedActions = listOf("Check Diagnostics", "Protocol Clean Slate", "DIY Hardware Matrix")
      )
    )
  }

  private fun speakResponse(text: String) {
    speechManager.speak(text)
  }

  private fun evaluateSimpleMath(query: String): String? {
    return try {
      val expr = query.replace("calculate", "").replace("what is", "").replace("?", "").trim()
      if (expr.contains("percent of") || expr.contains("% of")) {
        val parts = expr.split(Regex("percent of|% of"))
        val p = parts[0].trim().toDouble()
        val total = parts[1].trim().toDouble()
        val res = (p / 100.0) * total
        "$p% of $total = $res"
      } else if (expr.contains("+")) {
        val nums = expr.split("+").map { it.trim().toDouble() }
        "${nums[0]} + ${nums[1]} = ${nums[0] + nums[1]}"
      } else if (expr.contains("-")) {
        val nums = expr.split("-").map { it.trim().toDouble() }
        "${nums[0]} - ${nums[1]} = ${nums[0] - nums[1]}"
      } else if (expr.contains("*") || expr.contains("times") || expr.contains("x")) {
        val parts = expr.split(Regex("\\*|times|x"))
        val nums = parts.map { it.trim().toDouble() }
        "${nums[0]} × ${nums[1]} = ${nums[0] * nums[1]}"
      } else if (expr.contains("/")) {
        val nums = expr.split("/").map { it.trim().toDouble() }
        if (nums[1] != 0.0) "${nums[0]} / ${nums[1]} = ${nums[0] / nums[1]}" else "Division by zero undefined"
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun generateJarvisResponse(query: String): String {
    val q = query.lowercase()
    return when {
      q.contains("who are you") || q.contains("what are you") ->
        "I am J.A.R.V.I.S.—Just A Rather Very Intelligent System. I am configured here as your personal DIY engineering console, managing home automation, device telemetry, and Stark-grade protocols."

      q.contains("joke") ->
        "An engineer, a physicist, and a computer programmer are in a car driving downhill when the brakes fail. The engineer suggests retrofitting an auxiliary hydraulic piston. The physicist calculates the friction of the crash barrier. The programmer says: 'Let's push it back to the top and see if it happens again.' Quite amusing, wouldn't you say, sir?"

      q.contains("thank") ->
        "At your service as always, sir. It is an absolute privilege."

      q.contains("hello") || q.contains("hi") || q.contains("hey jarvis") ->
        "Greetings, sir. All telemetry channels are synchronized and awaiting your command."

      q.contains("time") ->
        "The current local system time is verified and synchronized with the master atomic clock, sir."

      q.contains("diy") || q.contains("esp32") || q.contains("arduino") || q.contains("raspberry pi") ->
        "Our DIY Hardware Grid allows you to bind any ESP32, Arduino, or Raspberry Pi via HTTP/REST webhooks, sir. You can also simulate GPIO logic levels directly in the DIY Hub tab."

      q.contains("help") ->
        "I can control device hardware (such as your flashlight), execute automated protocols (like Clean Slate or Lockdown), trigger your DIY IoT network, test GPIO logic levels, and monitor real-time battery and RAM telemetry. Simply speak or type your request, sir."

      else ->
        "Understood, sir. I have logged your query: '$query'. Telemetry and automation nodes remain synchronized and ready for further instructions."
    }
  }

  override fun onCleared() {
    super.onCleared()
    speechManager.destroy()
  }
}
