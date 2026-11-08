package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ArcReactorState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedGlow
import com.example.ui.theme.ArcGold
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCyan
import com.example.ui.theme.StarkCyanGlow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
  state: ArcReactorState,
  modifier: Modifier = Modifier,
  onTap: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

  val outerRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 22000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "outer_rot"
  )

  val middleRotation by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 14000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "mid_rot"
  )

  val innerRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 7000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "inner_rot"
  )

  val pulseFactor by infiniteTransition.animateFloat(
    initialValue = 0.88f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (state.isListening || state.isSpeaking) 600 else 1800,
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  val primaryColor = when {
    state.isAlert -> AlertRed
    state.isListening -> ArcGold
    state.isSpeaking -> StarkCyanGlow
    else -> StarkCyan
  }

  val secondaryColor = when {
    state.isAlert -> AlertRedGlow
    state.isListening -> ArcGold
    else -> ArcGold
  }

  Box(
    modifier = modifier
      .size(260.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onTap
      )
      .testTag("arc_reactor_core"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = size.minDimension / 2f - 12f

      // 1. Ambient Glow Field behind the core
      val glowRadius = maxRadius * 0.95f * pulseFactor
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            primaryColor.copy(alpha = if (state.isSpeaking || state.isListening) 0.35f else 0.18f),
            primaryColor.copy(alpha = 0.05f),
            Color.Transparent
          ),
          center = center,
          radius = glowRadius
        ),
        radius = glowRadius,
        center = center
      )

      // 2. Outermost static measurement circle
      drawCircle(
        color = CyberBorder.copy(alpha = 0.6f),
        radius = maxRadius,
        center = center,
        style = Stroke(width = 1.5f)
      )

      // 3. Rotating outer segmented track with ticks
      rotate(outerRotation, pivot = center) {
        val outerRadius = maxRadius * 0.92f
        drawCircle(
          color = primaryColor.copy(alpha = 0.7f),
          radius = outerRadius,
          center = center,
          style = Stroke(
            width = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 10f, 15f), 0f)
          )
        )

        // Cardinal nodes (top, bottom, left, right)
        for (i in 0 until 12) {
          val angle = (i * 30.0) * Math.PI / 180.0
          val nodeDist = outerRadius
          val nx = (center.x + nodeDist * cos(angle)).toFloat()
          val ny = (center.y + nodeDist * sin(angle)).toFloat()
          val nodeSize = if (i % 3 == 0) 4f else 2f
          drawCircle(
            color = if (i % 3 == 0) secondaryColor else primaryColor,
            radius = nodeSize,
            center = Offset(nx, ny)
          )
        }
      }

      // 4. Middle counter-rotating power ring
      rotate(middleRotation, pivot = center) {
        val midRadius = maxRadius * 0.76f
        // 8 distinct Arc Reactor power coil segments
        for (i in 0 until 8) {
          val startAngle = i * 45f + 5f
          val sweepAngle = 35f
          drawArc(
            color = if (i % 2 == 0) primaryColor else secondaryColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - midRadius, center.y - midRadius),
            size = Size(midRadius * 2, midRadius * 2),
            style = Stroke(width = 7f, cap = StrokeCap.Round)
          )
        }
      }

      // 5. High-speed inner gear ring with dashes
      rotate(innerRotation, pivot = center) {
        val innerTrackRadius = maxRadius * 0.58f
        drawCircle(
          color = primaryColor.copy(alpha = 0.85f),
          radius = innerTrackRadius,
          center = center,
          style = Stroke(
            width = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
          )
        )
      }

      // 6. Soundwave / Audio reactive ring when listening or speaking
      if (state.isListening || state.isSpeaking) {
        val waveRadius = maxRadius * 0.44f
        val numBars = 24
        val rmsBoost = (state.audioRms * 18f).coerceAtLeast(3f)
        for (i in 0 until numBars) {
          val angle = (i * (360f / numBars)) * (Math.PI / 180.0)
          val barLen = (rmsBoost * (0.6f + 0.4f * sin(i * 1.5 + innerRotation * 0.1f))).toFloat()
          val startX = (center.x + waveRadius * cos(angle)).toFloat()
          val startY = (center.y + waveRadius * sin(angle)).toFloat()
          val endX = (center.x + (waveRadius + barLen) * cos(angle)).toFloat()
          val endY = (center.y + (waveRadius + barLen) * sin(angle)).toFloat()

          drawLine(
            color = if (state.isListening) ArcGold else StarkCyanGlow,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
          )
        }
      }

      // 7. Central Arc Reactor Core Iris (glowing heart)
      val coreRadius = maxRadius * 0.30f * pulseFactor
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            primaryColor,
            primaryColor.copy(alpha = 0.6f),
            Color.Transparent
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // Core reticle crosshair
      val crosshairLen = coreRadius * 0.65f
      drawLine(
        color = Color.White.copy(alpha = 0.8f),
        start = Offset(center.x - crosshairLen, center.y),
        end = Offset(center.x + crosshairLen, center.y),
        strokeWidth = 1.5f
      )
      drawLine(
        color = Color.White.copy(alpha = 0.8f),
        start = Offset(center.x, center.y - crosshairLen),
        end = Offset(center.x, center.y + crosshairLen),
        strokeWidth = 1.5f
      )
    }

    // Centered status badge readout
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
    ) {
      Spacer(modifier = Modifier.height(130.dp))
      Text(
        text = state.status,
        color = primaryColor,
        fontFamily = OrbitronFontFamily,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
      )
      Text(
        text = "OUTPUT: ${state.energyPercent}%",
        color = HudTextSecondary,
        fontFamily = ShareTechMonoFontFamily,
        fontSize = 9.sp,
        letterSpacing = 1.sp
      )
    }
  }
}
