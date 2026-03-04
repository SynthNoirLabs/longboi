package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect

/**
 * CompositionLocal providing access to SharedTransitionScope for shared element transitions.
 * Use this to coordinate shared element animations between different composables.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * CompositionLocal providing access to AnimatedVisibilityScope for shared element transitions.
 */
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Shared element key generator for app icons.
 * Use this to create consistent keys between source and target shared elements.
 */
object SharedElementKeys {
    fun appIcon(
        packageName: String,
        userIdentifier: Long = 0L,
    ): String = "app_icon_${packageName}_$userIdentifier"

    fun appLabel(
        packageName: String,
        userIdentifier: Long = 0L,
    ): String = "app_label_${packageName}_$userIdentifier"

    fun appContainer(
        packageName: String,
        userIdentifier: Long = 0L,
    ): String = "app_container_${packageName}_$userIdentifier"
}

/**
 * Default bounds transform for shared element animations with spring physics.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val DefaultBoundsTransform =
    BoundsTransform { _: Rect, _: Rect ->
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }

/**
 * Wrapper composable that provides SharedTransitionScope to its children.
 * Use this at the root of screens that need shared element transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionProvider(content: @Composable SharedTransitionScope.() -> Unit) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            content()
        }
    }
}

/**
 * Extension function to apply shared element modifier when scope is available.
 * Falls back to identity modifier when shared transition scope is not provided.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementWithKey(
    key: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
): Modifier =
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@sharedElementWithKey.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
            )
        }
    } else {
        this
    }

/**
 * Extension function to apply shared bounds modifier when scope is available.
 * Use for elements that change size/shape between states.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsWithKey(
    key: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
): Modifier =
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@sharedBoundsWithKey.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
            )
        }
    } else {
        this
    }
