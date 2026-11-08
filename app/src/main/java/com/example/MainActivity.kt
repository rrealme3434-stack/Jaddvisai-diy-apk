package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.JarvisSoundFx
import com.example.ui.components.HudTopBar
import com.example.ui.screens.CoreHudScreen
import com.example.ui.screens.DiyHubScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.ArcGold
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.StarkCyan

class MainActivity : ComponentActivity() {

  private val viewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val activeTab by viewModel.activeTab.collectAsState()
        val telemetry by viewModel.telemetry.collectAsState()
        val soundFxEnabled by viewModel.soundFxEnabled.collectAsState()

        val audioPermissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
          if (isGranted) {
            viewModel.startVoiceListening()
          } else {
            Toast.makeText(context, "Microphone permission is required for voice commands.", Toast.LENGTH_SHORT).show()
          }
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
          if (isGranted) {
            viewModel.toggleFlashlight()
          }
        }

        LaunchedEffect(Unit) {
          // Play Jarvis system startup chime
          JarvisSoundFx.playSystemOnline()
        }

        // BackHandler: Return to Core HUD if in subscreen
        BackHandler(enabled = activeTab != HudTab.CORE_HUD) {
          viewModel.setActiveTab(HudTab.CORE_HUD)
        }

        Scaffold(
          contentWindowInsets = WindowInsets.safeDrawing,
          modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack),
          topBar = {
            HudTopBar(
              batteryPct = telemetry.batteryPercent,
              isCharging = telemetry.isCharging,
              soundFxEnabled = soundFxEnabled,
              onToggleSoundFx = { viewModel.toggleSoundFx() }
            )
          },
          bottomBar = {
            JarvisBottomNavigationBar(
              currentTab = activeTab,
              onTabSelected = { tab -> viewModel.setActiveTab(tab) }
            )
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(CyberBlack)
              .padding(innerPadding)
          ) {
            when (activeTab) {
              HudTab.CORE_HUD -> CoreHudScreen(
                viewModel = viewModel,
                onRequestAudioPermission = {
                  audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
              )
              HudTab.TERMINAL -> TerminalScreen(
                viewModel = viewModel,
                onRequestAudioPermission = {
                  audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
              )
              HudTab.DIY_HUB -> DiyHubScreen(viewModel = viewModel)
              HudTab.PROTOCOLS -> ProtocolsScreen(viewModel = viewModel)
              HudTab.TELEMETRY -> TelemetryScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

data class NavTabItem(
  val tab: HudTab,
  val label: String,
  val icon: ImageVector,
  val tag: String
)

@Composable
fun JarvisBottomNavigationBar(
  currentTab: HudTab,
  onTabSelected: (HudTab) -> Unit
) {
  val tabs = listOf(
    NavTabItem(HudTab.CORE_HUD, "CORE", Icons.Default.Adjust, "nav_core"),
    NavTabItem(HudTab.TERMINAL, "TERMINAL", Icons.Default.Terminal, "nav_terminal"),
    NavTabItem(HudTab.DIY_HUB, "DIY HUB", Icons.Default.DeveloperBoard, "nav_diy_hub"),
    NavTabItem(HudTab.PROTOCOLS, "PROTOCOLS", Icons.Default.Bolt, "nav_protocols"),
    NavTabItem(HudTab.TELEMETRY, "TELEMETRY", Icons.Default.Speed, "nav_telemetry")
  )

  Surface(
    color = CyberDark,
    border = BorderStroke(1.dp, CyberBorder),
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    NavigationBar(
      containerColor = CyberDark,
      contentColor = StarkCyan,
      tonalElevation = 0.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      tabs.forEach { item ->
        val selected = currentTab == item.tab
        NavigationBarItem(
          selected = selected,
          onClick = { onTabSelected(item.tab) },
          icon = {
            Icon(
              imageVector = item.icon,
              contentDescription = item.label,
              modifier = Modifier.size(20.dp)
            )
          },
          label = {
            Text(
              text = item.label,
              fontFamily = OrbitronFontFamily,
              fontSize = 9.sp,
              letterSpacing = 0.5.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = StarkCyan,
            selectedTextColor = StarkCyan,
            indicatorColor = StarkCyan.copy(alpha = 0.18f),
            unselectedIconColor = HudTextMuted,
            unselectedTextColor = HudTextMuted
          ),
          modifier = Modifier.testTag(item.tag)
        )
      }
    }
  }
}
