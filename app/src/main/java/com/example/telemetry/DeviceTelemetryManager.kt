package com.example.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.SystemTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DeviceTelemetryManager(private val context: Context) {

  private val _telemetry = MutableStateFlow(fetchCurrentTelemetry())
  val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

  private val _isFlashlightOn = MutableStateFlow(false)
  val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

  private var cameraManager: CameraManager? = null
  private var torchCameraId: String? = null

  init {
    try {
      cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      cameraManager?.let { cm ->
        for (id in cm.cameraIdList) {
          val chars = cm.getCameraCharacteristics(id)
          val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
          val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
          if (flashAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
            torchCameraId = id
            break
          }
        }
        if (torchCameraId == null && cm.cameraIdList.isNotEmpty()) {
          torchCameraId = cm.cameraIdList[0]
        }
      }
    } catch (_: Exception) {}
  }

  fun refreshTelemetry(): SystemTelemetry {
    val updated = fetchCurrentTelemetry()
    _telemetry.value = updated
    return updated
  }

  private fun fetchCurrentTelemetry(): SystemTelemetry {
    // Battery
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 85
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
    val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
    val chargeSource = when (plugged) {
      BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
      BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
      BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
      else -> if (isCharging) "Charging" else "On Battery"
    }
    val rawTemp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 280
    val batteryTempC = rawTemp / 10f

    // Memory
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager?.getMemoryInfo(memInfo)
    val totalRamMb = (memInfo.totalMem / (1024 * 1024)).toInt()
    val availRamMb = (memInfo.availMem / (1024 * 1024)).toInt()
    val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)
    val ramUsagePercent = if (totalRamMb > 0) ((usedRamMb.toFloat() / totalRamMb) * 100).toInt() else 45

    // Storage
    val dataDir: File = Environment.getDataDirectory()
    val statFs = StatFs(dataDir.path)
    val totalBytes = statFs.totalBytes
    val availableBytes = statFs.availableBytes
    val totalStorageGb = String.format("%.1f GB", totalBytes.toDouble() / (1024 * 1024 * 1024))
    val freeStorageGb = String.format("%.1f GB", availableBytes.toDouble() / (1024 * 1024 * 1024))
    val storagePercent = if (totalBytes > 0) (((totalBytes - availableBytes).toDouble() / totalBytes) * 100).toInt() else 60

    // Network
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNetwork = connManager?.activeNetwork
    val caps = connManager?.getNetworkCapabilities(activeNetwork)
    val networkType = when {
      caps == null -> "Offline"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (High Speed)"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/LTE"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet LAN"
      else -> "Connected"
    }

    val uptimeHours = SystemClock.elapsedRealtime() / (1000 * 60 * 60)
    val uptimeMinutes = (SystemClock.elapsedRealtime() / (1000 * 60)) % 60
    val uptimeStr = "${uptimeHours}h ${uptimeMinutes}m"

    val model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    return SystemTelemetry(
      batteryPercent = batteryPct,
      isCharging = isCharging,
      chargeSource = chargeSource,
      batteryTempCelsius = batteryTempC,
      totalRamMb = totalRamMb,
      usedRamMb = usedRamMb,
      ramUsagePercent = ramUsagePercent,
      totalStorage = totalStorageGb,
      freeStorage = freeStorageGb,
      storageUsagePercent = storagePercent,
      networkType = networkType,
      isOnline = caps != null,
      deviceModel = model,
      osVersion = osVersion,
      deviceUptime = uptimeStr,
      coreTemperature = (34.0f + (ramUsagePercent * 0.15f)),
      arcReactorOutput = 98.4f
    )
  }

  fun toggleFlashlight(forceState: Boolean? = null): Boolean {
    val cm = cameraManager ?: return false
    val id = torchCameraId ?: return false
    val target = forceState ?: !_isFlashlightOn.value
    try {
      cm.setTorchMode(id, target)
      _isFlashlightOn.value = target
      return true
    } catch (_: Exception) {
      return false
    }
  }

  fun triggerHapticFeedback(patternMs: Long = 60) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(
          VibrationEffect.createOneShot(patternMs, VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(patternMs, VibrationEffect.DEFAULT_AMPLITUDE))
      }
    } catch (_: Exception) {}
  }
}
