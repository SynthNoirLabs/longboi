package com.longboilauncher.app.core.appcatalog

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun refreshAppCatalog() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val allApps = mutableListOf<AppEntry>()

        // Get personal profile apps
        allApps.addAll(getAppsForUser(Process.myUserHandle(), ProfileType.PERSONAL))

        // Get work profile apps if available
        userManager.userProfiles.forEach { userHandle ->
            if (userHandle != Process.myUserHandle()) {
                val profileType = if (isPrivateSpace(userHandle)) {
                    ProfileType.PRIVATE
                } else {
                    ProfileType.WORK
                }
                allApps.addAll(getAppsForUser(userHandle, profileType))
            }
        }

        _apps.value = allApps.sortedBy { it.label.lowercase() }
        _isLoading.value = false
    }

    private fun getAppsForUser(user: UserHandle, profileType: ProfileType): List<AppEntry> {
        return try {
            val activities = launcherApps.getActivityList(null, user)
            activities.mapNotNull { launcherActivityInfo ->
                try {
                    val appInfo = launcherActivityInfo.applicationInfo
                    val packageInfo = try {
                        context.packageManager.getPackageInfo(appInfo.packageName, 0)
                    } catch (e: Exception) {
                        null
                    }

                    val hasShortcuts = try {
                        val query = LauncherApps.ShortcutQuery().apply {
                            setQueryFlags(
                                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                            )
                            setPackage(appInfo.packageName)
                        }
                        launcherApps.getShortcuts(query, user)?.isNotEmpty() ?: false
                    } catch (e: Exception) {
                        false
                    }

                    AppEntry.create(
                        packageName = appInfo.packageName,
                        className = launcherActivityInfo.name,
                        label = launcherActivityInfo.label.toString(),
                        user = user,
                        profile = profileType,
                        lastUpdateTime = packageInfo?.lastUpdateTime ?: 0L,
                        firstInstallTime = packageInfo?.firstInstallTime ?: 0L,
                        isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                        isEnabled = appInfo.enabled,
                        isSuspended = try {
                            // Check if app is suspended via ApplicationInfo flags
                            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SUSPENDED) != 0
                        } catch (e: Exception) { false },
                        supportShortcuts = hasShortcuts
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    private fun isPrivateSpace(user: UserHandle): Boolean {
        return try {
            // Private Space detection is complex and requires Android 15+
            // For now, treat non-main users that aren't managed profiles as potential private space
            // This is a simplified heuristic
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getAppIcon(appEntry: AppEntry): Drawable? = try {
        val activities = launcherApps.getActivityList(appEntry.packageName, appEntry.user)
        val activity = activities.find { it.name == appEntry.className }
        activity?.getBadgedIcon(0)
    } catch (e: Exception) {
        null
    }

    fun getAppIcon(packageName: String, className: String, user: UserHandle): Drawable? = try {
        val activities = launcherApps.getActivityList(packageName, user)
        val activity = activities.find { it.name == className }
        activity?.getBadgedIcon(0)
    } catch (e: Exception) {
        null
    }

    fun launchApp(appEntry: AppEntry) {
        try {
            val component = ComponentName(appEntry.packageName, appEntry.className)
            launcherApps.startMainActivity(component, appEntry.user, null, null)
        } catch (e: Exception) {
            // Handle launch failure - could show toast or log
            e.printStackTrace()
        }
    }

    fun launchApp(packageName: String, className: String, user: UserHandle) {
        try {
            val component = ComponentName(packageName, className)
            launcherApps.startMainActivity(component, user, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchShortcut(appEntry: AppEntry, shortcutId: String) {
        // TODO: Implement shortcut launching via LauncherApps.startShortcut
    }

    fun openSettings(destination: String) {
        // TODO: Navigate to specific settings screen via Intent
    }

    fun getAppShortcuts(appEntry: AppEntry): List<ShortcutInfo> = try {
        val query = LauncherApps.ShortcutQuery().apply {
            setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )
            setPackage(appEntry.packageName)
        }
        launcherApps.getShortcuts(query, appEntry.user) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    fun registerPackageListener(callback: LauncherApps.Callback) {
        launcherApps.registerCallback(callback)
    }

    fun unregisterPackageListener(callback: LauncherApps.Callback) {
        launcherApps.unregisterCallback(callback)
    }
}
