package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.longboilauncher.app.core.designsystem.theme.LongboiMotion
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Wrapper for staggered entrance animations. Each item fades in and scales up with a delay based on
 * its index.
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerItem: Long = 50L,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * delayPerItem)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = LongboiMotion.Duration.MEDIUM2,
                        easing = LongboiMotion.Easing.StandardDecelerate,
                    ),
            ) +
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                ),
    ) { content() }
}

/** Enhanced press state modifier with glow and micro-tilt effects. */
@Composable
fun Modifier.enhancedPressEffect(
    isPressed: Boolean,
    enableTilt: Boolean = true,
    enableGlow: Boolean = true,
): Modifier {
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "pressScale",
        )

    val tiltAngle by
        animateFloatAsState(
            targetValue =
                if (isPressed && enableTilt) {
                    Random.nextFloat() * 1.5f - 0.75f
                } else {
                    0f
                },
            animationSpec =
                spring(
                    dampingRatio = 0.4f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            label = "tiltAngle",
        )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = tiltAngle
    }
}

/** Pulsing animation for notification badges or attention indicators. */
@Composable
fun PulsingBadge(
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")

    val pulseScale by
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pulseScale",
        )

    Box(
        modifier = modifier.scale(if (isPulsing) pulseScale else 1f),
    ) { content() }
}

/** 3D flip animation for individual characters (clock digits). */
@Composable
fun FlipAnimatedDigit(
    digit: Char,
    modifier: Modifier = Modifier,
    content: @Composable (Char) -> Unit,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(digit) {
        rotation.animateTo(
            targetValue = 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
        )
    }

    Box(
        modifier =
            modifier.graphicsLayer {
                rotationX = rotation.value
                cameraDistance = 12f * density
            },
    ) { content(digit) }
}

/** Elastic overscroll effect for scrollable components. */
@Composable
fun elasticOverscrollOffset(
    currentIndex: Int,
    maxIndex: Int,
    dampingFactor: Float = 8f,
): Float {
    val elasticOffset by
        animateFloatAsState(
            targetValue =
                when {
                    currentIndex < 0 -> currentIndex * dampingFactor
                    currentIndex > maxIndex -> (currentIndex - maxIndex) * dampingFactor
                    else -> 0f
                },
            animationSpec =
                spring(
                    dampingRatio = 0.5f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            label = "elasticOffset",
        )
    return elasticOffset
}
