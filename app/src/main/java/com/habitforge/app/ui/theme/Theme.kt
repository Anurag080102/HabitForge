package com.habitforge.app.ui.theme

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

// UI styling: Orange and white color scheme
private val DarkColorScheme = darkColorScheme(
    primary = OrangeDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = OrangeContainer.copy(alpha = 0.2f),
    onPrimaryContainer = OrangeLight,
    secondary = OrangeLight,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = OrangeLight,
    onTertiary = Color(0xFFFFFFFF),
    background = DarkBackground,
    onBackground = Color(0xFFFFFFFF),
    surface = DarkSurface,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OrangePrimaryDark,
    secondary = OrangeLight,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = OrangeLight,
    onTertiary = Color(0xFFFFFFFF),
    background = WhiteBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = WhiteBackground,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = LightGray,
    onSurfaceVariant = Color(0xFF666666),
    outline = GrayVariant,
    outlineVariant = GrayVariant
)

@Composable
fun HabitForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // UI styling: Disabled to use custom orange theme
    content: @Composable () -> Unit
) {
    // UI styling: Force custom orange color scheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

