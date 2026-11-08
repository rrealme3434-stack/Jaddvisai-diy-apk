package com.example.data

enum class CommandType {
  SYSTEM_CHECK,
  PROTOCOL,
  DIY_IOT,
  FLASHLIGHT,
  CALCULATION,
  KNOWLEDGE,
  GENERAL
}

enum class DiyDeviceType {
  ESP32,
  RASPBERRY_PI,
  ARDUINO,
  SMART_RELAY,
  SENSOR_NODE
}

data class JarvisMessage(
  val id: String,
  val text: String,
  val isFromUser: Boolean,
  val timestamp: Long = System.currentTimeMillis(),
  val commandType: CommandType = CommandType.GENERAL,
  val actionResult: String? = null,
  val suggestedActions: List<String> = emptyList()
)

data class DiyDevice(
  val id: String,
  val name: String,
  val deviceType: DiyDeviceType,
  val ipAddressOrUrl: String,
  val httpMethod: String = "GET",
  val payload: String = "",
  val isOnline: Boolean = true,
  val state: Boolean = false,
  val lastPingMs: Long = System.currentTimeMillis(),
  val sensorReading: String? = null
)

data class DiyPinout(
  val pinNumber: Int,
  val pinName: String,
  val mode: String, // "OUTPUT_DIGITAL", "PWM_OUTPUT", "ANALOG_INPUT"
  val digitalState: Boolean = false,
  val analogValue: Float = 0f // 0 to 3.3V or PWM %
)

enum class ActionType {
  SPEAK,
  FLASHLIGHT_STROBE,
  TRIGGER_DEVICE,
  SOUND_FX,
  VIBRATE
}

data class ProtocolAction(
  val actionType: ActionType,
  val parameters: String
)

data class ProtocolRoutine(
  val id: String,
  val title: String,
  val codeName: String,
  val description: String,
  val voiceTrigger: String,
  val actions: List<ProtocolAction>,
  val isArmed: Boolean = true,
  val lastExecuted: Long? = null
)

data class SystemTelemetry(
  val batteryPercent: Int = 92,
  val isCharging: Boolean = false,
  val chargeSource: String = "On Battery",
  val batteryTempCelsius: Float = 29.5f,
  val totalRamMb: Int = 8192,
  val usedRamMb: Int = 3650,
  val ramUsagePercent: Int = 45,
  val totalStorage: String = "128.0 GB",
  val freeStorage: String = "64.2 GB",
  val storageUsagePercent: Int = 50,
  val networkType: String = "Wi-Fi (High Speed)",
  val isOnline: Boolean = true,
  val deviceModel: String = "Stark Mark VII",
  val osVersion: String = "Android 15 (JARVIS OS v4.2)",
  val deviceUptime: String = "14h 22m",
  val coreTemperature: Float = 36.8f,
  val arcReactorOutput: Float = 99.4f
)

data class ArcReactorState(
  val status: String = "ONLINE",
  val energyPercent: Float = 99.4f,
  val isListening: Boolean = false,
  val isSpeaking: Boolean = false,
  val isAlert: Boolean = false,
  val audioRms: Float = 0f
)
