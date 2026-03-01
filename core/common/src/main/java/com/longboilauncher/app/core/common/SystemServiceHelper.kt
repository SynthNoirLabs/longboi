package com.longboilauncher.app.core.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemServiceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Expands the notification shade.
     * Uses reflection as this is a hidden API.
     */
    @SuppressLint("WrongConstant")
    fun expandNotifications() {
        try {
            val service = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(service)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Expands the quick settings panel.
     */
    @SuppressLint("WrongConstant")
    fun expandQuickSettings() {
        try {
            val service = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandSettingsPanel")
            method.invoke(service)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
