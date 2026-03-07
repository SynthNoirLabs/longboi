package com.longboilauncher.app.core.designsystem.components

import android.view.View
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.longboilauncher.app.core.designsystem.theme.LocalLongboiColors
import com.longboilauncher.app.core.designsystem.theme.shouldReduceMotion
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Premium Curved Alphabet Scrubber.
 * Matches the cardless, minimalist aesthetic of the redesign.
 */
@Composable
fun CurvedAlphabetScrubber(
    letters: List<String>,
    currentLetter: String,
    onHapticTick: (View) -> Unit,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteStar: Boolean = true,
    curveRadius: Dp = 300.dp,
    letterSpacing: Dp = 20.dp,
    onScrubStateChanged: (active: Boolean, letter: String?) -> Unit = { _, _ -> },
    onLetterConfirmed: (String) -> Unit = {},
) {
    val view = LocalView.current
    val reduceMotion = shouldReduceMotion()
    val density = LocalDensity.current

    val allItems =
        remember(letters, showFavoriteStar) {
            if (showFavoriteStar) listOf("★") + letters else letters
        }

    var isDragging by remember { mutableStateOf(false) }
    var lastLetter by remember { mutableStateOf<String?>(null) }

    val activeIndex =
        remember(currentLetter, allItems) {
            val idx = allItems.indexOf(currentLetter)
            if (idx >= 0) idx else allItems.indexOf("A").coerceAtLeast(0)
        }

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .pointerInput(allItems) {
                    if (allItems.isEmpty()) return@pointerInput

                    var lastIndex = -1
                    val scrubberHeight = size.height.toFloat()

                    fun calculateIndex(y: Float): Int {
                        if (allItems.isEmpty() || scrubberHeight <= 0f) return 0
                        val clampedY = y.coerceIn(0f, scrubberHeight)
                        val ratio = (clampedY / scrubberHeight).coerceIn(0f, 1f)
                        return (ratio * allItems.size).toInt().coerceIn(0, allItems.size - 1)
                    }

                    fun selectIndex(index: Int) {
                        if (index == lastIndex) return
                        lastIndex = index
                        val letter = allItems[index]
                        lastLetter = letter
                        onScrubStateChanged(true, letter)
                        onHapticTick(view)
                        onLetterSelected(letter)
                    }

                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            selectIndex(calculateIndex(offset.y))
                        },
                        onDragCancel = {
                            isDragging = false
                            onScrubStateChanged(false, null)
                        },
                        onDragEnd = {
                            isDragging = false
                            lastLetter?.let { onLetterConfirmed(it) }
                            onScrubStateChanged(false, null)
                        },
                        onDrag = { change, _ ->
                            selectIndex(calculateIndex(change.position.y))
                        },
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        allItems.forEachIndexed { index, letter ->
            CurvedLetter(
                letter = letter,
                index = index,
                activeIndex = activeIndex,
                totalItems = allItems.size,
                letterSpacing = letterSpacing,
                isDragging = isDragging,
                reduceMotion = reduceMotion,
                onClick = {
                    onHapticTick(view)
                    onScrubStateChanged(true, letter)
                    onLetterSelected(letter)
                    onLetterConfirmed(letter)
                    onScrubStateChanged(false, null)
                },
            )
        }
    }
}

@Composable
private fun CurvedLetter(
    letter: String,
    index: Int,
    activeIndex: Int,
    totalItems: Int,
    letterSpacing: Dp,
    isDragging: Boolean,
    reduceMotion: Boolean,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val customColors = LocalLongboiColors.current
    val letterSpacingPx = with(density) { letterSpacing.toPx() }

    val distanceFromActive = index - activeIndex
    val absDistanceInt = abs(distanceFromActive)

    val animatedDistance by animateFloatAsState(
        targetValue = distanceFromActive.toFloat(),
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            },
        label = "letterDistance",
    )

    val absDistance = abs(animatedDistance)

    // Wave Pull: Letters near thumb pull towards the center of the screen
    val maxCurveOffsetPx = with(density) { 64.dp.toPx() }
    val curveOffsetX by animateFloatAsState(
        targetValue =
            if (reduceMotion || !isDragging) {
                0f
            } else {
                val progress = (absDistance / 5f).coerceIn(0f, 1f)
                val curveShape = (1f - progress) * (1f - progress) * (1f - progress)
                -curveShape * maxCurveOffsetPx
            },
        label = "curveX",
    )

    // Scale and Alpha logic
    val scale by animateFloatAsState(
        targetValue =
            when {
                absDistanceInt == 0 -> 1.25f
                absDistance < 2f -> 1.1f
                else -> 1f
            },
        label = "letterScale",
    )

    val alpha by animateFloatAsState(
        targetValue =
            when {
                absDistanceInt == 0 -> 1f
                absDistance < 3f -> 0.7f
                else -> 0.4f
            },
        label = "letterAlpha",
    )

    val centerOffset = (totalItems - 1) / 2f
    val yOffset = (index - centerOffset) * letterSpacingPx

    val isActive = absDistanceInt == 0
    val highlightColor = MaterialTheme.colorScheme.primary
    val onHighlightColor = MaterialTheme.colorScheme.onPrimary
    val textColor = customColors.onWallpaperContent
    val shadowColor = if (textColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier =
            Modifier
                .offset { IntOffset(x = curveOffsetX.roundToInt(), y = yOffset.roundToInt()) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }.size(24.dp)
                .then(
                    if (isActive) {
                        Modifier.background(highlightColor, CircleShape)
                    } else {
                        Modifier
                    },
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style =
                androidx.compose.ui.text.TextStyle(
                    shadow =
                        if (!isActive) {
                            Shadow(
                                color = shadowColor.copy(alpha = 0.3f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f,
                            )
                        } else {
                            null
                        },
                ),
            fontSize = if (isActive) 14.sp else 12.sp,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            color = if (isActive) onHighlightColor else textColor.copy(alpha = alpha),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Floating letter bubble shown in the centre of the screen while scrubbing.
 */
@Composable
fun FloatingLetterIndicator(
    letter: String,
    modifier: Modifier = Modifier,
) {
    val customColors = LocalLongboiColors.current
    val bgColor = customColors.onWallpaperContent
    val textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier =
            modifier
                .size(72.dp)
                .background(
                    color = bgColor,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

/**
 * Compact version of the curved scrubber for home screen use.
 */
@Composable
fun CompactCurvedAlphabetScrubber(
    letters: List<String>,
    currentLetter: String,
    onHapticTick: (View) -> Unit,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onScrubStateChanged: (active: Boolean, letter: String?) -> Unit = { _, _ -> },
    onLetterConfirmed: (String) -> Unit = {},
) {
    CurvedAlphabetScrubber(
        letters = letters,
        currentLetter = currentLetter,
        onHapticTick = onHapticTick,
        onLetterSelected = onLetterSelected,
        modifier = modifier,
        showFavoriteStar = true,
        letterSpacing = 24.dp,
        onScrubStateChanged = onScrubStateChanged,
        onLetterConfirmed = onLetterConfirmed,
    )
}
