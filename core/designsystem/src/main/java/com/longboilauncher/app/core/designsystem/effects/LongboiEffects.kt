package com.longboilauncher.app.core.designsystem.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Floating particles ambient effect - creates a dreamy, premium feel
 */
@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 15,
    particleColor: Color = Color.White.copy(alpha = 0.15f),
    maxRadius: Float = 4f,
) {
    data class Particle(
        val x: Float,
        val y: Float,
        val radius: Float,
        val speed: Float,
        val angle: Float,
        val alpha: Float,
    )

    var particles by remember {
        mutableStateOf(
            List(particleCount) {
                Particle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    radius = Random.nextFloat() * maxRadius + 1f,
                    speed = Random.nextFloat() * 0.0003f + 0.0001f,
                    angle = Random.nextFloat() * 360f,
                    alpha = Random.nextFloat() * 0.3f + 0.05f,
                )
            },
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(100000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "particle_time",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val offsetX = sin((time + particle.angle) * particle.speed * PI.toFloat()) * 50f
            val offsetY = cos((time + particle.angle) * particle.speed * PI.toFloat() * 0.7f) * 30f

            val x = (particle.x * size.width + offsetX).mod(size.width)
            val y = (particle.y * size.height + offsetY).mod(size.height)

            drawCircle(
                color = particleColor.copy(alpha = particle.alpha),
                radius = particle.radius,
                center = Offset(x, y),
            )
        }
    }
}

/**
 * Staggered slide-in animation with spring physics
 */
@Composable
fun StaggeredSlideIn(
    index: Int,
    totalItems: Int,
    delayPerItem: Long = 40L,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * delayPerItem)
        visible = true
    }

    val offsetX by animateFloatAsState(
        targetValue = if (visible) 0f else -80f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "slide_x_$index",
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "alpha_$index",
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "scale_$index",
    )

    Box(
        modifier =
            Modifier.graphicsLayer {
                translationX = offsetX
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            },
    ) {
        content()
    }
}

/**
 * Glow effect modifier for highlighting elements
 */
fun Modifier.glowEffect(
    color: Color,
    radius: Float = 20f,
    alpha: Float = 0.4f,
): Modifier =
    this.drawBehind {
        for (i in 3 downTo 1) {
            drawCircle(
                color = color.copy(alpha = alpha / i),
                radius = radius * i,
                center = center,
            )
        }
    }

/**
 * Pulsing glow animation for notifications or highlights
 */
@Composable
fun PulsingGlow(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    baseRadius: Float = 8f,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse_scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse_alpha",
    )

    Box(
        modifier =
            modifier.drawBehind {
                drawCircle(
                    color = color.copy(alpha = pulseAlpha),
                    radius = baseRadius * pulseScale,
                    center = center,
                )
            },
    ) {
        content()
    }
}

/**
 * Elastic overscroll effect for lists
 */
@Composable
fun rememberElasticState(): ElasticState = remember { ElasticState() }

class ElasticState {
    private val _offset = Animatable(0f)
    val offset: Float get() = _offset.value

    suspend fun stretch(delta: Float) {
        val resistance = 0.3f
        _offset.snapTo(_offset.value + delta * resistance)
    }

    suspend fun release() {
        _offset.animateTo(
            targetValue = 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
        )
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    baseColor: Color = Color.White.copy(alpha = 0.1f),
    highlightColor: Color = Color.White.copy(alpha = 0.3f),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerPosition by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmer_pos",
    )

    Canvas(modifier = modifier) {
        val brush =
            Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(size.width * shimmerPosition, 0f),
                end = Offset(size.width * (shimmerPosition + 1f), size.height),
            )
        drawRect(brush = brush)
    }
}

/**
 * Breathing/ambient color shift effect
 */
@Composable
fun BreathingGradient(
    modifier: Modifier = Modifier,
    color1: Color = Color(0xFF1A1A2E),
    color2: Color = Color(0xFF16213E),
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "breathe",
    )

    Box(
        modifier =
            modifier.drawBehind {
                val interpolatedColor =
                    androidx.compose.ui.graphics
                        .lerp(color1, color2, breathePhase)
                drawRect(interpolatedColor)
            },
    ) {
        content()
    }
}

/**
 * Ripple expand effect for app launches
 */
@Composable
fun LaunchRipple(
    isLaunching: Boolean,
    color: Color = Color.White,
    onComplete: () -> Unit = {},
) {
    var rippleProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isLaunching) {
        if (isLaunching) {
            val animatable = Animatable(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
            )
            onComplete()
        }
        rippleProgress = 0f
    }

    if (isLaunching && rippleProgress < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = color.copy(alpha = 0.3f * (1f - rippleProgress)),
                radius = size.maxDimension * rippleProgress,
                center = center,
            )
        }
    }
}
