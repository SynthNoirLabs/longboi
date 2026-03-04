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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.longboilauncher.app.core.designsystem.theme.shouldReduceMotion
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A curved alphabet scrubber that displays letters in an arc pattern,
 * with the currently selected letter emphasized in the center.
 *
 * Inspired by Niagara Launcher's signature curved letter navigation.
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
    letterSpacing: Dp = 18.dp,
    onScrubStateChanged: (active: Boolean, letter: String?) -> Unit = { _, _ -> },
) {
    val view = LocalView.current
    val reduceMotion = shouldReduceMotion()
    val density = LocalDensity.current

    val curveRadiusPx = with(density) { curveRadius.toPx() }
    val letterSpacingPx = with(density) { letterSpacing.toPx() }

    val allItems =
        remember(letters, showFavoriteStar) {
            if (showFavoriteStar) listOf("★") + letters else letters
        }

    var isDragging by remember { mutableStateOf(false) }
    var scrubberHeight by remember { mutableFloatStateOf(0f) }

    val activeIndex =
        remember(currentLetter, allItems) {
            allItems.indexOf(currentLetter).takeIf { it >= 0 }
                ?: if (showFavoriteStar && currentLetter == "★") {
                    0
                } else {
                    allItems.indexOfFirst { it == "A" }.coerceAtLeast(0)
                }
        }

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .pointerInput(allItems) {
                    if (allItems.isEmpty()) return@pointerInput

                    var lastIndex = -1
                    scrubberHeight = size.height.toFloat()

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
                curveRadiusPx = curveRadiusPx,
                isDragging = isDragging,
                reduceMotion = reduceMotion,
                onClick = {
                    onHapticTick(view)
                    onScrubStateChanged(true, letter)
                    onLetterSelected(letter)
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
    curveRadiusPx: Float,
    isDragging: Boolean,
    reduceMotion: Boolean,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
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
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            },
        label = "letterDistance",
    )

    val absDistance = abs(animatedDistance)

    // Niagara-style curve: sharp peak at active letter, smooth falloff
    val maxCurveOffsetPx = with(density) { 80.dp.toPx() } // Huge pull
    val curveOffsetX by animateFloatAsState(
        targetValue =
            if (reduceMotion || !isDragging) {
                0f
            } else {
                val progress = (absDistance / 6f).coerceIn(0f, 1f)
                // Cubic ease-out for a sharp peak that smoothly transitions back to origin
                val curveShape = (1f - progress) * (1f - progress) * (1f - progress)
                -curveShape * maxCurveOffsetPx
            },
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            },
        label = "curveX",
    )

    // Scale: active 1.3×, nearby 1.1×, far 1.0×
    val scale by animateFloatAsState(
        targetValue =
            when {
                reduceMotion -> if (absDistanceInt == 0) 1.2f else 1f
                absDistance < 0.5f -> 1.3f
                absDistance < 2f -> 1.15f - (absDistance - 0.5f) * 0.06f
                else -> 1f
            },
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            },
        label = "letterScale",
    )

    // Alpha: strong for nearby, 0.7 floor so all letters are vibrant
    val alpha by animateFloatAsState(
        targetValue =
            when {
                absDistance < 0.5f -> 1f
                absDistance < 2f -> 0.9f
                absDistance < 5f -> 0.8f
                else -> 0.7f
            },
        animationSpec =
            if (reduceMotion) {
                snap()
            } else {
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
            },
        label = "letterAlpha",
    )

    val centerOffset = (totalItems - 1) / 2f
    val yOffset = (index - centerOffset) * letterSpacingPx

    val isActive = absDistanceInt == 0
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val textColor = Color.White

    Box(
        modifier =
            Modifier
                .offset { IntOffset(x = curveOffsetX.roundToInt(), y = yOffset.roundToInt()) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }.then(
                    if (isActive && isDragging) {
                        Modifier
                            .size(28.dp)
                            .background(primaryColor, CircleShape)
                    } else {
                        Modifier.padding(2.dp)
                    },
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style =
                androidx.compose.ui.text.TextStyle(
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.85f), // Stronger shadow
                            offset = Offset(0f, 3f),
                            blurRadius = 6f,
                        ),
                ),
            fontSize =
                when {
                    isActive -> 22.sp
                    absDistanceInt < 3 -> 17.sp
                    else -> 15.sp
                },
            fontWeight =
                when {
                    isActive -> FontWeight.Bold
                    absDistanceInt < 2 -> FontWeight.Bold
                    else -> FontWeight.Bold
                },
            color =
                if (isActive && isDragging) {
                    onPrimaryColor
                } else {
                    textColor.copy(alpha = alpha)
                },
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
    Box(
        modifier =
            modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

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
) {
    CurvedAlphabetScrubber(
        letters = letters,
        currentLetter = currentLetter,
        onHapticTick = onHapticTick,
        onLetterSelected = onLetterSelected,
        modifier = modifier,
        showFavoriteStar = true,
        curveRadius = 250.dp,
        letterSpacing = 24.dp, // Safely spread across screen without clipping
        onScrubStateChanged = onScrubStateChanged,
    )
}
