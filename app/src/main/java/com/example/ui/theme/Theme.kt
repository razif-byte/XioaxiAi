package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    background = ElegantBackground,
    onBackground = ElegantText,
    surface = ElegantSurface,
    onSurface = ElegantOnSurface,
    outline = ElegantOutline,
    secondary = ElegantTextSecondary,
    onSecondary = ElegantBackground,
    surfaceVariant = ElegantSurface,
    onSurfaceVariant = ElegantTextSecondary
  )

private val LightColorScheme = DarkColorScheme // Standardize on Elegant Dark for this app as requested

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for "Elegant Dark" design theme
  dynamicColor: Boolean = false, // Disable dynamic colors so our theme is strictly applied
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
