package com.longboilauncher.app.core.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing notifications with support for progress-centric notifications.
 */
@Singleton
class NotificationRepository
    @Inject
    constructor() {
        private val _notifications = MutableStateFlow<List<NotificationEntry>>(emptyList())
        val notifications: StateFlow<List<NotificationEntry>> = _notifications.asStateFlow()

        /** Flow of notifications that have active progress (downloads, uploads, etc.) */
        val progressNotifications =
            _notifications.map { list ->
                list
                    .filter { it.progress != null }
                    .sortedByDescending { it.timestamp }
            }

        /** Flow of progress notifications grouped by category */
        val progressByCategory =
            _notifications.map { list ->
                list
                    .filter { it.progress != null }
                    .groupBy { it.category }
            }

        fun updateNotifications(notifications: List<NotificationEntry>) {
            _notifications.value = notifications
        }

        fun getNotificationsForPackage(packageName: String): List<NotificationEntry> =
            _notifications.value.filter {
                it.packageName == packageName
            }

        fun getProgressNotificationsForPackage(packageName: String): List<NotificationEntry> =
            _notifications.value.filter {
                it.packageName == packageName && it.progress != null
            }

        fun hasActiveProgress(packageName: String): Boolean =
            _notifications.value.any {
                it.packageName == packageName &&
                    it.progress != null &&
                    !it.progress.isComplete
            }

        fun clearNotificationsForPackage(packageName: String) {
            _notifications.value = _notifications.value.filter { it.packageName != packageName }
        }
    }
