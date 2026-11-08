package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
  primary = StarkCyan,
  onPrimary = CyberBlack,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = StarkCyanGlow,
  secondary = ArcGold,
  onSecondary = CyberBlack,
  secondaryContainer = CyberSurfaceVariant,
  onSecondaryContainer = ArcGoldGlow,
  tertiary = SystemOnlineGreen,
  onTertiary = CyberBlack,
  background = CyberBlack,
  onBackground = HudTextPrimary,
  surface = CyberDark,
  onSurface = HudTextPrimary,
  surfaceVariant = CyberSurface,
  onSurfaceVariant = HudTextSecondary,
  outline = CyberBorder,
  outlineVariant = CyberBorder.copy(alpha = 0.5f),
  error = AlertRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = JarvisDarkColorScheme,
    typography = Typography,
    content = content
  )
}
