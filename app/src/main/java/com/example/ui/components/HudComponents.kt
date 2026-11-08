package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.JarvisSoundFx
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

@Composable
fun HudTopBar(
  batteryPct: Int,
  isCharging: Boolean,
  soundFxEnabled: Boolean,
  onToggleSoundFx: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentTime by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    while (true) {
      currentTime = formatter.format(Date())
      delay(1000)
    }
  }

  Surface(
    color = CyberDark.copy(alpha = 0.95f),
    border = BorderStroke(1.dp, CyberBorder),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: JARVIS Brand & Online Beacon
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(SystemOnlineGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "J.A.R.V.I.S.",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Text(
            text = "DIY SYSTEM V4.2",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 9.sp,
            color = HudTextMuted
          )
        }
      }

      // Center: HUD Real-time Clock
      Text(
        text = currentTime,
        fontFamily = OrbitronFontFamily,
        fontSize = 13.sp,
        color = ArcGold,
        letterSpacing = 1.5.sp
      )

      // Right: Battery & Sound Toggle
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Battery Indicator Pill
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberBorder, CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Icon(
            imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
            contentDescription = "Battery",
            tint = if (batteryPct > 20) StarkCyan else AlertRed,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "$batteryPct%",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = HudTextPrimary
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            JarvisSoundFx.playClick()
            onToggleSoundFx()
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("sound_fx_toggle")
        ) {
          Icon(
            imageVector = if (soundFxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = "Toggle Audio FX",
            tint = if (soundFxEnabled) StarkCyan else HudTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun SciFiCard(
  modifier: Modifier = Modifier,
  borderColor: Color = CyberBorder,
  cornerCut: Float = 12f,
  content: @Composable () -> Unit
) {
  val shape = CutCornerShape(topStart = cornerCut.dp, bottomEnd = cornerCut.dp)
  Box(
    modifier = modifier
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            CyberSurface,
            CyberDark
          )
        )
      )
      .border(BorderStroke(1.dp, borderColor), shape)
      .padding(14.dp)
  ) {
    content()
  }
}

@Composable
fun SciFiButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  isPrimary: Boolean = true,
  isAlert: Boolean = false,
  testTag: String = "scifi_button"
) {
  val accentColor = when {
    isAlert -> AlertRed
    isPrimary -> StarkCyan
    else -> ArcGold
  }

  val shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

  OutlinedButton(
    onClick = {
      JarvisSoundFx.playClick()
      onClick()
    },
    modifier = modifier.testTag(testTag),
    shape = shape,
    border = BorderStroke(1.dp, accentColor),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = accentColor.copy(alpha = 0.12f),
      contentColor = accentColor
    ),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = text,
        fontFamily = OrbitronFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp
      )
    }
  }
}

@Composable
fun TelemetryMiniMeter(
  label: String,
  valueText: String,
  percent: Int,
  color: Color = StarkCyan,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        fontFamily = ShareTechMonoFontFamily,
        fontSize = 11.sp,
        color = HudTextSecondary
      )
      Text(
        text = valueText,
        fontFamily = OrbitronFontFamily,
        fontSize = 11.sp,
        color = color
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { (percent.coerceIn(0, 100) / 100f) },
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp)),
      color = color,
      trackColor = CyberSurfaceVariant
    )
  }
}

@Composable
fun LiveWaveformRow(
  isActive: Boolean,
  audioRms: Float,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * Math.PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "phase"
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(28.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.GraphicEq,
      contentDescription = "Waveform",
      tint = if (isActive) StarkCyanGlow else HudTextMuted,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))

    val bars = 16
    for (i in 0 until bars) {
      val barHeight = if (isActive) {
        val wave = ((sin(phase + i * 0.45) + 1.0) / 2.0).toFloat()
        (8.dp + ((audioRms * 14f) + (wave * 10f)).dp).coerceIn(4.dp, 24.dp)
      } else {
        4.dp
      }
      Box(
        modifier = Modifier
          .padding(horizontal = 2.dp)
          .width(3.dp)
          .height(barHeight)
          .clip(RoundedCornerShape(1.5.dp))
          .background(if (isActive) StarkCyan else CyberBorder)
      )
    }
  }
}
