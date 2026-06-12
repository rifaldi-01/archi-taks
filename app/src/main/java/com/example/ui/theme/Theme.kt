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
    primary = MinimalistDarkPrimary,
    onPrimary = MinimalistDarkOnPrimary,
    primaryContainer = MinimalistDarkPrimaryContainer,
    onPrimaryContainer = MinimalistDarkOnPrimaryContainer,
    secondary = Color(0xFFD0BCFF),
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFE7E0FF),
    background = MinimalistDarkBg,
    surface = MinimalistDarkSurface,
    onBackground = Color(0xFFEFF1F8),
    onSurface = Color(0xFFEFF1F8),
    surfaceVariant = Color(0xFF2F323A),
    onSurfaceVariant = Color(0xFF9EABB8),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    outline = MinimalistDarkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MinimalistPrimary,
    onPrimary = Color.White,
    primaryContainer = MinimalistPrimaryContainer,
    onPrimaryContainer = MinimalistOnPrimaryContainer,
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7E0FF),
    onSecondaryContainer = Color(0xFF21005D),
    background = MinimalistBg,
    surface = MinimalistSurface,
    onBackground = MinimalistDarkText,
    onSurface = MinimalistDarkText,
    surfaceVariant = Color(0xFFEFF1F8),
    onSurfaceVariant = MinimalistSecondaryText,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    outline = MinimalistBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Clean Minimalism (light background) is enabled by default
  dynamicColor: Boolean = false, // Preserve carefully-designed branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
