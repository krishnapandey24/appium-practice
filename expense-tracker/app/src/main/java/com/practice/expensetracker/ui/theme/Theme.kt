package com.practice.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E6B4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBD9),
    onPrimaryContainer = Color(0xFF0B2A1C),
    secondary = Color(0xFF4F6358),
    secondaryContainer = Color(0xFFD2E8DA),
    tertiary = Color(0xFF3C6472),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFF7FAF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD5B0),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF145136),
    onPrimaryContainer = Color(0xFFCDEBD9),
    secondary = Color(0xFFB6CCBE),
    secondaryContainer = Color(0xFF384B40),
    tertiary = Color(0xFFA4CDDD),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
)

@Composable
fun ExpenseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
