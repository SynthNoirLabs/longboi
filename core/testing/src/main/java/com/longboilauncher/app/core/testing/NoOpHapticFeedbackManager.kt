package com.longboilauncher.app.core.testing

import android.view.View
import com.longboilauncher.app.core.settings.HapticFeedbackInterface
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of HapticFeedbackInterface for tests and previews.
 */
@Singleton
class NoOpHapticFeedbackManager
    @Inject
    constructor() : HapticFeedbackInterface {
        override fun tick(view: View) {}

        override fun confirm(view: View) {}

        override fun heavy(view: View) {}
    }
