package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OrbitronFontFamily = FontFamily(
  Font(R.font.orbitron, FontWeight.Normal)
)

val ShareTechMonoFontFamily = FontFamily(
  Font(R.font.share_tech_mono, FontWeight.Normal)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 1.sp
  ),
  displayMedium = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.5.sp
  ),
  displaySmall = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.25.sp
  ),
  headlineLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.25.sp
  ),
  titleLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.5.sp
  ),
  titleMedium = TextStyle(
    fontFamily = ShareTechMonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.25.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = ShareTechMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.25.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = ShareTechMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.2.sp
  ),
  bodySmall = TextStyle(
    fontFamily = ShareTechMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp
  ),
  labelLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = ShareTechMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.5.sp
  ),
  labelSmall = TextStyle(
    fontFamily = ShareTechMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 9.sp,
    lineHeight = 12.sp,
    letterSpacing = 0.5.sp
  )
)
