package com.longboilauncher.app.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-global singleton that holds notification counts per package.
 *
 * Written by [NotificationService] (app module) when the system posts/removes notifications.
 * Read by [HomeViewModel] (feature:home module) to enrich FavoriteEntry with badge counts.
 *
 * Using an object rather than DI because NotificationListenerService is system-instantiated
 * and its lifecycle is independent of the activity/Hilt component hierarchy.
 */
object NotificationState {
    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())

    /** Map of packageName → active (non-ongoing) notification count. */
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    /** Called by [NotificationService] when notifications change. */
    fun updateCounts(newCounts: Map<String, Int>) {
        _counts.value = newCounts
    }
}
