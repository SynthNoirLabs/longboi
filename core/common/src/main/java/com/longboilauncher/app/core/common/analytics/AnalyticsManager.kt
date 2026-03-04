package com.longboilauncher.app.core.common.analytics

import android.util.Log
import com.longboilauncher.app.core.common.result.LongboiError
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction for analytics and crash reporting.
 * Supports multiple backends (Firebase, local logging, etc.)
 */
interface AnalyticsManager {
    /** Log a non-fatal error for debugging */
    fun logError(
        error: LongboiError,
        context: Map<String, String> = emptyMap(),
    )

    /** Log a non-fatal exception */
    fun logException(
        throwable: Throwable,
        context: Map<String, String> = emptyMap(),
    )

    /** Log a breadcrumb for crash context */
    fun logBreadcrumb(
        message: String,
        category: String = "general",
    )

    /** Log a user action event */
    fun logEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )

    /** Set user property for segmentation */
    fun setUserProperty(
        key: String,
        value: String?,
    )

    /** Set user ID for crash reports */
    fun setUserId(id: String?)
}

/**
 * Debug implementation that logs to Logcat.
 * Use in debug builds or when Firebase is not configured.
 */
@Singleton
class DebugAnalyticsManager
    @Inject
    constructor() : AnalyticsManager {
        override fun logError(
            error: LongboiError,
            context: Map<String, String>,
        ) {
            Log.e(TAG, "Error: ${error.message}", error.cause)
            context.forEach { (key, value) ->
                Log.e(TAG, "  $key: $value")
            }
        }

        override fun logException(
            throwable: Throwable,
            context: Map<String, String>,
        ) {
            Log.e(TAG, "Exception: ${throwable.message}", throwable)
            context.forEach { (key, value) ->
                Log.e(TAG, "  $key: $value")
            }
        }

        override fun logBreadcrumb(
            message: String,
            category: String,
        ) {
            Log.d(TAG, "[$category] $message")
        }

        override fun logEvent(
            name: String,
            params: Map<String, Any>,
        ) {
            Log.d(TAG, "Event: $name")
            params.forEach { (key, value) ->
                Log.d(TAG, "  $key: $value")
            }
        }

        override fun setUserProperty(
            key: String,
            value: String?,
        ) {
            Log.d(TAG, "UserProperty: $key = $value")
        }

        override fun setUserId(id: String?) {
            Log.d(TAG, "UserId: $id")
        }

        companion object {
            private const val TAG = "LongboiAnalytics"
        }
    }

/**
 * No-op implementation for tests.
 */
class NoOpAnalyticsManager : AnalyticsManager {
    override fun logError(
        error: LongboiError,
        context: Map<String, String>,
    ) {}

    override fun logException(
        throwable: Throwable,
        context: Map<String, String>,
    ) {}

    override fun logBreadcrumb(
        message: String,
        category: String,
    ) {}

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {}

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {}

    override fun setUserId(id: String?) {}
}

/**
 * Standard event names for consistency.
 */
object AnalyticsEvents {
    const val APP_LAUNCH = "app_launch"
    const val APP_OPENED = "app_opened"
    const val SEARCH_PERFORMED = "search_performed"
    const val FAVORITE_ADDED = "favorite_added"
    const val FAVORITE_REMOVED = "favorite_removed"
    const val SETTINGS_CHANGED = "settings_changed"
    const val ICON_PACK_CHANGED = "icon_pack_changed"
    const val THEME_CHANGED = "theme_changed"
    const val GESTURE_USED = "gesture_used"
    const val WIDGET_ADDED = "widget_added"
    const val ERROR_SHOWN = "error_shown"
}

/**
 * Standard user properties.
 */
object AnalyticsProperties {
    const val THEME_MODE = "theme_mode"
    const val ICON_PACK = "icon_pack"
    const val FAVORITES_COUNT = "favorites_count"
    const val GRID_ENABLED = "grid_enabled"
    const val HAPTICS_ENABLED = "haptics_enabled"
}
