package com.longboilauncher.app.core.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.platform.testTag
import com.longboilauncher.app.core.common.LauncherGestureHandler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class LauncherGestureHandlerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gestureHandler_detectsSwipeUp() {
        val onSwipeUp = mockk<() -> Unit>(relaxed = true)
        val handler = LauncherGestureHandler(onSwipeUp = onSwipeUp)

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("gesture_box")
                    .with(handler) { handleGestures() }
            )
        }

        composeTestRule.onNodeWithTag("gesture_box").performTouchInput {
            swipeUp()
        }

        verify { onSwipeUp() }
    }

    @Test
    fun gestureHandler_detectsSwipeDown() {
        val onSwipeDown = mockk<() -> Unit>(relaxed = true)
        val handler = LauncherGestureHandler(onSwipeDown = onSwipeDown)

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("gesture_box")
                    .with(handler) { handleGestures() }
            )
        }

        composeTestRule.onNodeWithTag("gesture_box").performTouchInput {
            swipeDown()
        }

        verify { onSwipeDown() }
    }

    @Test
    fun gestureHandler_detectsDoubleTap() {
        val onDoubleTap = mockk<() -> Unit>(relaxed = true)
        val handler = LauncherGestureHandler(onDoubleTap = onDoubleTap)

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("gesture_box")
                    .with(handler) { handleGestures() }
            )
        }

        composeTestRule.onNodeWithTag("gesture_box").performTouchInput {
            doubleClick()
        }

        verify { onDoubleTap() }
    }

    @Test
    fun gestureHandler_detectsLongPress() {
        val onLongPress = mockk<() -> Unit>(relaxed = true)
        val handler = LauncherGestureHandler(onLongPress = onLongPress)

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("gesture_box")
                    .with(handler) { handleGestures() }
            )
        }

        composeTestRule.onNodeWithTag("gesture_box").performTouchInput {
            longClick()
        }

        verify { onLongPress() }
    }

    private fun Modifier.with(handler: LauncherGestureHandler, block: LauncherGestureHandler.() -> Modifier): Modifier {
        return handler.block()
    }
}
