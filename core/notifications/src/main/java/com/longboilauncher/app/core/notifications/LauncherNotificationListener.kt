package com.longboilauncher.app.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Notification listener service that extracts progress-centric notification data.
 * Supports Android 16 progress notification patterns.
 */
@AndroidEntryPoint
class LauncherNotificationListener : NotificationListenerService() {
    @Inject lateinit var notificationRepository: NotificationRepository

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        updateNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        updateNotifications()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        updateNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        notificationRepository.updateNotifications(emptyList())
    }

    private fun updateNotifications() {
        val entries =
            try {
                activeNotifications?.mapNotNull { sbn ->
                    val notification = sbn.notification
                    val extras = notification.extras
                    val title =
                        extras
                            .getCharSequence(Notification.EXTRA_TITLE)
                            ?.toString()
                            ?: ""
                    val text =
                        extras
                            .getCharSequence(Notification.EXTRA_TEXT)
                            ?.toString()
                            ?: ""

                    if (title.isBlank() && text.isBlank()) return@mapNotNull null

                    val progress = extractProgress(extras)
                    val category = mapCategory(notification.category)

                    NotificationEntry(
                        key = sbn.key,
                        packageName = sbn.packageName,
                        title = title,
                        text = text,
                        icon = notification.smallIcon,
                        timestamp = sbn.postTime,
                        progress = progress,
                        category = category,
                    )
                }
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

        notificationRepository.updateNotifications(entries)
    }

    private fun extractProgress(extras: android.os.Bundle): ProgressInfo? {
        val max = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        val current = extras.getInt(Notification.EXTRA_PROGRESS, 0)
        val indeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)

        return if (max > 0 || indeterminate) {
            ProgressInfo(
                current = current,
                max = max,
                isIndeterminate = indeterminate,
            )
        } else {
            null
        }
    }

    private fun mapCategory(category: String?): NotificationCategory =
        when (category) {
            Notification.CATEGORY_PROGRESS -> NotificationCategory.PROGRESS
            Notification.CATEGORY_TRANSPORT -> NotificationCategory.TRANSPORT
            Notification.CATEGORY_NAVIGATION -> NotificationCategory.NAVIGATION
            Notification.CATEGORY_CALL -> NotificationCategory.CALL
            Notification.CATEGORY_ALARM -> NotificationCategory.TIMER
            Notification.CATEGORY_STOPWATCH -> NotificationCategory.TIMER
            Notification.CATEGORY_WORKOUT -> NotificationCategory.WORKOUT
            "android.media" -> NotificationCategory.MEDIA // Media session category
            else -> NotificationCategory.DEFAULT
        }
}
