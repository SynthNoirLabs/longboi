package com.longboilauncher.app.core.notifications

import android.graphics.drawable.Icon

/**
 * Represents a notification with optional progress information.
 * Supports Android 16 progress-centric notification patterns.
 */
data class NotificationEntry(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val icon: Icon?,
    val timestamp: Long,
    val progress: ProgressInfo? = null,
    val category: NotificationCategory = NotificationCategory.DEFAULT,
)

/**
 * Progress information for progress-centric notifications.
 * Used for downloads, uploads, media playback, timers, etc.
 */
data class ProgressInfo(
    val current: Int,
    val max: Int,
    val isIndeterminate: Boolean = false,
) {
    val fraction: Float
        get() = if (max > 0 && !isIndeterminate) current.toFloat() / max else 0f

    val percentage: Int
        get() = (fraction * 100).toInt()

    val isComplete: Boolean
        get() = !isIndeterminate && current >= max
}

/**
 * Categories for progress-centric notifications following Android 16 patterns.
 */
enum class NotificationCategory {
    DEFAULT,
    PROGRESS, // Downloads, uploads, installations
    MEDIA, // Music, video playback
    NAVIGATION, // Maps, directions
    TRANSPORT, // Rideshare, delivery tracking
    CALL, // Ongoing calls
    TIMER, // Timers, alarms, countdowns
    WORKOUT, // Fitness tracking
}
