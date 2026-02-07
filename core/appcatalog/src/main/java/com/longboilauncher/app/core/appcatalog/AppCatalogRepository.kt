package com.longboilauncher.app.core.appcatalog

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.model.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCatalogRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

        private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
        val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        init {
            val callback = object : LauncherApps.Callback() {
                override fun onPackageAdded(packageName: String, user: UserHandle) {
                    refreshTrigger.tryEmit(Unit)
                }

                override fun onPackageChanged(packageName: String, user: UserHandle) {
                    refreshTrigger.tryEmit(Unit)
                }

                override fun onPackageRemoved(packageName: String, user: UserHandle) {
                    refreshTrigger.tryEmit(Unit)
                }

                override fun onPackagesAvailable(
                    packageNames: Array<out String>,
                    user: UserHandle,
                    replacing: Boolean,
                ) {
                    refreshTrigger.tryEmit(Unit)
                }

                override fun onPackagesUnavailable(
                    packageNames: Array<out String>,
                    user: UserHandle,
                    replacing: Boolean,
                ) {
                    refreshTrigger.tryEmit(Unit)
                }
            }
            launcherApps.registerCallback(callback)

            @OptIn(kotlinx.coroutines.FlowPreview::class)
            refreshTrigger
                .debounce(500)
                .onEach { refreshAppCatalog() }
                .launchIn(scope)
        }

        suspend fun refreshAppCatalog() =
            withContext(Dispatchers.IO) {
                _isLoading.value = true
                try {
                    val allApps = mutableListOf<AppEntry>()

                    // Get personal profile apps
                    allApps.addAll(getAppsForUser(Process.myUserHandle(), ProfileType.PERSONAL))

                    // Get work profile apps if available
                    userManager.userProfiles.forEach { userHandle ->
                        if (userHandle != Process.myUserHandle()) {
                            val profileType =
                                if (isPrivateSpace(userHandle)) {
                                    ProfileType.PRIVATE
                                } else {
                                    ProfileType.WORK
                                }
                            allApps.addAll(getAppsForUser(userHandle, profileType))
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
                            isSuspended =
                                try {
                                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SUSPENDED) != 0
                                } catch (e: Exception) {
                                    false
                                },
                            supportShortcuts = false,
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to process app: ${launcherActivityInfo.name}", e)
                        null
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception getting apps for user", e)
                emptyList()
            }

        private fun isPrivateSpace(user: UserHandle): Boolean =
            try {
                if (Build.VERSION.SDK_INT >= 35) {
                    val userContext = context.createContextAsUser(user, 0)
                    val um = userContext.getSystemService(UserManager::class.java)
                    um?.isPrivateProfile ?: false
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check private space status", e)
                false
            }

        fun getAppIcon(appEntry: AppEntry): Drawable? =
            try {
                val activities = launcherApps.getActivityList(appEntry.packageName, appEntry.user)
                val activity = activities.find { it.name == appEntry.className }
                activity?.getBadgedIcon(0)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get app icon for ${appEntry.packageName}", e)
                null
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
                Log.w(TAG, "Failed to get app icon for $packageName", e)
                null
            }

        fun launchApp(appEntry: AppEntry) {
            try {
                val component = ComponentName(appEntry.packageName, appEntry.className)
                launcherApps.startMainActivity(component, appEntry.user, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch app ${appEntry.packageName}", e)
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
                Log.e(TAG, "Failed to launch app $packageName", e)
            }
        }

        fun launchShortcut(
            appEntry: AppEntry,
            shortcutId: String,
        ) {
            // TODO: Implement shortcut launching via LauncherApps.startShortcut
        }

        fun openSettings(destination: String) {
            // TODO: Navigate to specific settings screen via Intent
        }

        fun getAppShortcuts(appEntry: AppEntry): List<ShortcutInfo> =
            try {
                val query =
                    LauncherApps.ShortcutQuery().apply {
                        setQueryFlags(
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                        )
                        setPackage(appEntry.packageName)
                    }
                launcherApps.getShortcuts(query, appEntry.user) ?: emptyList()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get shortcuts for ${appEntry.packageName}", e)
                emptyList()
            }

        companion object {
            private const val TAG = "AppCatalogRepository"
        }
    }
