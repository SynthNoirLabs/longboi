package com.longboilauncher.app.core.appcatalog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import com.longboilauncher.app.core.common.UserHandleManager
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCatalogRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val userHandleManager: UserHandleManager,
    ) {
        private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
        val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val packageCallback =
            object : LauncherApps.Callback() {
                override fun onPackageRemoved(
                    packageName: String,
                    user: UserHandle,
                ) {
                    repositoryScope.launch { refreshAppCatalog() }
                }

                override fun onPackageAdded(
                    packageName: String,
                    user: UserHandle,
                ) {
                    repositoryScope.launch { refreshAppCatalog() }
                }

                override fun onPackageChanged(
                    packageName: String,
                    user: UserHandle,
                ) {
                    repositoryScope.launch { refreshAppCatalog() }
                }

                override fun onPackagesAvailable(
                    packageNames: Array<out String>,
                    user: UserHandle,
                    replacing: Boolean,
                ) {
                    repositoryScope.launch { refreshAppCatalog() }
                }

                override fun onPackagesUnavailable(
                    packageNames: Array<out String>,
                    user: UserHandle,
                    replacing: Boolean,
                ) {
                    repositoryScope.launch { refreshAppCatalog() }
                }

                override fun onShortcutsChanged(
                    packageName: String,
                    shortcuts: MutableList<ShortcutInfo>,
                    user: UserHandle,
                ) {
                    repositoryScope.launch { refreshAppCatalog() }
                }
            }

        init {
            launcherApps.registerCallback(packageCallback)
            repositoryScope.launch { refreshAppCatalog() }
        }

        suspend fun refreshAppCatalog() =
            withContext(Dispatchers.IO) {
                try {
                    _isLoading.value = true
                    val allApps = mutableListOf<AppEntry>()

                    val myUser = userHandleManager.myUserHandle()
                    val myUserSerial = userHandleManager.getSerialNumberForUser(myUser)

                    // Get personal profile apps
                    allApps.addAll(getAppsForUser(myUser, myUserSerial, ProfileType.PERSONAL))

                    // Get other profile apps if available
                    userManager.userProfiles.forEach { userHandle ->
                        if (userHandle != myUser) {
                            val serial = userHandleManager.getSerialNumberForUser(userHandle)
                            val profileType =
                                if (isPrivateSpace(userHandle)) {
                                    ProfileType.PRIVATE
                                } else {
                                    ProfileType.WORK
                                }
                            allApps.addAll(getAppsForUser(userHandle, serial, profileType))
                        }
                    }

                    _apps.value = allApps.sortedBy { it.label.lowercase() }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh app catalog", e)
                } finally {
                    _isLoading.value = false
                }
            }

        private fun getAppsForUser(
            user: UserHandle,
            userSerialNumber: Long,
            profileType: ProfileType,
        ): List<AppEntry> =
            try {
                val activities = launcherApps.getActivityList(null, user)
                activities.mapNotNull { launcherActivityInfo ->
                    try {
                        val appInfo = launcherActivityInfo.applicationInfo
                        val packageInfo =
                            try {
                                context.packageManager.getPackageInfo(appInfo.packageName, 0)
                            } catch (e: Exception) {
                                null
                            }

                        val hasShortcuts =
                            try {
                                val query =
                                    LauncherApps.ShortcutQuery().apply {
                                        setQueryFlags(
                                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST,
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
                            userSerialNumber = userSerialNumber,
                            profile = profileType,
                            lastUpdateTime = packageInfo?.lastUpdateTime ?: 0L,
                            firstInstallTime = packageInfo?.firstInstallTime ?: 0L,
                            isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                            isEnabled = appInfo.enabled,
                            isSuspended =
                                try {
                                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SUSPENDED) != 0
                                } catch (e: Exception) {
                                    false
                                },
                            supportShortcuts = hasShortcuts,
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: SecurityException) {
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting apps for user $user", e)
                emptyList()
            }

        private fun isPrivateSpace(user: UserHandle): Boolean {
            // Private space arrived in Android 14 (API 34) but a stable public detection
            // API (UserProperties.PROFILE_TYPE_PRIVATE) only exists from API 35.
            // Use reflection to avoid a compile-time hard dependency on SDK 35 symbols.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return false
            return try {
                val userPropertiesClass = Class.forName("android.content.pm.UserProperties")
                val getPropertiesMethod =
                    UserManager::class.java.getMethod("getUserProperties", UserHandle::class.java)
                val properties = getPropertiesMethod.invoke(userManager, user) ?: return false
                val profileType =
                    userPropertiesClass.getMethod("getProfileType").invoke(properties) as? Int
                        ?: return false
                val privateType = userPropertiesClass.getField("PROFILE_TYPE_PRIVATE").getInt(null)
                profileType == privateType
            } catch (e: Exception) {
                Log.w(TAG, "Could not resolve profile type for $user", e)
                false
            }
        }

        fun getAppIcon(appEntry: AppEntry): Drawable? {
            val user = appEntry.user ?: return null
            return try {
                val activities = launcherApps.getActivityList(appEntry.packageName, user)
                val activity = activities.find { it.name == appEntry.className }
                activity?.getBadgedIcon(0)
            } catch (e: Exception) {
                null
            }
        }

        fun getAppIcon(
            packageName: String,
            className: String,
            user: UserHandle,
        ): Drawable? =
            try {
                val activities = launcherApps.getActivityList(packageName, user)
                val activity = activities.find { it.name == className }
                activity?.getBadgedIcon(0)
            } catch (e: Exception) {
                null
            }

        fun launchApp(appEntry: AppEntry) {
            val user = appEntry.user ?: return
            try {
                val component = ComponentName(appEntry.packageName, appEntry.className)
                launcherApps.startMainActivity(component, user, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch ${appEntry.packageName}", e)
            }
        }

        fun launchApp(
            packageName: String,
            className: String,
            user: UserHandle,
        ) {
            try {
                val component = ComponentName(packageName, className)
                launcherApps.startMainActivity(component, user, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch $packageName/$className", e)
            }
        }

        fun launchShortcut(
            appEntry: AppEntry,
            shortcutId: String,
        ) {
            val user = appEntry.user ?: return
            try {
                launcherApps.startShortcut(appEntry.packageName, shortcutId, null, null, user)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch shortcut $shortcutId for ${appEntry.packageName}", e)
            }
        }

        fun showAppInfo(appEntry: AppEntry) {
            val user = appEntry.user ?: return
            try {
                val component = ComponentName(appEntry.packageName, appEntry.className)
                launcherApps.startAppDetailsActivity(component, user, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open app info for ${appEntry.packageName}", e)
            }
        }

        fun openSettings(destination: String) {
            try {
                val intent =
                    when (destination) {
                        "wifi" -> Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                        "bluetooth" -> Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                        "display" -> Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)
                        "sound" -> Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                        "apps" -> Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                        "battery" -> Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
                        "storage" -> Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                        "security" -> Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                        else -> Intent(android.provider.Settings.ACTION_SETTINGS)
                    }.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open settings destination=$destination", e)
            }
        }

        fun uninstallApp(appEntry: AppEntry) {
            try {
                val intent =
                    Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${appEntry.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start uninstall for ${appEntry.packageName}", e)
            }
        }

        fun getAppShortcuts(appEntry: AppEntry): List<ShortcutInfo> {
            val user = appEntry.user ?: return emptyList()
            return try {
                val query =
                    LauncherApps.ShortcutQuery().apply {
                        setQueryFlags(
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                        )
                        setPackage(appEntry.packageName)
                    }
                launcherApps.getShortcuts(query, user) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun registerPackageListener(callback: LauncherApps.Callback) {
            launcherApps.registerCallback(callback)
        }

        fun unregisterPackageListener(callback: LauncherApps.Callback) {
            launcherApps.unregisterCallback(callback)
        }

        private companion object {
            const val TAG = "AppCatalogRepository"
        }
    }
