package com.longboilauncher.app.core.settings

import android.view.View
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of HapticFeedbackManager for tests and previews.
 */
@Singleton
class NoOpHapticFeedbackManager @Inject constructor() : HapticFeedbackInterface {
    override fun tick(view: View) {}
    override fun confirm(view: View) {}
    override fun heavy(view: View) {}
}
