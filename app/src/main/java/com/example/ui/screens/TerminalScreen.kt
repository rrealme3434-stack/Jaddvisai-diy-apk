package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.JarvisViewModel
import com.example.audio.JarvisSoundFx
import com.example.data.JarvisMessage
import com.example.ui.components.LiveWaveformRow
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalScreen(
  viewModel: JarvisViewModel,
  onRequestAudioPermission: () -> Unit,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.messages.collectAsState()
  val reactorState by viewModel.reactorState.collectAsState()
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto scroll to bottom when new messages appear
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val suggestedQueries = listOf(
    "Check diagnostics",
    "Protocol Clean Slate",
    "Toggle torch",
    "Toggle ESP32 lights",
    "What is Jarvis?",
    "Calculate 18 * 45",
    "Tell me a joke"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDark)
  ) {
    // Top Terminal Header Toolbar
    Surface(
      color = CyberSurface,
      border = BorderStroke(1.dp, CyberBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            tint = StarkCyan,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "JARVIS TERMINAL // CONSOLE",
            fontFamily = OrbitronFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
        }

        IconButton(
          onClick = {
            JarvisSoundFx.playClick()
            viewModel.clearTerminal()
          },
          modifier = Modifier
            .size(32.dp)
            .testTag("clear_terminal_button")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear Terminal",
            tint = HudTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Audio reactive banner if speaking/listening
    if (reactorState.isListening || reactorState.isSpeaking) {
      LiveWaveformRow(
        isActive = true,
        audioRms = reactorState.audioRms,
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberSurfaceVariant.copy(alpha = 0.5f))
          .padding(vertical = 4.dp)
      )
    }

    // Conversation List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 12.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages, key = { it.id }) { message ->
        TerminalMessageCard(
          message = message,
          onActionClick = { actionText ->
            viewModel.processUserQuery(actionText)
          }
        )
      }
    }

    // Suggestion Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(suggestedQueries) { query ->
        Box(
          modifier = Modifier
            .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberBorder, CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .clickable {
              JarvisSoundFx.playClick()
              viewModel.processUserQuery(query)
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = query,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = StarkCyan
          )
        }
      }
    }

    // Bottom Input Bar
    Surface(
      color = CyberSurface,
      border = BorderStroke(1.dp, CyberBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Voice Button
        IconButton(
          onClick = {
            if (reactorState.isListening) {
              viewModel.stopVoiceListening()
            } else {
              onRequestAudioPermission()
              viewModel.startVoiceListening()
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (reactorState.isListening) ArcGold.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
              1.dp,
              if (reactorState.isListening) ArcGold else CyberBorder,
              CircleShape
            )
            .testTag("terminal_mic_button")
        ) {
          Icon(
            imageVector = if (reactorState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Voice Input",
            tint = if (reactorState.isListening) ArcGold else StarkCyan,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = {
            Text(
              text = if (reactorState.isListening) "Listening to voice input..." else "Command J.A.R.V.I.S....",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 12.sp,
              color = HudTextMuted
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("terminal_input_field"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HudTextPrimary,
            unfocusedTextColor = HudTextPrimary,
            focusedBorderColor = StarkCyan,
            unfocusedBorderColor = CyberBorder,
            focusedContainerColor = CyberDark,
            unfocusedContainerColor = CyberDark
          ),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 13.sp
          ),
          shape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(
            onSend = {
              if (inputText.isNotBlank()) {
                val q = inputText
                inputText = ""
                viewModel.processUserQuery(q)
              }
            }
          )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (inputText.isNotBlank()) {
              val q = inputText
              inputText = ""
              viewModel.processUserQuery(q)
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(StarkCyan.copy(alpha = 0.15f))
            .border(1.dp, StarkCyan, CircleShape)
            .testTag("terminal_send_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Command",
            tint = StarkCyan,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun TerminalMessageCard(
  message: JarvisMessage,
  onActionClick: (String) -> Unit
) {
  val timeStr = remember(message.timestamp) {
    SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(message.timestamp))
  }

  val isUser = message.isFromUser
  val borderColor = if (isUser) ArcGold.copy(alpha = 0.6f) else StarkCyan.copy(alpha = 0.6f)
  val shape = CutCornerShape(
    topStart = if (isUser) 10.dp else 0.dp,
    topEnd = if (isUser) 0.dp else 10.dp,
    bottomStart = 10.dp,
    bottomEnd = 10.dp
  )

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(shape)
        .background(if (isUser) CyberSurfaceVariant.copy(alpha = 0.7f) else CyberSurface)
        .border(1.dp, borderColor, shape)
        .padding(12.dp)
        .testTag(if (isUser) "user_message_bubble" else "jarvis_message_bubble")
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isUser) "OPERATOR // COMMAND" else "J.A.R.V.I.S. // AI BUTLER",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (isUser) ArcGold else StarkCyan,
            letterSpacing = 1.sp
          )
          Text(
            text = timeStr,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 9.sp,
            color = HudTextMuted
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = message.text,
          fontFamily = ShareTechMonoFontFamily,
          fontSize = 13.sp,
          color = HudTextPrimary,
          lineHeight = 18.sp
        )

        if (message.suggestedActions.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            message.suggestedActions.forEach { action ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(CyberDark)
                  .border(1.dp, StarkCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                  .clickable {
                    JarvisSoundFx.playClick()
                    onActionClick(action)
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = action,
                  fontFamily = ShareTechMonoFontFamily,
                  fontSize = 10.sp,
                  color = StarkCyanGlow
                )
              }
            }
          }
        }
      }
    }
  }
}
