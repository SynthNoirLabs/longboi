package com.longboilauncher.app.core.designsystem.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.delay

/**
 * Creates and remembers a [FocusRequester] that will request focus after a short delay.
 * Useful for dialogs, bottom sheets, and new screens where initial focus should be set
 * for accessibility (TalkBack) users.
 *
 * @param delayMs Delay before requesting focus. Default is 100ms to allow layout to settle.
 * @param key Optional key to trigger re-focus when changed.
 * @return A [FocusRequester] that can be attached via [Modifier.focusRequester].
 */
@Composable
fun rememberInitialFocusRequester(
    delayMs: Long = 100L,
    key: Any? = Unit,
): FocusRequester {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key) {
        delay(delayMs)
        try {
            focusRequester.requestFocus()
        } catch (_: IllegalStateException) {
            // FocusRequester not attached yet, ignore
        }
    }

    return focusRequester
}

/**
 * Modifier that requests focus when the composable enters composition.
 * Useful for setting initial focus on dialogs, sheets, or new screens.
 *
 * @param delayMs Delay before requesting focus. Default is 100ms.
 * @param key Optional key to trigger re-focus when changed.
 */
@Composable
fun Modifier.requestInitialFocus(
    delayMs: Long = 100L,
    key: Any? = Unit,
): Modifier {
    val focusRequester = rememberInitialFocusRequester(delayMs, key)
    return this.focusRequester(focusRequester)
}

/**
 * Modifier that marks a composable as a polite live region.
 * Changes to this region will be announced by TalkBack without interrupting current speech.
 * Use for progress updates, status changes, or non-critical notifications.
 */
fun Modifier.liveRegionPolite(): Modifier =
    this.semantics {
        liveRegion = LiveRegionMode.Polite
    }

/**
 * Modifier that marks a composable as an assertive live region.
 * Changes to this region will immediately interrupt TalkBack and be announced.
 * Use sparingly for critical alerts or errors that need immediate attention.
 */
fun Modifier.liveRegionAssertive(): Modifier =
    this.semantics {
        liveRegion = LiveRegionMode.Assertive
    }

/**
 * Composable helper that announces text changes to TalkBack users via a polite live region.
 * The announcement won't interrupt current speech.
 *
 * @param text The text to announce when it changes.
 * @param modifier Optional modifier for the announcement container.
 */
@Composable
fun AccessibilityAnnouncement(
    text: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                },
    ) {
        androidx.compose.material3.Text(
            text = text,
            modifier =
                Modifier.semantics {
                    // The text content will be announced when it changes
                },
            // Make invisible but keep in accessibility tree
            color = androidx.compose.ui.graphics.Color.Transparent,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Modifier that makes an element focusable for keyboard/D-pad navigation
 * with proper visual feedback via the interaction source.
 */
@Composable
fun Modifier.accessibleFocusable(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.focusable(interactionSource = interactionSource)
}

/**
 * Modifier that handles D-pad/keyboard navigation for horizontal lists.
 * Moves focus left/right on arrow keys.
 *
 * @param focusManager The focus manager to use for navigation.
 */
fun Modifier.horizontalKeyboardNavigation(focusManager: FocusManager): Modifier =
    this.onKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {
                Key.DirectionLeft -> {
                    focusManager.moveFocus(FocusDirection.Left)
                    true
                }
                Key.DirectionRight -> {
                    focusManager.moveFocus(FocusDirection.Right)
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }

/**
 * Modifier that handles D-pad/keyboard navigation for vertical lists.
 * Moves focus up/down on arrow keys.
 *
 * @param focusManager The focus manager to use for navigation.
 */
fun Modifier.verticalKeyboardNavigation(focusManager: FocusManager): Modifier =
    this.onKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {
                Key.DirectionUp -> {
                    focusManager.moveFocus(FocusDirection.Up)
                    true
                }
                Key.DirectionDown -> {
                    focusManager.moveFocus(FocusDirection.Down)
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }

/**
 * Modifier that handles D-pad/keyboard navigation for grid layouts.
 * Moves focus in all four directions on arrow keys.
 *
 * @param focusManager The focus manager to use for navigation.
 */
fun Modifier.gridKeyboardNavigation(focusManager: FocusManager): Modifier =
    this.onKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {
                Key.DirectionUp -> {
                    focusManager.moveFocus(FocusDirection.Up)
                    true
                }
                Key.DirectionDown -> {
                    focusManager.moveFocus(FocusDirection.Down)
                    true
                }
                Key.DirectionLeft -> {
                    focusManager.moveFocus(FocusDirection.Left)
                    true
                }
                Key.DirectionRight -> {
                    focusManager.moveFocus(FocusDirection.Right)
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }

/**
 * Modifier that handles Enter/Space key to trigger click action.
 * Essential for keyboard accessibility on custom clickable elements.
 *
 * @param onClick The action to perform on Enter/Space key press.
 */
fun Modifier.keyboardClickable(onClick: () -> Unit): Modifier =
    this.onKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {
                Key.Enter, Key.Spacebar -> {
                    onClick()
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }
