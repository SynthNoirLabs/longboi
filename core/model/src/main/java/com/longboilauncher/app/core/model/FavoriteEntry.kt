package com.longboilauncher.app.core.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Stable
@Serializable
data class FavoriteEntry(
    val id: String,
    val appEntry: AppEntry,
    val position: Int,
    val customLabel: String? = null,
    val notificationCount: Int = 0,
    val hasNotifications: Boolean = false,
    val isPlaying: Boolean = false,
    val groupId: String? = null
) {
    @Transient
    val displayLabel: String = customLabel ?: appEntry.label
}

@Stable
@Serializable
data class FavoriteGroup(
    val id: String,
    val name: String,
    val emoji: String? = null,
    val isCollapsed: Boolean = false,
    val position: Int
)
