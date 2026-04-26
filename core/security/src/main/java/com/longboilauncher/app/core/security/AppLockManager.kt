package com.longboilauncher.app.core.security

import android.hardware.biometrics.BiometricManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages app locking with biometric/PIN authentication.
 * Locked apps require authentication before launching.
 */
@Singleton
class AppLockManager
    @Inject
    constructor(
        private val biometricPromptManager: BiometricPromptManager,
        private val dataStore: DataStore<Preferences>,
    ) {
        private val lockedAppsKey = stringSetPreferencesKey("locked_apps")
        private val recentlyUnlockedApps = mutableSetOf<String>()

        /**
         * Flow of package names that are locked.
         */
        val lockedApps: Flow<Set<String>> =
            dataStore.data.map { prefs ->
                prefs[lockedAppsKey] ?: emptySet()
            }

        /**
         * Check if an app is locked.
         */
        suspend fun isAppLocked(packageName: String): Boolean {
            val locked = dataStore.data.first()[lockedAppsKey] ?: emptySet()
            return packageName in locked && packageName !in recentlyUnlockedApps
        }

        /**
         * Lock an app (require authentication to launch).
         */
        suspend fun lockApp(packageName: String) {
            dataStore.edit { prefs ->
                val current = prefs[lockedAppsKey] ?: emptySet()
                prefs[lockedAppsKey] = current + packageName
            }
            recentlyUnlockedApps.remove(packageName)
        }

        /**
         * Unlock an app (remove lock requirement).
         */
        suspend fun unlockApp(packageName: String) {
            dataStore.edit { prefs ->
                val current = prefs[lockedAppsKey] ?: emptySet()
                prefs[lockedAppsKey] = current - packageName
            }
        }

        /**
         * Authenticate to access a locked app.
         * Returns true if authentication succeeded.
         */
        suspend fun authenticateForApp(packageName: String): Boolean {
            if (!isAppLocked(packageName)) return true

            val success =
                biometricPromptManager.showPrompt(
                    title = "Unlock App",
                    subtitle = "Authenticate to open this app",
                    authenticators =
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                )

            if (success) {
                // Temporarily unlock this app for this session
                recentlyUnlockedApps.add(packageName)
            }

            return success
        }

        /**
         * Re-lock all apps that were temporarily unlocked.
         * Call this when the launcher goes to background or screen turns off.
         */
        fun relockAllApps() {
            recentlyUnlockedApps.clear()
        }

        /**
         * Check if any apps are locked.
         */
        suspend fun hasLockedApps(): Boolean {
            val locked = dataStore.data.first()[lockedAppsKey] ?: emptySet()
            return locked.isNotEmpty()
        }
    }
