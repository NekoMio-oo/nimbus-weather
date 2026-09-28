package com.example.nimbus.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fallback palette for devices without dynamic colour (Android 11 and below): a sky-blue seed with an
// amber tertiary for the sun, laid out in Material 3 tonal roles. On Android 12+ the wallpaper wins.

val LightColors = lightColorScheme(
    primary = Color(0xFF2456B8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = Color(0xFF00658E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC7E7FF),
    onSecondaryContainer = Color(0xFF001E2E),
    tertiary = Color(0xFF7A5900),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF261A00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF7F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFAFC6FF),
    onPrimary = Color(0xFF002D6C),
    primaryContainer = Color(0xFF00429A),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFF84CFFF),
    onSecondary = Color(0xFF00344C),
    secondaryContainer = Color(0xFF004C6C),
    onSecondaryContainer = Color(0xFFC7E7FF),
    tertiary = Color(0xFFF5BE48),
    onTertiary = Color(0xFF402D00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFDF9E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
)
