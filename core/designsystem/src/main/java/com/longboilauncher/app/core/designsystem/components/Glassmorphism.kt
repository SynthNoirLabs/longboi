package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated ambient light background effect that creates a subtle, breathing gradient behind
 * content. Inspired by iOS and premium launcher designs.
 */
@Composable
fun AmbientLightBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    animationDurationMs: Int = 8000,
    intensity: Float = 0.08f,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientLight")

    val animationProgress by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis = animationDurationMs,
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "lightProgress",
        )

    val colors =
        remember(primaryColor, secondaryColor, intensity) {
            listOf(
                primaryColor.copy(alpha = intensity * 1.5f),
                secondaryColor.copy(alpha = intensity * 0.8f),
                primaryColor.copy(alpha = intensity * 0.5f),
                Color.Transparent,
                Color.Transparent,
            )
        }

    Box(
        modifier =
            modifier.drawBehind {
                val lightSourceX =
                    size.width * 0.85f +
                        cos(animationProgress * 2 * Math.PI).toFloat() *
                        size.width *
                        0.03f
                val lightSourceY =
                    size.height * 0.15f +
                        sin(animationProgress * 2 * Math.PI * 0.7f).toFloat() *
                        size.height *
                        0.02f

                // Primary light source
                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors = colors,
                            center = Offset(lightSourceX, lightSourceY),
                            radius = size.width * 1.2f,
                        ),
                )

                // Secondary light source (opposite corner)
                val secondaryX =
                    size.width * 0.15f +
                        sin(animationProgress * 2 * Math.PI).toFloat() *
                        size.width *
                        0.02f
                val secondaryY =
                    size.height * 0.85f +
                        cos(animationProgress * 2 * Math.PI * 0.5f).toFloat() *
                        size.height *
                        0.015f

                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors =
                                colors.map {
                                    it.copy(alpha = it.alpha * 0.5f)
                                },
                            center = Offset(secondaryX, secondaryY),
                            radius = size.width * 0.8f,
                        ),
                )
            },
    ) { content() }
}

/**
 * A subtle glass-like surface with frosted appearance. Uses a semi-transparent background with
 * subtle border.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    backgroundAlpha: Float = 0.7f,
    tintAlpha: Float = 0.05f,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier.background(
                Brush.verticalGradient(
                    colors =
                        listOf(
                            backgroundColor.copy(alpha = backgroundAlpha),
                            backgroundColor.copy(
                                alpha = backgroundAlpha * 0.9f,
                            ),
                            tintColor.copy(alpha = tintAlpha),
                        ),
                ),
            ),
    ) { content() }
}

/**
 * Modifier extension for adding a subtle glow effect behind content. Used for press states and
 * focused elements.
 */
fun Modifier.glowEffect(
    color: Color,
    alpha: Float = 0.15f,
    radius: Float = 0.6f,
): Modifier =
    this.drawBehind {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.maxDimension * radius,
            center = center,
        )
    }
