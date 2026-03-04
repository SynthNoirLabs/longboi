package com.longboilauncher.app.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Motion tokens for Longboi Launcher.
 * Based on Material 3 Expressive motion guidelines with launcher-specific tuning.
 *
 * M3 Expressive emphasizes:
 * - Physics-based animations with natural feel
 * - Expressive springs with personality
 * - Spatial relationships through motion
 * - Continuous, fluid transitions
 */
@Stable
object LongboiMotion {
    // Duration tokens (milliseconds) - M3 Expressive timing
    object Duration {
        const val SHORT1 = 50
        const val SHORT2 = 100
        const val SHORT3 = 150
        const val SHORT4 = 200
        const val MEDIUM1 = 250
        const val MEDIUM2 = 300
        const val MEDIUM3 = 350
        const val MEDIUM4 = 400
        const val LONG1 = 450
        const val LONG2 = 500
        const val LONG3 = 550
        const val LONG4 = 600
        const val EXTRA_LONG1 = 700
        const val EXTRA_LONG2 = 800
        const val EXTRA_LONG3 = 900
        const val EXTRA_LONG4 = 1000
    }

    // Easing tokens - Material 3 Expressive curves
    object Easing {
        // Standard easing for most transitions
        val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val StandardDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
        val StandardAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

        // Emphasized easing for important transitions
        val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

        // M3 Expressive - More dramatic curves for expressive UI
        val Expressive = CubicBezierEasing(0.1f, 0.9f, 0.2f, 1f)
        val ExpressiveDecelerate = CubicBezierEasing(0f, 0.8f, 0.1f, 1f)
        val ExpressiveAccelerate = CubicBezierEasing(0.4f, 0f, 0.9f, 0.1f)

        // Legacy for compatibility
        val FastOutSlowIn = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        val LinearOutSlowIn = CubicBezierEasing(0f, 0f, 0.2f, 1f)
        val FastOutLinearIn = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    }

    // Spring configurations - M3 Expressive physics-based animations
    object Springs {
        // Quick, snappy interactions
        val Snappy =
            spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )

        // Smooth, gentle transitions
        val Gentle =
            spring<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            )

        // Bouncy, playful feedback - M3 Expressive signature
        val Bouncy =
            spring<Float>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            )

        // Fast response for scrubber/gestures
        val Responsive =
            spring<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh,
            )

        // M3 Expressive springs - more personality
        val ExpressiveBouncy =
            spring<Float>(
                dampingRatio = 0.5f, // More bounce than standard
                stiffness = 300f,
            )

        val ExpressiveSnap =
            spring<Float>(
                dampingRatio = 0.7f,
                stiffness = 800f,
            )

        val ExpressiveFloat =
            spring<Float>(
                dampingRatio = 0.8f,
                stiffness = 150f,
            )

        // Elastic spring for overscroll effects
        val Elastic =
            spring<Float>(
                dampingRatio = 0.4f,
                stiffness = 200f,
            )

        // Wobbly spring for playful micro-interactions
        val Wobbly =
            spring<Float>(
                dampingRatio = 0.35f,
                stiffness = 400f,
            )
    }

    // Pre-built animation specs
    object Specs {
        // Surface transitions (home <-> all apps)
        fun <T> surfaceEnter() =
            tween<T>(
                durationMillis = Duration.MEDIUM2,
                easing = Easing.EmphasizedDecelerate,
            )

        fun <T> surfaceExit() =
            tween<T>(
                durationMillis = Duration.SHORT4,
                easing = Easing.EmphasizedAccelerate,
            )

        // Item animations (list items, favorites)
        fun <T> itemEnter(index: Int = 0) =
            tween<T>(
                durationMillis = Duration.MEDIUM1,
                delayMillis = index * 30,
                easing = Easing.EmphasizedDecelerate,
            )

        // Fade animations
        fun <T> fadeIn() =
            tween<T>(
                durationMillis = Duration.SHORT3,
                easing = Easing.StandardDecelerate,
            )

        fun <T> fadeOut() =
            tween<T>(
                durationMillis = Duration.SHORT2,
                easing = Easing.StandardAccelerate,
            )

        // Slide animations for transitions
        fun <T> slideIn() =
            tween<T>(
                durationMillis = Duration.MEDIUM1,
                easing = Easing.EmphasizedDecelerate,
            )

        fun <T> slideOut() =
            tween<T>(
                durationMillis = Duration.SHORT3,
                easing = Easing.EmphasizedAccelerate,
            )

        // Scale animations for press states
        fun <T> pressScale() =
            spring<T>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )

        // M3 Expressive specs
        fun <T> expressiveEnter() =
            tween<T>(
                durationMillis = Duration.MEDIUM3,
                easing = Easing.ExpressiveDecelerate,
            )

        fun <T> expressiveExit() =
            tween<T>(
                durationMillis = Duration.SHORT4,
                easing = Easing.ExpressiveAccelerate,
            )

        // Morphing animations for shape changes
        fun <T> morphTransform() =
            spring<T>(
                dampingRatio = 0.6f,
                stiffness = 350f,
            )

        // Container transform (shared element style)
        fun <T> containerTransform() =
            tween<T>(
                durationMillis = Duration.MEDIUM4,
                easing = Easing.Expressive,
            )

        // Staggered item animations with M3 Expressive timing
        fun <T> staggeredItemEnter(index: Int = 0) =
            tween<T>(
                durationMillis = Duration.MEDIUM2,
                delayMillis = index * 40,
                easing = Easing.ExpressiveDecelerate,
            )

        // Elastic overscroll
        fun <T> elasticBounce() =
            spring<T>(
                dampingRatio = 0.4f,
                stiffness = 200f,
            )

        // Flip animation (for clock digits, cards)
        fun <T> flip3D() =
            tween<T>(
                durationMillis = Duration.MEDIUM1,
                easing = Easing.Emphasized,
            )
    }
}

/** Spacing tokens with animation-friendly values. */
@Stable
object LongboiSpacing {
    val XXS: Dp = 2.dp
    val XS: Dp = 4.dp
    val S: Dp = 8.dp
    val M: Dp = 12.dp
    val L: Dp = 16.dp
    val XL: Dp = 24.dp
    val XXL: Dp = 32.dp
    val XXXL: Dp = 48.dp
}

/** Corner radius tokens. */
@Stable
object LongboiCorners {
    val None: Dp = 0.dp
    val XS: Dp = 4.dp
    val S: Dp = 8.dp
    val M: Dp = 12.dp
    val L: Dp = 16.dp
    val XL: Dp = 24.dp
    val XXL: Dp = 32.dp
    val Full: Dp = 1000.dp // For circles
}

val LocalReduceMotion = staticCompositionLocalOf { false }

/** Utility to check if reduced motion is enabled. */
@Composable fun shouldReduceMotion(): Boolean = LocalReduceMotion.current
