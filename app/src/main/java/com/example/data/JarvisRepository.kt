package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

class JarvisRepository {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(4, TimeUnit.SECONDS)
    .writeTimeout(4, TimeUnit.SECONDS)
    .build()

  private val _messages = MutableStateFlow<List<JarvisMessage>>(emptyList())
  val messages: StateFlow<List<JarvisMessage>> = _messages.asStateFlow()

  private val _diyDevices = MutableStateFlow<List<DiyDevice>>(createDefaultDevices())
  val diyDevices: StateFlow<List<DiyDevice>> = _diyDevices.asStateFlow()

  private val _protocols = MutableStateFlow<List<ProtocolRoutine>>(createDefaultProtocols())
  val protocols: StateFlow<List<ProtocolRoutine>> = _protocols.asStateFlow()

  private val _pinouts = MutableStateFlow<List<DiyPinout>>(createDefaultPinouts())
  val pinouts: StateFlow<List<DiyPinout>> = _pinouts.asStateFlow()

  init {
    addMessage(
      JarvisMessage(
        id = UUID.randomUUID().toString(),
        text = "Good day, sir. J.A.R.V.I.S. core initialization complete. Arc Reactor operational at 99.4% capacity. All local telemetry and DIY hardware matrices are online. How may I be of assistance today?",
        isFromUser = false,
        commandType = CommandType.SYSTEM_CHECK,
        suggestedActions = listOf(
          "Run System Diagnostics",
          "Initiate Protocol Clean Slate",
          "Toggle ESP32 Lab Lights",
          "Check Battery & Telemetry"
        )
      )
    )
  }

  fun addMessage(message: JarvisMessage) {
    _messages.value = _messages.value + message
  }

  fun clearMessages() {
    _messages.value = listOf(
      JarvisMessage(
        id = UUID.randomUUID().toString(),
        text = "Terminal cleared, sir. Standing by for instructions.",
        isFromUser = false
      )
    )
  }

  // DIY Devices Management
  private fun createDefaultDevices(): List<DiyDevice> {
    return listOf(
      DiyDevice(
        id = "esp32_lights",
        name = "ESP32 Lab Overhead LEDs",
        deviceType = DiyDeviceType.ESP32,
        ipAddressOrUrl = "http://192.168.1.101/api/relay/1",
        httpMethod = "POST",
        payload = "{\"state\": \"toggle\"}",
        isOnline = true,
        state = true,
        sensorReading = "12V | 2.1A Draw"
      ),
      DiyDevice(
        id = "pi_camera",
        name = "Raspberry Pi Security Gate",
        deviceType = DiyDeviceType.RASPBERRY_PI,
        ipAddressOrUrl = "http://192.168.1.105:5000/trigger_servo",
        httpMethod = "POST",
        payload = "{\"lock\": false}",
        isOnline = true,
        state = false,
        sensorReading = "Cam: Active 1080p"
      ),
      DiyDevice(
        id = "arduino_cool",
        name = "Arduino Reactor Cooling Fan",
        deviceType = DiyDeviceType.ARDUINO,
        ipAddressOrUrl = "http://192.168.1.120/fan",
        httpMethod = "GET",
        isOnline = true,
        state = true,
        sensorReading = "PWM 80% | 2450 RPM"
      ),
      DiyDevice(
        id = "sensor_dht22",
        name = "Living Room Ambient Node",
        deviceType = DiyDeviceType.SENSOR_NODE,
        ipAddressOrUrl = "http://192.168.1.140/metrics",
        httpMethod = "GET",
        isOnline = true,
        state = true,
        sensorReading = "23.8°C | 45% Humidity"
      )
    )
  }

  fun addDiyDevice(device: DiyDevice) {
    _diyDevices.value = _diyDevices.value + device
  }

  fun removeDiyDevice(deviceId: String) {
    _diyDevices.value = _diyDevices.value.filter { it.id != deviceId }
  }

  suspend fun toggleDevice(deviceId: String): Pair<Boolean, String> {
    val current = _diyDevices.value.find { it.id == deviceId } ?: return false to "Device not found"
    val newState = !current.state

    // Update state locally first
    _diyDevices.value = _diyDevices.value.map {
      if (it.id == deviceId) it.copy(state = newState, lastPingMs = System.currentTimeMillis()) else it
    }

    // Attempt real HTTP network request asynchronously if reachable
    val result = withContext(Dispatchers.IO) {
      try {
        val requestBuilder = Request.Builder().url(current.ipAddressOrUrl)
        if (current.httpMethod.equals("POST", ignoreCase = true)) {
          val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
          val body = (if (current.payload.isNotBlank()) current.payload else "{\"state\": $newState}")
            .toRequestBody(mediaType)
          requestBuilder.post(body)
        } else {
          requestBuilder.get()
        }
        val response = httpClient.newCall(requestBuilder.build()).execute()
        val success = response.isSuccessful
        response.close()
        if (success) {
          true to "HTTP 200 OK sent to ${current.name}"
        } else {
          true to "Local state toggled. Remote endpoint returned HTTP ${response.code}"
        }
      } catch (e: Exception) {
        // Normal in DIY environments where device might be on another subnet or local simulation
        true to "Local switch state toggled to ${if (newState) "ON" else "OFF"} (device offline: ${e.javaClass.simpleName})"
      }
    }
    return result
  }

  // Pinout Workbench
  private fun createDefaultPinouts(): List<DiyPinout> {
    return listOf(
      DiyPinout(pinNumber = 2, pinName = "STATUS_LED", mode = "OUTPUT_DIGITAL", digitalState = true, analogValue = 3.3f),
      DiyPinout(pinNumber = 4, pinName = "RELAY_AC_MAINS", mode = "OUTPUT_DIGITAL", digitalState = false, analogValue = 0f),
      DiyPinout(pinNumber = 5, pinName = "FAN_PWM_SPEED", mode = "PWM_OUTPUT", digitalState = true, analogValue = 2.6f),
      DiyPinout(pinNumber = 12, pinName = "BUZZER_ALARM", mode = "OUTPUT_DIGITAL", digitalState = false, analogValue = 0f),
      DiyPinout(pinNumber = 14, pinName = "SERVO_POSITION", mode = "PWM_OUTPUT", digitalState = true, analogValue = 1.65f),
      DiyPinout(pinNumber = 21, pinName = "PIR_MOTION_DETECTOR", mode = "DIGITAL_INPUT", digitalState = true, analogValue = 3.3f),
      DiyPinout(pinNumber = 34, pinName = "LDR_LIGHT_SENSOR", mode = "ANALOG_INPUT", digitalState = true, analogValue = 1.95f),
      DiyPinout(pinNumber = 36, pinName = "BATTERY_VOLTAGE_ADC", mode = "ANALOG_INPUT", digitalState = true, analogValue = 2.85f)
    )
  }

  fun updatePinDigitalState(pinNumber: Int, newState: Boolean) {
    _pinouts.value = _pinouts.value.map {
      if (it.pinNumber == pinNumber) it.copy(digitalState = newState, analogValue = if (newState) 3.3f else 0f) else it
    }
  }

  fun updatePinAnalogValue(pinNumber: Int, newValue: Float) {
    _pinouts.value = _pinouts.value.map {
      if (it.pinNumber == pinNumber) it.copy(analogValue = newValue, digitalState = newValue > 0.1f) else it
    }
  }

  // Protocols Management
  private fun createDefaultProtocols(): List<ProtocolRoutine> {
    return listOf(
      ProtocolRoutine(
        id = "clean_slate",
        title = "Protocol Clean Slate",
        codeName = "MK-42",
        description = "Re-align all subsystem matrices, reset DIY actuators, and verify thermal integrity.",
        voiceTrigger = "clean slate",
        actions = listOf(
          ProtocolAction(ActionType.SOUND_FX, "REACTOR_CHARGE"),
          ProtocolAction(ActionType.VIBRATE, "120"),
          ProtocolAction(ActionType.SPEAK, "Initiating Protocol Clean Slate. Resetting all DIY telemetry and re-engaging primary reactor containment. All systems nominal, sir.")
        )
      ),
      ProtocolRoutine(
        id = "lockdown",
        title = "Protocol Lockdown",
        codeName = "SENTINEL-1",
        description = "Secure the perimeter, flash tactical strobe, lock Pi security gate, and arm sensors.",
        voiceTrigger = "lockdown",
        actions = listOf(
          ProtocolAction(ActionType.SOUND_FX, "ALERT"),
          ProtocolAction(ActionType.FLASHLIGHT_STROBE, "3"),
          ProtocolAction(ActionType.VIBRATE, "250"),
          ProtocolAction(ActionType.SPEAK, "Perimeter lockdown active. Raspberry Pi gate engaged. Intrusion sensors armed to maximum sensitivity, sir.")
        )
      ),
      ProtocolRoutine(
        id = "night_owl",
        title = "Protocol Night Owl",
        codeName = "NOCTURNE",
        description = "Dims lab LED illumination, activates low-power telemetry, and readies sleep monitors.",
        voiceTrigger = "good night",
        actions = listOf(
          ProtocolAction(ActionType.SOUND_FX, "ACKNOWLEDGE"),
          ProtocolAction(ActionType.SPEAK, "Good night, sir. Disengaging high-voltage relays and dimming lab illumination. I shall monitor the telemetry while you rest.")
        )
      ),
      ProtocolRoutine(
        id = "workspace_focus",
        title = "Protocol Workspace Focus",
        codeName = "EINSTEIN-9",
        description = "Engages overhead workbench illumination, spins up cooling turbines, and silences notifications.",
        voiceTrigger = "workspace focus",
        actions = listOf(
          ProtocolAction(ActionType.SOUND_FX, "ACKNOWLEDGE"),
          ProtocolAction(ActionType.SPEAK, "Workspace Focus engaged. Activating workbench illumination and spooling DIY fan matrix to optimum airflow.")
        )
      ),
      ProtocolRoutine(
        id = "security_sweep",
        title = "Protocol Security Sweep",
        codeName = "ARGUS-4",
        description = "Pings all network endpoints, queries DIY IoT sensor nodes, and audits battery levels.",
        voiceTrigger = "security sweep",
        actions = listOf(
          ProtocolAction(ActionType.SOUND_FX, "REACTOR_CHARGE"),
          ProtocolAction(ActionType.SPEAK, "Security sweep completed. All 4 DIY nodes responded with nominal latency. Arc containment holding steady.")
        )
      )
    )
  }

  fun addProtocol(routine: ProtocolRoutine) {
    _protocols.value = _protocols.value + routine
  }

  fun markProtocolExecuted(protocolId: String) {
    _protocols.value = _protocols.value.map {
      if (it.id == protocolId) it.copy(lastExecuted = System.currentTimeMillis()) else it
    }
  }
}
