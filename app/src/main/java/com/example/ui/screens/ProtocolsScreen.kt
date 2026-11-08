package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.JarvisViewModel
import com.example.data.ActionType
import com.example.data.ProtocolRoutine
import com.example.ui.components.SciFiButton
import com.example.ui.components.SciFiCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProtocolsScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val protocols by viewModel.protocols.collectAsState()
  var showCreateDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "JARVIS PROTOCOLS",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Text(
            text = "AUTOMATION SEQUENCES & WORKFLOWS",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = HudTextMuted
          )
        }

        SciFiButton(
          text = "+ PROTOCOL",
          onClick = { showCreateDialog = true },
          icon = Icons.Default.Add,
          isPrimary = false,
          testTag = "create_protocol_button"
        )
      }
    }

    // 2. Protocol Cards
    items(protocols, key = { it.id }) { protocol ->
      ProtocolCard(
        protocol = protocol,
        onExecute = { viewModel.executeProtocol(protocol) }
      )
    }
  }

  if (showCreateDialog) {
    CreateProtocolDialog(
      onDismiss = { showCreateDialog = false },
      onConfirm = { title, codeName, trigger, desc, speech ->
        viewModel.addCustomProtocol(title, codeName, trigger, desc, speech)
        showCreateDialog = false
      }
    )
  }
}

@Composable
fun ProtocolCard(
  protocol: ProtocolRoutine,
  onExecute: () -> Unit
) {
  val isLockdown = protocol.id == "lockdown"
  val accentColor = if (isLockdown) AlertRed else StarkCyan

  val lastRunStr = remember(protocol.lastExecuted) {
    if (protocol.lastExecuted != null) {
      SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(protocol.lastExecuted))
    } else {
      "NEVER"
    }
  }

  SciFiCard(
    modifier = Modifier.fillMaxWidth(),
    borderColor = if (isLockdown) AlertRed.copy(alpha = 0.8f) else CyberBorder
  ) {
    Column {
      // Header: CodeName badge & Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
              .background(accentColor.copy(alpha = 0.2f))
              .border(1.dp, accentColor, CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = protocol.codeName,
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = accentColor,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Text(
            text = protocol.title,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = HudTextPrimary
          )
        }

        SciFiButton(
          text = "EXECUTE",
          onClick = onExecute,
          icon = if (isLockdown) Icons.Default.Security else Icons.Default.PlayArrow,
          isPrimary = !isLockdown,
          isAlert = isLockdown,
          testTag = "execute_protocol_${protocol.id}"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = protocol.description,
        fontFamily = ShareTechMonoFontFamily,
        fontSize = 11.sp,
        color = HudTextSecondary,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Trigger Phrase & Last Run
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "VOICE TRIGGER: ",
            fontFamily = OrbitronFontFamily,
            fontSize = 9.sp,
            color = HudTextMuted
          )
          Text(
            text = "\"${protocol.voiceTrigger}\"",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = ArcGold
          )
        }

        Text(
          text = "LAST RUN: $lastRunStr",
          fontFamily = ShareTechMonoFontFamily,
          fontSize = 9.sp,
          color = HudTextMuted
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Action Badges Flow
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        protocol.actions.forEach { action ->
          val (icon, label) = when (action.actionType) {
            ActionType.SPEAK -> Icons.Default.RecordVoiceOver to "VOICE"
            ActionType.SOUND_FX -> Icons.Default.VolumeUp to "AUDIO"
            ActionType.FLASHLIGHT_STROBE -> Icons.Default.FlashlightOn to "STROBE"
            ActionType.VIBRATE -> Icons.Default.Vibration to "HAPTIC"
            ActionType.TRIGGER_DEVICE -> Icons.Default.PlayArrow to "IOT NODE"
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(CyberDark)
              .border(1.dp, CyberBorder, RoundedCornerShape(3.dp))
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = label,
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 9.sp,
              color = HudTextSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
fun CreateProtocolDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, codeName: String, trigger: String, desc: String, speech: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var codeName by remember { mutableStateOf("MK-99") }
  var trigger by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var speech by remember { mutableStateOf("Protocol executed. All parameters nominal, sir.") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = CyberDark,
    title = {
      Text(
        text = "COMPILE CUSTOM PROTOCOL",
        fontFamily = OrbitronFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = StarkCyan
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Protocol Title (e.g. Protocol House Party)", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HudTextPrimary,
            unfocusedTextColor = HudTextPrimary,
            focusedBorderColor = StarkCyan,
            unfocusedBorderColor = CyberBorder
          ),
          modifier = Modifier.fillMaxWidth().testTag("custom_protocol_title_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = codeName,
            onValueChange = { codeName = it },
            label = { Text("CodeName (e.g. OMEGA-7)", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = HudTextPrimary,
              unfocusedTextColor = HudTextPrimary,
              focusedBorderColor = StarkCyan,
              unfocusedBorderColor = CyberBorder
            ),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = trigger,
            onValueChange = { trigger = it },
            label = { Text("Voice Trigger", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = HudTextPrimary,
              unfocusedTextColor = HudTextPrimary,
              focusedBorderColor = StarkCyan,
              unfocusedBorderColor = CyberBorder
            ),
            modifier = Modifier.weight(1f).testTag("custom_protocol_trigger_input")
          )
        }

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HudTextPrimary,
            unfocusedTextColor = HudTextPrimary,
            focusedBorderColor = StarkCyan,
            unfocusedBorderColor = CyberBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = speech,
          onValueChange = { speech = it },
          label = { Text("Jarvis Voice Response", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HudTextPrimary,
            unfocusedTextColor = HudTextPrimary,
            focusedBorderColor = StarkCyan,
            unfocusedBorderColor = CyberBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (title.isNotBlank() && trigger.isNotBlank()) {
            onConfirm(title, codeName, trigger, desc, speech)
          }
        },
        modifier = Modifier.testTag("confirm_create_protocol_button")
      ) {
        Text("COMPILE", fontFamily = OrbitronFontFamily, color = StarkCyan)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", fontFamily = OrbitronFontFamily, color = HudTextMuted)
      }
    }
  )
}
