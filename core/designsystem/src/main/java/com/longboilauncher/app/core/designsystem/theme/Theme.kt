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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.longboilauncher.app.core.model.ThemeType

@Immutable
data class LongboiColors(
    val onWallpaperContent: Color,
    val cardAlpha: Float,
    val borderAlpha: Float,
    val useBlur: Boolean,
    val glassIntensity: Float = 0f,
)

val LocalLongboiColors =
    staticCompositionLocalOf {
        LongboiColors(
            onWallpaperContent = Color.White,
            cardAlpha = 0.3f,
            borderAlpha = 0.2f,
            useBlur = false,
        )
    }

val LocalThemeType = staticCompositionLocalOf { ThemeType.MATERIAL_YOU }

// Default Material colors
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

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

private val MinimalistLightColors =
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        secondary = Color(0xFFF48C25),
        onSecondary = Color.White,
        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFF757575),
    )

private val SleekDarkColors =
    darkColorScheme(
        primary = Color(0xFFF2CC0D),
        onPrimary = Color.Black,
        secondary = Color(0xFFE0E0E0),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color.White,
        surface = Color(0xFF121212),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFFBDBDBD),
    )

private val PlayfulLightColors =
    lightColorScheme(
        primary = Color(0xFF25AFF4),
        onPrimary = Color.White,
        secondary = Color(0xFFF425AF),
        onSecondary = Color.White,
        tertiary = Color(0xFFA5D6A7),
        background = Color(0xFFF0F8FF),
        onBackground = Color(0xFF001F3F),
        surface = Color.White,
        onSurface = Color(0xFF001F3F),
        surfaceVariant = Color(0xFFE1F5FE),
        onSurfaceVariant = Color(0xFF0277BD),
    )

private val PlayfulShapes =
    Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(24.dp),
        large = RoundedCornerShape(32.dp),
        extraLarge = RoundedCornerShape(40.dp),
    )

@Composable
fun LongboiLauncherTheme(
    themeType: ThemeType = ThemeType.MATERIAL_YOU,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val longboiColors =
        when (themeType) {
            ThemeType.GLASSMORPHISM ->
                LongboiColors(
                    onWallpaperContent = Color.White,
                    cardAlpha = 0.15f,
                    borderAlpha = 0.2f,
                    useBlur = true,
                    glassIntensity = 0.15f,
                )
            ThemeType.VIBRANT_PLAYFUL ->
                LongboiColors(
                    onWallpaperContent = Color(0xFF001F3F),
                    cardAlpha = 1.0f,
                    borderAlpha = 0.0f,
                    useBlur = false,
                )
            ThemeType.SOPHISTICATED_SLEEK ->
                LongboiColors(
                    onWallpaperContent = Color(0xFFF2CC0D),
                    cardAlpha = 0.8f,
                    borderAlpha = 0.3f,
                    useBlur = false,
                )
            ThemeType.MODERN_MINIMALIST ->
                LongboiColors(
                    onWallpaperContent = Color.Black,
                    cardAlpha = 0.0f,
                    borderAlpha = 0.0f,
                    useBlur = false,
                )
            ThemeType.MATERIAL_YOU ->
                LongboiColors(
                    onWallpaperContent = if (darkTheme) Color.White else Color.Black,
                    cardAlpha = 0.3f,
                    borderAlpha = 0.1f,
                    useBlur = false,
                )
        }

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
                darkColorScheme(
                    primary = Color(0xFFBBDEFB),
                    background = Color.Transparent,
                )
            }
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalThemeType provides themeType,
        LocalLongboiColors provides longboiColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = if (themeType == ThemeType.VIBRANT_PLAYFUL) PlayfulShapes else MaterialTheme.shapes,
            content = content,
        )
    }
}
