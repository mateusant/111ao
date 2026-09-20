package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SecCyanLight,
    onPrimary = Color(0xFF003554),
    primaryContainer = SecCyanDark,
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = SecAmber,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = SecEmerald,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = SecEmeraldDark,
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = SecRed,
    onError = Color.White,
    background = SecNavyDark,
    onBackground = Color(0xFFF1F5F9),
    surface = SecSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SecSurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = SecBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = SecCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = SecAmberDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SecEmeraldDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = SecRedDark,
    onError = Color.White,
    background = SecNavyLight,
    onBackground = Color(0xFF0F172A),
    surface = SecSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SecSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = SecBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tactical branding identity consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

