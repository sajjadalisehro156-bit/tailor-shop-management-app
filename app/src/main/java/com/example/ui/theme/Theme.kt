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

private val DarkColorScheme = darkColorScheme(
    primary = TailorDarkGold,
    onPrimary = Color(0xFF141A2B),
    primaryContainer = Color(0xFF333D5E),
    onPrimaryContainer = TailorDarkGold,
    secondary = TailorDarkGold,
    onSecondary = Color(0xFF141A2B),
    background = TailorDarkBg,
    onBackground = TailorDarkInk,
    surface = TailorDarkCard,
    onSurface = TailorDarkInk,
    surfaceVariant = Color(0xFF263152),
    onSurfaceVariant = TailorDarkMuted,
    outline = TailorDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TailorNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E7F5),
    onPrimaryContainer = TailorNavy,
    secondary = TailorTapeGold,
    onSecondary = Color(0xFF1D2740),
    secondaryContainer = Color(0xFFFDF4DC),
    onSecondaryContainer = Color(0xFF523B00),
    background = TailorCreamBg,
    onBackground = TailorInk,
    surface = TailorCardLight,
    onSurface = TailorInk,
    surfaceVariant = Color(0xFFEBE6DC),
    onSurfaceVariant = TailorMuted,
    outline = TailorBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct tailor branding colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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
