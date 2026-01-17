package com.longboilauncher.app.core.common

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

class LauncherGestureHandler(
    private val onSwipeUp: () -> Unit = {},
    private val onSwipeDown: () -> Unit = {},
    private val onDoubleTap: () -> Unit = {},
    private val onLongPress: () -> Unit = {}
) {
    private val swipeThreshold = 50.dp
    private val doubleTapTimeout = 300L

    fun Modifier.handleGestures(): Modifier = this.pointerInput(Unit) {
        var isDragging = false
        var dragStartY = 0f

        detectVerticalDragGestures(
            onDragStart = { offset ->
                isDragging = true
                dragStartY = offset.y
            },
            onDragEnd = {
                isDragging = false
            }
        ) { change, dragAmount ->
            val totalDrag = change.position.y - dragStartY

            if (!isDragging) return@detectVerticalDragGestures

            // Check for swipe gestures
            if (totalDrag.absoluteValue > swipeThreshold.toPx()) {
                if (totalDrag < 0) {
                    onSwipeUp()
                } else {
                    onSwipeDown()
                }
                isDragging = false
            }
        }
    }.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { onDoubleTap() },
            onLongPress = { onLongPress() }
        )
    }
}

@Composable
fun Modifier.handleGestures(
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onLongPress: () -> Unit = {}
): Modifier {
    val handler = remember {
        LauncherGestureHandler(onSwipeUp, onSwipeDown, onDoubleTap, onLongPress)
    }
    return with(handler) { handleGestures() }
}
