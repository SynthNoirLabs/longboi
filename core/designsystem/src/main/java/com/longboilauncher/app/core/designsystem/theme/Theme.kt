package com.longboilauncher.app.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.longboilauncher.app.core.model.ThemeType

val LocalThemeType = staticCompositionLocalOf { ThemeType.MATERIAL_YOU }

// Default Material colors if none provided
private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)

private val Purple40 = Color(0xFF6650a4)
private val PurpleGrey40 = Color(0xFF625b71)
private val Pink40 = Color(0xFF7D5260)

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
    )

private val MinimalistLightColors =
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        secondary = Color(0xFFF48C25), // Orange accent
        background = Color.White,
        surface = Color.White,
    )

private val SleekDarkColors =
    darkColorScheme(
        primary = Color(0xFFF2CC0D), // Gold
        onPrimary = Color.Black,
        background = Color.Black,
        surface = Color(0xFF121212),
    )

private val PlayfulShapes =
    Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(24.dp),
        large = RoundedCornerShape(32.dp),
        extraLarge = RoundedCornerShape(40.dp),
    )

private val PlayfulLightColors =
    lightColorScheme(
        primary = Color(0xFF25AFF4),
        secondary = Color(0xFFF425AF),
        background = Color(0xFFF0F8FF),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

@Composable
fun LongboiLauncherTheme(
    themeType: ThemeType = ThemeType.MATERIAL_YOU,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when (themeType) {
            ThemeType.MATERIAL_YOU -> {
                if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else if (darkTheme) {
                    DarkColorScheme
                } else {
                    LightColorScheme
                }
            }
            ThemeType.MODERN_MINIMALIST -> MinimalistLightColors
            ThemeType.SOPHISTICATED_SLEEK -> SleekDarkColors
            ThemeType.VIBRANT_PLAYFUL -> PlayfulLightColors
            ThemeType.GLASSMORPHISM -> {
                // Glassmorphism uses transparency, so we'll use a modified dark theme
                darkColorScheme(
                    primary = Color(0xFFBBDEFB),
                    background = Color.Transparent, // Surface handles the background
                )
            }
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeType provides themeType) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = if (themeType == ThemeType.VIBRANT_PLAYFUL) PlayfulShapes else MaterialTheme.shapes,
            content = content,
        )
    }
}
