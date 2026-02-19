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
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color(0xFFBBDEFB), // Soft blue
                                        Color(0xFFE1BEE7), // Soft purple
                                    ),
                            ),
                        ),
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
