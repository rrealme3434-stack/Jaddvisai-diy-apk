package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.JarvisViewModel
import com.example.audio.JarvisSoundFx
import com.example.ui.components.SciFiButton
import com.example.ui.components.SciFiCard
import com.example.ui.components.TelemetryMiniMeter
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArcGold
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCyan
import com.example.ui.theme.SystemOnlineGreen

@Composable
fun TelemetryScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val telemetry by viewModel.telemetry.collectAsState()
  val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header with Refresh
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "SYSTEM TELEMETRY",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Text(
            text = "LIVE HARDWARE TELEMETRY STREAM",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = HudTextMuted
          )
        }

        SciFiButton(
          text = "REFRESH",
          onClick = {
            viewModel.telemetryManager.refreshTelemetry()
            viewModel.telemetryManager.triggerHapticFeedback(50)
            JarvisSoundFx.playClick()
          },
          icon = Icons.Default.Refresh,
          isPrimary = false,
          testTag = "refresh_telemetry_button"
        )
      }
    }

    // 2. Arc Reactor Output
    item {
      SciFiCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = StarkCyan,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "ARC REACTOR MK-VII CORE",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = StarkCyan
              )
            }
            Text(
              text = "CONTAINMENT: 100%",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = SystemOnlineGreen
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          TelemetryMiniMeter(
            label = "CORE ENERGY CAPACITY",
            valueText = "${telemetry.arcReactorOutput}%",
            percent = telemetry.arcReactorOutput.toInt(),
            color = StarkCyan
          )
        }
      }
    }

    // 3. Power Matrix & Battery
    item {
      TelemetrySectionCard(
        title = "POWER MATRIX",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = if (telemetry.batteryPercent > 20) StarkCyan else AlertRed
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TelemetryMiniMeter(
            label = "BATTERY LEVEL",
            valueText = "${telemetry.batteryPercent}%",
            percent = telemetry.batteryPercent,
            color = if (telemetry.batteryPercent > 20) StarkCyan else AlertRed
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TelemetryItem("STATE", if (telemetry.isCharging) "CHARGING" else "DISCHARGING")
            TelemetryItem("SOURCE", telemetry.chargeSource)
            TelemetryItem("TEMP", "${telemetry.batteryTempCelsius}°C")
          }
        }
      }
    }

    // 4. Memory / RAM Subsystem
    item {
      TelemetrySectionCard(
        title = "MEMORY & COMPUTE SUBSYSTEM",
        icon = Icons.Default.Memory,
        accentColor = ArcGold
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TelemetryMiniMeter(
            label = "RAM ALLOCATION",
            valueText = "${telemetry.usedRamMb}MB / ${telemetry.totalRamMb}MB (${telemetry.ramUsagePercent}%)",
            percent = telemetry.ramUsagePercent,
            color = ArcGold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TelemetryItem("USED RAM", "${telemetry.usedRamMb} MB")
            TelemetryItem("FREE RAM", "${telemetry.totalRamMb - telemetry.usedRamMb} MB")
            TelemetryItem("STATUS", "NOMINAL")
          }
        }
      }
    }

    // 5. Internal Storage Breakdown
    item {
      TelemetrySectionCard(
        title = "STORAGE MATRIX",
        icon = Icons.Default.SdStorage,
        accentColor = SystemOnlineGreen
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TelemetryMiniMeter(
            label = "STORAGE OCCUPANCY",
            valueText = "${telemetry.storageUsagePercent}%",
            percent = telemetry.storageUsagePercent,
            color = SystemOnlineGreen
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TelemetryItem("TOTAL DISK", telemetry.totalStorage)
            TelemetryItem("AVAILABLE", telemetry.freeStorage)
            TelemetryItem("DRIVE", "EMMC/UFS FLASH")
          }
        }
      }
    }

    // 6. Network Telemetry
    item {
      TelemetrySectionCard(
        title = "NETWORK & COMMUNICATION BUS",
        icon = Icons.Default.NetworkCheck,
        accentColor = StarkCyan
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TelemetryItem("CARRIER / LINK", telemetry.networkType)
            TelemetryItem("STATUS", if (telemetry.isOnline) "ONLINE" else "DISCONNECTED")
          }
        }
      }
    }

    // 7. Tactical Hardware Test Controls
    item {
      SciFiCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Text(
            text = "HARDWARE TEST BENCH",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SciFiButton(
              text = if (isFlashlightOn) "TORCH [ON]" else "TORCH [OFF]",
              onClick = { viewModel.toggleFlashlight() },
              icon = Icons.Default.FlashlightOn,
              isPrimary = isFlashlightOn,
              modifier = Modifier.weight(1f),
              testTag = "telemetry_torch_toggle"
            )

            SciFiButton(
              text = "HAPTIC PULSE",
              onClick = {
                viewModel.telemetryManager.triggerHapticFeedback(150)
                JarvisSoundFx.playClick()
              },
              icon = Icons.Default.Vibration,
              isPrimary = false,
              modifier = Modifier.weight(1f),
              testTag = "test_haptic_button"
            )

            SciFiButton(
              text = "SOUND FX",
              onClick = { JarvisSoundFx.playSystemOnline() },
              icon = Icons.Default.VolumeUp,
              isPrimary = false,
              modifier = Modifier.weight(1f),
              testTag = "test_sound_fx_button"
            )
          }
        }
      }
    }

    // 8. Device Hardware Information
    item {
      SciFiCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "DEVICE ARCHITECTURE",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          TelemetryRow("CHASSIS / MODEL", telemetry.deviceModel)
          TelemetryRow("OS PLATFORM", telemetry.osVersion)
          TelemetryRow("SYSTEM UPTIME", telemetry.deviceUptime)
          TelemetryRow("AI ENGINE", "J.A.R.V.I.S. QUANTUM V4.2")
        }
      }
    }
  }
}

@Composable
fun TelemetrySectionCard(
  title: String,
  icon: ImageVector,
  accentColor: androidx.compose.ui.graphics.Color,
  content: @Composable () -> Unit
) {
  SciFiCard(modifier = Modifier.fillMaxWidth(), borderColor = CyberBorder) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = accentColor,
          letterSpacing = 1.sp
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      content()
    }
  }
}

@Composable
fun TelemetryItem(label: String, value: String) {
  Column {
    Text(
      text = label,
      fontFamily = OrbitronFontFamily,
      fontSize = 9.sp,
      color = HudTextMuted
    )
    Text(
      text = value,
      fontFamily = ShareTechMonoFontFamily,
      fontSize = 11.sp,
      color = HudTextPrimary,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun TelemetryRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontFamily = OrbitronFontFamily,
      fontSize = 10.sp,
      color = HudTextMuted
    )
    Text(
      text = value,
      fontFamily = ShareTechMonoFontFamily,
      fontSize = 11.sp,
      color = HudTextSecondary
    )
  }
}
