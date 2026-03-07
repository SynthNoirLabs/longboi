package com.longboilauncher.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Premium typography for Longboi Launcher.
 * Focuses on clean, high-contrast sans-serif weights and generous sizing.
 */
val Typography =
    Typography(
        // Massive Digital Clock
        displayLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraLight,
                fontSize = 112.sp,
                lineHeight = 116.sp,
                letterSpacing = (-4).sp,
            ),
        // Date Line below clock
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.5.sp,
            ),
        // App list labels - bolder and more readable
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.75.sp,
            ),
        // App category / Section headers
        labelMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 2.sp,
            ),
        // Secondary info
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp,
            ),
        // Search headers
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 1.5.sp,
            ),
    )
