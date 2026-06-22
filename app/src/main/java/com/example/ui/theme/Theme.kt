package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CwcGreen,
    secondary = CwcDarkGreen,
    tertiary = GoldBadge,
    background = CwcBlack,
    surface = CwcSlate,
    onPrimary = CwcBlack,
    onSecondary = Color.White,
    onTertiary = CwcBlack,
    onBackground = Color.White,
    onSurface = Color.White,
    error = OrangeWarning
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CwcDarkGreen,
    secondary = CwcGreen,
    tertiary = GoldBadge,
    background = CwcSurfaceLight,
    surface = CwcCardLight,
    onPrimary = Color.White,
    onSecondary = CwcBlack,
    onTertiary = CwcBlack,
    onBackground = CwcBlack,
    onSurface = CwcBlack,
    error = OrangeWarning
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic system color fallback to safeguard CWC brand consistency
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      // Dynamic color disabled to strictly present our exact CWC color profile
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
