package com.longboilauncher.app.core.common.gestures

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes gesture actions.
 */
@Singleton
class GestureExecutor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private var flashlightOn = false
        private val cameraManager by lazy {
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }

        /**
         * Execute the specified gesture action.
         * Returns true if the action was handled.
         */
        fun execute(
            action: GestureAction,
            appPackageName: String? = null,
            callbacks: GestureCallbacks? = null,
        ): Boolean {
            Log.d(TAG, "Executing gesture action: $action")

            return when (action) {
                GestureAction.NONE -> false

                GestureAction.OPEN_APP_DRAWER -> {
                    callbacks?.onOpenAppDrawer()
                    true
                }

                GestureAction.OPEN_SEARCH -> {
                    callbacks?.onOpenSearch()
                    true
                }

                GestureAction.OPEN_NOTIFICATIONS -> {
                    expandNotifications()
                }

                GestureAction.OPEN_QUICK_SETTINGS -> {
                    expandQuickSettings()
                }

                GestureAction.OPEN_SETTINGS -> {
                    openSettings()
                }

                GestureAction.TOGGLE_FLASHLIGHT -> {
                    toggleFlashlight()
                }

                GestureAction.LOCK_SCREEN -> {
                    callbacks?.onLockScreen()
                    true
                }

                GestureAction.TAKE_SCREENSHOT -> {
                    callbacks?.onTakeScreenshot()
                    true
                }

                GestureAction.LAUNCH_APP -> {
                    appPackageName?.let { launchApp(it) } ?: false
                }

                GestureAction.GO_HOME -> {
                    callbacks?.onGoHome()
                    true
                }

                GestureAction.SHOW_RECENTS -> {
                    callbacks?.onShowRecents()
                    true
                }

                GestureAction.EXPAND_WIDGETS -> {
                    callbacks?.onExpandWidgets()
                    true
                }
            }
        }

        @Suppress("DEPRECATION", "PrivateApi")
        private fun expandNotifications(): Boolean {
            return try {
                val statusBarService = context.getSystemService("statusbar") ?: return false
                val statusBarClass = Class.forName("android.app.StatusBarManager")
                val expandMethod = statusBarClass.getMethod("expandNotificationsPanel")
                expandMethod.invoke(statusBarService)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to expand notifications", e)
                false
            }
        }

        @Suppress("DEPRECATION", "PrivateApi")
        private fun expandQuickSettings(): Boolean {
            return try {
                val statusBarService = context.getSystemService("statusbar") ?: return false
                val statusBarClass = Class.forName("android.app.StatusBarManager")
                val expandMethod = statusBarClass.getMethod("expandSettingsPanel")
                expandMethod.invoke(statusBarService)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to expand quick settings", e)
                false
            }
        }

        private fun openSettings(): Boolean {
            return try {
                val intent =
                    Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                if (intent.resolveActivity(context.packageManager) == null) return false
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open settings", e)
                false
            }
        }

        private fun toggleFlashlight(): Boolean {
            return try {
                val cameraId =
                    cameraManager.cameraIdList.firstOrNull { id ->
                        runCatching {
                            cameraManager
                                .getCameraCharacteristics(id)
                                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                        }.getOrDefault(false)
                    } ?: return false

                val newState = !flashlightOn
                cameraManager.setTorchMode(cameraId, newState)
                flashlightOn = newState
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle flashlight", e)
                false
            }
        }

        private fun launchApp(packageName: String): Boolean =
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    Log.w(TAG, "No launch intent for package: $packageName")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch app: $packageName", e)
                false
            }

        companion object {
            private const val TAG = "GestureExecutor"
        }
    }

/**
 * Callbacks for gesture actions that require launcher-level handling.
 */
interface GestureCallbacks {
    fun onOpenAppDrawer()

    fun onOpenSearch()

    fun onLockScreen()

    fun onTakeScreenshot()

    fun onGoHome()

    fun onShowRecents()

    fun onExpandWidgets()
}
