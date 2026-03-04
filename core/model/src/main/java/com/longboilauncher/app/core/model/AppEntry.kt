package com.longboilauncher.app.core.model

import android.os.UserHandle
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents an installed application.
 *
 * [userSerialNumber] is the stable ID used for persistence (from UserManager.getSerialNumberForUser).
 * [user] is a transient field populated during runtime. It should NOT be persisted.
 */
@Stable
@Serializable
data class AppEntry(
    val packageName: String,
    val className: String,
    val label: String,
    val userSerialNumber: Long = 0L,
    val profile: ProfileType = ProfileType.PERSONAL,
    val lastUpdateTime: Long = 0L,
    val firstInstallTime: Long = 0L,
    val isSystemApp: Boolean = false,
    val isEnabled: Boolean = true,
    val isSuspended: Boolean = false,
    val isArchived: Boolean = false,
    val supportShortcuts: Boolean = false,
    @Transient
    val user: UserHandle? = null,
) {
    /**
     * Returns true when both entries refer to the same installed app component.
     */
    fun sameApp(other: AppEntry): Boolean =
        packageName == other.packageName &&
            className == other.className &&
            userSerialNumber == other.userSerialNumber

    override fun toString(): String = label

    val component: android.content.ComponentName
        get() = android.content.ComponentName(packageName, className)

    companion object {
        fun create(
            packageName: String,
            className: String,
            label: String,
            user: UserHandle,
            userSerialNumber: Long,
            profile: ProfileType = ProfileType.PERSONAL,
            lastUpdateTime: Long = 0L,
            firstInstallTime: Long = 0L,
            isSystemApp: Boolean = false,
            isEnabled: Boolean = true,
            isSuspended: Boolean = false,
            isArchived: Boolean = false,
            supportShortcuts: Boolean = false,
        ): AppEntry =
            AppEntry(
                packageName = packageName,
                className = className,
                label = label,
                userSerialNumber = userSerialNumber,
                user = user,
                profile = profile,
                lastUpdateTime = lastUpdateTime,
                firstInstallTime = firstInstallTime,
                isSystemApp = isSystemApp,
                isEnabled = isEnabled,
                isSuspended = isSuspended,
                isArchived = isArchived,
                supportShortcuts = supportShortcuts,
            )
    }
}

@Stable
@Serializable
enum class ProfileType {
    PERSONAL,
    WORK,
    PRIVATE,
}
