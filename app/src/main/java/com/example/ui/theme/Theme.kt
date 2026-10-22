package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MayraDarkColorScheme = darkColorScheme(
    primary = MayraPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = MayraPurpleDeep,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = MayraCyanAccent,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = MayraPinkAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = MayraNavyDark,
    onBackground = MayraTextPrimary,
    surface = MayraNavySurface,
    onSurface = MayraTextPrimary,
    surfaceVariant = MayraNavyCard,
    onSurfaceVariant = MayraTextSecondary,
    outline = MayraNavyBorder,
    error = MayraErrorRed,
    onError = Color.White
)

val MayraLightColorScheme = lightColorScheme(
    primary = MayraPurpleDeep,
    onPrimary = Color.White,
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    tertiary = Color(0xFFBE185D),
    onTertiary = Color.White,
    background = MayraLightBg,
    onBackground = MayraLightTextPrimary,
    surface = MayraLightSurface,
    onSurface = MayraLightTextPrimary,
    surfaceVariant = MayraLightCard,
    onSurfaceVariant = MayraLightTextSecondary,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MayraTheme(
    darkTheme: Boolean = true, // Default to futuristic dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MayraDarkColorScheme else MayraLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
