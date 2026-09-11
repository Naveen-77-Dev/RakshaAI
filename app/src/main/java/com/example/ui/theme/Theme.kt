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
    primary = RakshaTealLight,
    onPrimary = Color.Black,
    primaryContainer = RakshaNavyLight,
    onPrimaryContainer = RakshaTealLight,
    secondary = Color(0xFF60A5FA),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFBBF24),
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceContainerDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = RiskCriticalRed,
    errorContainer = RiskCriticalContainer,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RakshaTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = RakshaSapphire,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = RakshaSapphireDark,
    tertiary = RiskCautionAmber,
    background = SurfaceLight,
    surface = SurfaceContainerLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = RiskCriticalRed,
    errorContainer = RiskCriticalContainer,
    onError = Color.White
)

@Composable
fun RakshaAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tuned cyber trust palette
    content: @Composable () -> Unit
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
