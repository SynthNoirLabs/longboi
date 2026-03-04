package com.longboilauncher.app.core.designsystem.layout

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.shouldReduceMotion
import kotlinx.coroutines.delay

private val GridContentPadding = PaddingValues(16.dp)

/**
 * A grid layout component for displaying apps in a grid format
 * with staggered diagonal wave entrance animations.
 *
 * @param items List of items to display
 * @param modifier Modifier for the grid
 * @param columns Number of columns (defaults to 4)
 * @param animateEntrance Whether to animate item entrance (defaults to true)
 * @param itemKey Optional stable key provider for items
 * @param content Composable for rendering each item
 */
@Composable
fun <T> GridLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    animateEntrance: Boolean = true,
    itemKey: ((T) -> Any)? = null,
    content: @Composable (T) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = GridContentPadding,
    ) {
        if (itemKey != null) {
            itemsIndexed(items, key = { _, item -> itemKey(item) }) { index, item ->
                if (animateEntrance) {
                    AnimatedGridItem(
                        index = index,
                        columns = columns,
                        content = { content(item) },
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(4.dp)) { content(item) }
                }
            }
        } else {
            itemsIndexed(items) { index, item ->
                if (animateEntrance) {
                    AnimatedGridItem(
                        index = index,
                        columns = columns,
                        content = { content(item) },
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(4.dp)) { content(item) }
                }
            }
        }
    }
}

/**
 * Animated grid item with diagonal wave entrance effect.
 * Items animate in from top-left to bottom-right creating a wave pattern.
 */
@Composable
private fun AnimatedGridItem(
    index: Int,
    columns: Int,
    content: @Composable () -> Unit,
) {
    val reduceMotion = shouldReduceMotion()

    // Calculate row and column for diagonal wave delay
    val row = index / columns
    val col = index % columns
    // Diagonal distance creates wave from top-left to bottom-right
    val diagonalIndex = row + col

    var isVisible by remember { mutableStateOf(reduceMotion) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            // Staggered delay based on diagonal position
            delay(diagonalIndex * 40L)
        }
        isVisible = true
    }

    // Animated values - use snap when reduce motion is enabled
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 300),
        label = "gridItemAlpha",
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.6f,
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            },
        label = "gridItemScale",
    )

    val rotationX by animateFloatAsState(
        targetValue = if (isVisible || reduceMotion) 0f else -15f,
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            },
        label = "gridItemRotationX",
    )

    val translationY by animateFloatAsState(
        targetValue = if (isVisible || reduceMotion) 0f else 30f,
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            },
        label = "gridItemTranslationY",
    )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                    this.rotationX = if (reduceMotion) 0f else rotationX
                    this.translationY = translationY
                    // Add perspective for 3D effect
                    cameraDistance = 12f * density
                },
    ) {
        content()
    }
}
