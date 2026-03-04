package com.longboilauncher.app.core.common

import android.view.View

/**
 * Interface to enable test doubles for HapticFeedbackManager.
 */
interface HapticFeedbackInterface {
    fun tick(view: View)

    fun confirm(view: View)

    fun heavy(view: View)
}
