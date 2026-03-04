package com.longboilauncher.app.core.common

import android.view.HapticFeedbackConstants
import android.view.View
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized haptic feedback manager.
 * Features:
 * - Simple API for common interactions (tick, confirm, heavy)
 * - State managed externally (e.g. by ViewModels)
 */
@Singleton
class HapticFeedbackManager @Inject constructor() : HapticFeedbackInterface {
    @Volatile
    var isEnabled: Boolean = true

    override fun tick(view: View) {
        if (isEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    override fun confirm(view: View) {
        if (isEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    override fun heavy(view: View) {
        if (isEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
