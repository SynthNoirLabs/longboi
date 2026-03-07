package com.longboilauncher.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.longboilauncher.app.core.model.ThemeType

@Composable
fun ThemeBackground(
    themeType: ThemeType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (themeType) {
        ThemeType.GLASSMORPHISM -> {
            AmbientLightBackground(
                modifier = modifier.fillMaxSize(),
                primaryColor = Color(0xFFBBDEFB), // Soft blue
                secondaryColor = Color(0xFFE1BEE7), // Soft purple
                intensity = 0.15f,
            ) {
                content()
            }
        }
        ThemeType.VIBRANT_PLAYFUL -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFCCBC), // Orange
                                        Color(0xFFFCE4EC), // Pink
                                        Color(0xFFE1F5FE), // Blue
                                    ),
                            ),
                        ),
            ) {
                content()
            }
        }
        ThemeType.SOPHISTICATED_SLEEK -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(Color.Black),
            ) {
                content()
            }
        }
        else -> {
            Box(modifier = modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
