package com.longboilauncher.app.core.model

import android.os.Process
import android.os.UserHandle
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Stable
@Serializable
data class AppEntry(
    val packageName: String,
    val className: String,
    val label: String,
    val userIdentifier: Int = 0,
    val profile: ProfileType = ProfileType.PERSONAL,
    val lastUpdateTime: Long = 0L,
    val firstInstallTime: Long = 0L,
    val isSystemApp: Boolean = false,
    val isEnabled: Boolean = true,
    val isSuspended: Boolean = false,
    val isArchived: Boolean = false,
    val supportShortcuts: Boolean = false,
) {
    @Transient
    val user: UserHandle =
        if (userIdentifier == 0) {
            Process.myUserHandle()
        } else {
            UserHandle::class.java.getConstructor(Int::class.java).newInstance(userIdentifier)
        }

    /**
     * Returns true when both entries refer to the same installed app component (same package,
     * activity class, and user profile). Use this for identity checks (e.g. "did I already load
     * shortcuts for this app?"). Do NOT use it as a general equality test — data-class `equals`
     * covers all fields and is what `MutableStateFlow` / Compose stability need.
     */
    fun sameApp(other: AppEntry): Boolean =
        packageName == other.packageName &&
            className == other.className &&
            userIdentifier == other.userIdentifier

    override fun toString(): String = label

    val component: android.content.ComponentName
        get() = android.content.ComponentName(packageName, className)

    companion object {
        fun create(
            packageName: String,
            className: String,
            label: String,
            user: UserHandle,
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
                userIdentifier = user.hashCode(),
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
