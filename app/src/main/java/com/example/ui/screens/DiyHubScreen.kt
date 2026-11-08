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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.audio.JarvisSoundFx
import com.example.data.DiyDevice
import com.example.data.DiyDeviceType
import com.example.data.DiyPinout
import com.example.ui.components.SciFiButton
import com.example.ui.components.SciFiCard
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
import com.example.ui.theme.SystemOnlineGreen
import java.util.UUID

@Composable
fun DiyHubScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val devices by viewModel.diyDevices.collectAsState()
  val pinouts by viewModel.pinouts.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header & Add Device
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "DIY HARDWARE GRID",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = StarkCyan,
            letterSpacing = 1.sp
          )
          Text(
            text = "${devices.size} ACTIVE IOT NODES REGISTERED",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = HudTextMuted
          )
        }

        SciFiButton(
          text = "+ NODE",
          onClick = { showAddDialog = true },
          icon = Icons.Default.Add,
          isPrimary = false,
          testTag = "add_diy_node_button"
        )
      }
    }

    // 2. DIY Registered Devices
    items(devices, key = { it.id }) { device ->
      DiyDeviceCard(
        device = device,
        onToggle = { viewModel.toggleDevice(device.id) },
        onDelete = { viewModel.removeDevice(device.id) }
      )
    }

    // 3. GPIO Pinout & Logic Workbench Header
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.DeveloperBoard,
            contentDescription = null,
            tint = ArcGold,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "GPIO & SENSOR WORKBENCH",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = ArcGold,
            letterSpacing = 1.sp
          )
        }
        Text(
          text = "ESP32/ARDUINO BUS",
          fontFamily = ShareTechMonoFontFamily,
          fontSize = 10.sp,
          color = HudTextMuted
        )
      }
    }

    // 4. Pinout Matrix items
    items(pinouts, key = { it.pinNumber }) { pin ->
      GpioPinCard(
        pin = pin,
        onToggleDigital = { viewModel.togglePinState(pin.pinNumber, pin.digitalState) },
        onPwmChange = { value -> viewModel.setPinPwm(pin.pinNumber, value) }
      )
    }
  }

  if (showAddDialog) {
    AddDiyDeviceDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { newDevice ->
        viewModel.addCustomDevice(newDevice)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun DiyDeviceCard(
  device: DiyDevice,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  val typeIcon = when (device.deviceType) {
    DiyDeviceType.ESP32 -> Icons.Default.Memory
    DiyDeviceType.RASPBERRY_PI -> Icons.Default.DeveloperBoard
    DiyDeviceType.ARDUINO -> Icons.Default.PowerSettingsNew
    DiyDeviceType.SMART_RELAY -> Icons.Default.ToggleOn
    DiyDeviceType.SENSOR_NODE -> Icons.Default.Sensors
  }

  val activeColor = if (device.state) SystemOnlineGreen else HudTextMuted

  SciFiCard(
    modifier = Modifier.fillMaxWidth(),
    borderColor = if (device.state) StarkCyan.copy(alpha = 0.8f) else CyberBorder
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(CyberSurfaceVariant)
              .border(1.dp, if (device.state) StarkCyan else CyberBorder, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = typeIcon,
              contentDescription = null,
              tint = if (device.state) StarkCyan else HudTextMuted,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = device.name,
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp,
              color = HudTextPrimary
            )
            Text(
              text = "${device.deviceType.name} // ${device.ipAddressOrUrl}",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = HudTextMuted,
              maxLines = 1
            )
          }
        }

        // Toggle Switch Button
        Box(
          modifier = Modifier
            .clip(CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp))
            .background(if (device.state) StarkCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
              1.dp,
              if (device.state) StarkCyan else CyberBorder,
              CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("toggle_device_${device.id}")
        ) {
          Text(
            text = if (device.state) "STATE: ON" else "STATE: OFF",
            fontFamily = OrbitronFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (device.state) StarkCyan else HudTextMuted
          )
        }
      }

      if (device.sensorReading != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(CyberDark)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Thermostat,
              contentDescription = null,
              tint = ArcGold,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "TELEMETRY PAYLOAD: ${device.sensorReading}",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = HudTextSecondary
            )
          }

          Text(
            text = "METHOD: ${device.httpMethod}",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 9.sp,
            color = HudTextMuted
          )
        }
      }
    }
  }
}

@Composable
fun GpioPinCard(
  pin: DiyPinout,
  onToggleDigital: () -> Unit,
  onPwmChange: (Float) -> Unit
) {
  val isPwm = pin.mode == "PWM_OUTPUT" || pin.mode == "ANALOG_INPUT"

  SciFiCard(
    modifier = Modifier.fillMaxWidth(),
    borderColor = if (pin.digitalState) ArcGold.copy(alpha = 0.5f) else CyberBorder
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Illuminated LED indicator
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (pin.digitalState) ArcGold else CyberBorder)
              .border(
                1.dp,
                if (pin.digitalState) ArcGold else CyberBorder,
                CircleShape
              )
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "GPIO ${pin.pinNumber}: ${pin.pinName}",
              fontFamily = OrbitronFontFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = HudTextPrimary
            )
            Text(
              text = "MODE: ${pin.mode} | READOUT: ${String.format("%.2f", pin.analogValue)}V",
              fontFamily = ShareTechMonoFontFamily,
              fontSize = 10.sp,
              color = HudTextSecondary
            )
          }
        }

        if (!isPwm) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (pin.digitalState) ArcGold.copy(alpha = 0.2f) else CyberSurfaceVariant)
              .border(
                1.dp,
                if (pin.digitalState) ArcGold else CyberBorder,
                RoundedCornerShape(4.dp)
              )
              .clickable(onClick = onToggleDigital)
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("pin_${pin.pinNumber}_toggle")
          ) {
            Text(
              text = if (pin.digitalState) "HIGH [3.3V]" else "LOW [0.0V]",
              fontFamily = OrbitronFontFamily,
              fontSize = 9.sp,
              color = if (pin.digitalState) ArcGold else HudTextMuted
            )
          }
        }
      }

      if (isPwm) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Slider(
            value = pin.analogValue,
            onValueChange = onPwmChange,
            valueRange = 0f..3.3f,
            modifier = Modifier
              .weight(1f)
              .testTag("pin_${pin.pinNumber}_slider"),
            colors = SliderDefaults.colors(
              thumbColor = ArcGold,
              activeTrackColor = ArcGold,
              inactiveTrackColor = CyberBorder
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${(pin.analogValue / 3.3f * 100).toInt()}%",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = ArcGold,
            modifier = Modifier.width(36.dp)
          )
        }
      }
    }
  }
}

@Composable
fun AddDiyDeviceDialog(
  onDismiss: () -> Unit,
  onConfirm: (DiyDevice) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var ipUrl by remember { mutableStateOf("http://192.168.1.150/toggle") }
  var payload by remember { mutableStateOf("{\"state\": \"on\"}") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = CyberDark,
    title = {
      Text(
        text = "REGISTER DIY HARDWARE NODE",
        fontFamily = OrbitronFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = StarkCyan
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Device Name (e.g. ESP32 Relay)", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HudTextPrimary,
            unfocusedTextColor = HudTextPrimary,
            focusedBorderColor = StarkCyan,
            unfocusedBorderColor = CyberBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_device_name_input")
        )

        OutlinedTextField(
          value = ipUrl,
          onValueChange = { ipUrl = it },
          label = { Text("HTTP / REST Endpoint URL", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HudTextPrimary,
            unfocusedTextColor = HudTextPrimary,
            focusedBorderColor = StarkCyan,
            unfocusedBorderColor = CyberBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_device_url_input")
        )

        OutlinedTextField(
          value = payload,
          onValueChange = { payload = it },
          label = { Text("POST Payload JSON (optional)", color = HudTextSecondary, fontFamily = ShareTechMonoFontFamily) },
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
          if (name.isNotBlank()) {
            val newDevice = DiyDevice(
              id = "diy_${UUID.randomUUID()}",
              name = name,
              deviceType = DiyDeviceType.ESP32,
              ipAddressOrUrl = ipUrl,
              httpMethod = "POST",
              payload = payload,
              isOnline = true,
              state = false,
              sensorReading = "Standby"
            )
            onConfirm(newDevice)
          }
        },
        modifier = Modifier.testTag("confirm_add_device_button")
      ) {
        Text("BIND NODE", fontFamily = OrbitronFontFamily, color = StarkCyan)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", fontFamily = OrbitronFontFamily, color = HudTextMuted)
      }
    }
  )
}
