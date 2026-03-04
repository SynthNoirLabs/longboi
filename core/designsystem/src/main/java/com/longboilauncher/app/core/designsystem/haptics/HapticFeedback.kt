package com.longboilauncher.app.core.designsystem.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Rich haptic feedback utilities for Longboi Launcher.
 * Provides various haptic patterns for different interactions.
 */
object LongboiHaptics {
    /**
     * Light tick for scrubber letter changes
     */
    fun tickLight(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    /**
     * Segment tick for scrubber - slightly stronger than light tick
     */
    fun tickSegment(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    /**
     * Confirm haptic for successful actions
     */
    fun confirm(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Reject haptic for failed/cancelled actions
     */
    fun reject(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Gesture start haptic
     */
    fun gestureStart(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    /**
     * Gesture end haptic
     */
    fun gestureEnd(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE)
        }
    }

    /**
     * Gesture threshold activated (e.g., swipe passed threshold)
     */
    fun gestureThreshold(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    /**
     * Toggle on haptic
     */
    fun toggleOn(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    /**
     * Toggle off haptic
     */
    fun toggleOff(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_OFF)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    /**
     * Long press haptic
     */
    fun longPress(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * Custom vibration pattern using VibrationEffect (for advanced haptics)
     */
    fun customVibration(
        context: Context,
        timings: LongArray,
        amplitudes: IntArray,
    ) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator.hasAmplitudeControl()) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Scrubber scroll pattern - creates a "bump" feeling when scrolling through letters
     */
    fun scrubberScroll(
        context: Context,
        intensity: Float = 1f,
    ) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val amplitude = (50 * intensity).toInt().coerceIn(1, 255)
            val effect = VibrationEffect.createOneShot(10, amplitude)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Elastic overscroll haptic - light bump when hitting bounds
     */
    fun elasticBounce(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            vibrator.vibrate(effect)
        }
    }

    /**
     * App launch haptic - satisfying click
     */
    fun appLaunch(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Double click effect
     */
    fun doubleClick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Heavy click for important actions
     */
    fun heavyClick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            vibrator.vibrate(effect)
        }
    }

    private fun getVibrator(context: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
}

/**
 * Compose-friendly haptic controller
 */
class HapticController(
    private val view: View,
    private val context: Context,
    private val hapticFeedback: HapticFeedback,
) {
    fun tickLight() = LongboiHaptics.tickLight(view)

    fun tickSegment() = LongboiHaptics.tickSegment(view)

    fun confirm() = LongboiHaptics.confirm(view)

    fun reject() = LongboiHaptics.reject(view)

    fun gestureStart() = LongboiHaptics.gestureStart(view)

    fun gestureEnd() = LongboiHaptics.gestureEnd(view)

    fun gestureThreshold() = LongboiHaptics.gestureThreshold(view)

    fun toggleOn() = LongboiHaptics.toggleOn(view)

    fun toggleOff() = LongboiHaptics.toggleOff(view)

    fun longPress() = LongboiHaptics.longPress(view)

    fun scrubberScroll(intensity: Float = 1f) = LongboiHaptics.scrubberScroll(context, intensity)

    fun elasticBounce() = LongboiHaptics.elasticBounce(context)

    fun appLaunch() = LongboiHaptics.appLaunch(context)

    fun doubleClick() = LongboiHaptics.doubleClick(context)

    fun heavyClick() = LongboiHaptics.heavyClick(context)

    fun performStandard(type: HapticFeedbackType) {
        hapticFeedback.performHapticFeedback(type)
    }
}

/**
 * Remember a HapticController for use in Compose
 */
@Composable
fun rememberHapticController(): HapticController {
    val view = LocalView.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    return remember(view, context, hapticFeedback) {
        HapticController(view, context, hapticFeedback)
    }
}
