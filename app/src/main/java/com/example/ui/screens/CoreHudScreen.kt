package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HudTab
import com.example.JarvisViewModel
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.LiveWaveformRow
import com.example.ui.components.SciFiButton
import com.example.ui.components.SciFiCard
import com.example.ui.components.TelemetryMiniMeter
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArcGold
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCyan
import com.example.ui.theme.StarkCyanGlow
import com.example.ui.theme.SystemOnlineGreen

@Composable
fun CoreHudScreen(
  viewModel: JarvisViewModel,
  onRequestAudioPermission: () -> Unit,
  modifier: Modifier = Modifier
) {
  val reactorState by viewModel.reactorState.collectAsState()
  val telemetry by viewModel.telemetry.collectAsState()
  val statusText by viewModel.statusText.collectAsState()
  val protocols by viewModel.protocols.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()

  val latestMessage = messages.lastOrNull()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Arc Reactor Hero Core
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Holographic Arc Reactor Visualizer
        ArcReactorVisualizer(
          state = reactorState,
          onTap = { viewModel.triggerReactorPulse() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Soundwave / Audio Level Row
        LiveWaveformRow(
          isActive = reactorState.isListening || reactorState.isSpeaking,
          audioRms = reactorState.audioRms
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Voice Command Action Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(if (reactorState.isListening) ArcGold.copy(alpha = 0.25f) else StarkCyan.copy(alpha = 0.15f))
              .border(
                2.dp,
                if (reactorState.isListening) ArcGold else StarkCyan,
                CircleShape
              )
              .clickable {
                if (reactorState.isListening) {
                  viewModel.stopVoiceListening()
                } else {
                  onRequestAudioPermission()
                  viewModel.startVoiceListening()
                }
              }
              .testTag("voice_listen_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (reactorState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = "Voice Listen",
              tint = if (reactorState.isListening) ArcGold else StarkCyan,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = if (reactorState.isListening) "LISTENING TO VOICE..." else "VOICE INTERFACE READY",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (reactorState.isListening) ArcGold else StarkCyan
            )
            Text(
              text = if (reactorState.isListening) "Speak your command now, sir" else "Tap reactor or mic to speak",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 11.sp,
              color = HudTextSecondary
            )
          }
        }
      }
    }

    // 2. Telemetry Quick Bar
    item {
      SciFiCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (reactorState.isAlert) AlertRed else CyberBorder
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CORE TELEMETRY",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp,
              color = StarkCyan,
              letterSpacing = 1.sp
            )
            Text(
              text = telemetry.deviceModel,
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = HudTextMuted
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            TelemetryMiniMeter(
              label = "BATTERY",
              valueText = "${telemetry.batteryPercent}%",
              percent = telemetry.batteryPercent,
              color = if (telemetry.batteryPercent > 20) StarkCyan else AlertRed,
              modifier = Modifier.weight(1f)
            )
            TelemetryMiniMeter(
              label = "RAM LOAD",
              valueText = "${telemetry.ramUsagePercent}%",
              percent = telemetry.ramUsagePercent,
              color = if (telemetry.ramUsagePercent < 80) ArcGold else AlertRed,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "NET: ${telemetry.networkType}",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = HudTextSecondary
            )
            Text(
              text = "THERMAL: ${String.format("%.1f", telemetry.coreTemperature)}°C",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = SystemOnlineGreen
            )
          }
        }
      }
    }

    // 3. Quick Protocol Triggers
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "QUICK PROTOCOLS",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = HudTextPrimary,
            letterSpacing = 1.sp
          )
          Text(
            text = "SEE ALL",
            fontFamily = OrbitronFontFamily,
            fontSize = 10.sp,
            color = StarkCyan,
            modifier = Modifier
              .clickable { viewModel.setActiveTab(HudTab.PROTOCOLS) }
              .padding(4.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(protocols.take(4)) { protocol ->
            val isLockdown = protocol.id == "lockdown"
            SciFiButton(
              text = protocol.codeName,
              onClick = { viewModel.executeProtocol(protocol) },
              isPrimary = !isLockdown,
              isAlert = isLockdown,
              icon = if (isLockdown) Icons.Default.Security else Icons.Default.PlayArrow,
              testTag = "quick_protocol_${protocol.id}"
            )
          }
        }
      }
    }

    // 4. Tactical Hardware Controls
    item {
      SciFiCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Text(
            text = "TACTICAL HARDWARE CONTROLS",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            SciFiButton(
              text = if (isFlashlightOn) "TORCH [ON]" else "TORCH [OFF]",
              onClick = { viewModel.toggleFlashlight() },
              icon = Icons.Default.FlashlightOn,
              isPrimary = isFlashlightOn,
              modifier = Modifier.weight(1f),
              testTag = "hud_toggle_torch"
            )
            SciFiButton(
              text = "TERMINAL",
              onClick = { viewModel.setActiveTab(HudTab.TERMINAL) },
              icon = Icons.Default.Terminal,
              isPrimary = false,
              modifier = Modifier.weight(1f),
              testTag = "hud_open_terminal"
            )
          }
        }
      }
    }

    // 5. Recent Activity Terminal Log
    item {
      SciFiCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.setActiveTab(HudTab.TERMINAL) }
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "LATEST LOG ENTRY",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              color = ArcGold
            )
            Text(
              text = "TAP TO EXPAND",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 9.sp,
              color = HudTextMuted
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = latestMessage?.text ?: "All systems standing by.",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            color = HudTextPrimary,
            lineHeight = 17.sp,
            maxLines = 3
          )
        }
      }
    }
  }
}
