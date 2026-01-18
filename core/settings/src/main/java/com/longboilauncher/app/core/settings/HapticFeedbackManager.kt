package com.longboilauncher.app.core.settings

import android.view.HapticFeedbackConstants
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized haptic feedback manager that respects user settings.
 * Provides a simple API for triggering haptics while honoring hapticsEnabled and reduceMotion preferences.
 */
@Singleton
class HapticFeedbackManager
    @Inject
    constructor(
        preferencesRepository: PreferencesRepository,
        private val scope: CoroutineScope,
    ) : HapticFeedbackInterface {
        @Volatile
        private var shouldProvideHaptics: Boolean = true // Default to true until settings load

        init {
            combine(
                preferencesRepository.hapticsEnabled,
                preferencesRepository.reduceMotion,
            ) { hapticsEnabled, reduceMotion ->
                hapticsEnabled && !reduceMotion
            }.onEach { shouldProvideHaptics = it }
                .launchIn(scope)
        }

        /**
         * Trigger a light haptic tick (e.g., for scrubber per-letter feedback).
         * No-op if haptics are disabled or reduce motion is on.
         */
        override fun tick(view: View) {
            if (shouldProvideHaptics) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }

        /**
         * Trigger a medium haptic for UI confirmation (e.g., button press, popup open).
         */
        override fun confirm(view: View) {
            if (shouldProvideHaptics) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }

        /**
         * Trigger a strong haptic for important actions (e.g., delete, long press).
         */
        override fun heavy(view: View) {
            if (shouldProvideHaptics) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
