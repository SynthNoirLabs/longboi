package com.longboilauncher.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.longboilauncher.app.core.common.NotificationState

/**
 * System-bound NotificationListenerService that tracks active notification counts per package.
 *
 * Requires the user to grant Notification Access in Settings → Apps → Special access.
 * Declared in AndroidManifest with BIND_NOTIFICATION_LISTENER_SERVICE permission.
 *
 * Pushes counts into [NotificationState] (core:common) so that feature modules can
 * collect them without depending on the app module.
 */
class NotificationService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        rebuildCounts()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        rebuildCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        rebuildCounts()
    }

    /**
     * Rebuilds the full map from the system's [getActiveNotifications].
     * This is more reliable than incremental add/remove because the system
     * can silently group or dismiss notifications.
     */
    private fun rebuildCounts() {
        val counts = mutableMapOf<String, Int>()
        try {
            activeNotifications?.forEach { sbn ->
                // Skip ongoing/foreground-service notifications (they aren't user-facing alerts)
                if (sbn.isOngoing) return@forEach
                val pkg = sbn.packageName
                counts[pkg] = (counts[pkg] ?: 0) + 1
            }
        } catch (e: Exception) {
            // SecurityException if listener got disconnected mid-call
        }
        NotificationState.updateCounts(counts)
    }
}
