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

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E9E5B),
    secondary = Color(0xFF8BC34A),
    tertiary = Color(0xFFA2F7B5),
    background = Color(0xFF111411),
    surface = Color(0xFF1E221F),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE2E3DE),
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = Color(0xFF282D29),
    onSurfaceVariant = Color(0xFFC2C8C3)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F7A3E),
    secondary = Color(0xFF2E9E5B),
    tertiary = Color(0xFF8BC34A),
    background = Color(0xFFF3F6F4),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1C19),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFE0E5E0),
    onSurfaceVariant = Color(0xFF424943)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

