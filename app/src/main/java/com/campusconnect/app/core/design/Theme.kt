package com.campusconnect.app.core.design

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MidAmber = Color(0xFF6B4A0A)
private val LightMuted = Color(0xFFB6C6D8)
private val LightRose = Color(0xFFFDA4AF)
private val DarkRose = Color(0xFF4C0519)

private val LightColors = lightColorScheme(
    primary = Teal600,
    onPrimary = White,
    primaryContainer = Teal100,
    onPrimaryContainer = Navy800,
    secondary = Navy700,
    onSecondary = White,
    secondaryContainer = Cloud200,
    onSecondaryContainer = Navy800,
    tertiary = Amber500,
    onTertiary = Navy900,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Navy800,
    background = Cloud50,
    onBackground = Ink900,
    surface = White,
    onSurface = Ink900,
    surfaceVariant = Cloud100,
    onSurfaceVariant = Ink700,
    outline = Ink300,
    outlineVariant = Cloud200,
    error = Rose500,
    onError = White,
    errorContainer = Rose100,
    onErrorContainer = Navy900,
)

private val DarkColors = darkColorScheme(
    primary = Teal400,
    onPrimary = Navy900,
    primaryContainer = Teal600,
    onPrimaryContainer = Teal100,
    secondary = Teal200,
    onSecondary = Navy900,
    secondaryContainer = Navy600,
    onSecondaryContainer = Teal100,
    tertiary = Amber500,
    onTertiary = Navy900,
    tertiaryContainer = MidAmber,
    onTertiaryContainer = Amber100,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = LightRose,
    onError = Navy900,
    errorContainer = DarkRose,
    onErrorContainer = LightRose,
)

@Composable
fun CampusConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = CampusTypography,
        shapes = CampusShapes,
        content = content,
    )
}
