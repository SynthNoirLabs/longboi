package com.longboilauncher.app.core.settings

import androidx.datastore.core.DataStore
import com.longboilauncher.app.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<UserSettings>,
    ) {
        private val safeData: Flow<UserSettings> =
            dataStore.data.catch { exception ->
                if (exception is IOException) emit(UserSettings.getDefaultInstance()) else throw exception
            }

        val theme: Flow<String> = safeData.map { it.theme.ifBlank { "system" } }
        val showNotifications: Flow<Boolean> = safeData.map { it.showNotifications }
        val gestureSwipeUp: Flow<String> = safeData.map { it.gestureSwipeUp.ifBlank { "all_apps" } }
        val gestureSwipeDown: Flow<String> = safeData.map { it.gestureSwipeDown }
        val hapticsEnabled: Flow<Boolean> = safeData.map { it.hapticsEnabled }
        val density: Flow<String> = safeData.map { it.density.ifBlank { "default" } }
        val iconPackPackageName: Flow<String> = safeData.map { it.iconPackPackageName }
        val perAppIconOverrides: Flow<Map<String, String>> = safeData.map { it.perAppIconOverridesMap }
        val reduceMotion: Flow<Boolean> = safeData.map { it.reduceMotion }

        suspend fun setTheme(theme: String) {
            dataStore.updateData { it.toBuilder().setTheme(theme).build() }
        }

        suspend fun setShowNotifications(show: Boolean) {
            dataStore.updateData { it.toBuilder().setShowNotifications(show).build() }
        }

        suspend fun setGestureSwipeUp(action: String) {
            dataStore.updateData { it.toBuilder().setGestureSwipeUp(action).build() }
        }

        suspend fun setGestureSwipeDown(action: String) {
            dataStore.updateData { it.toBuilder().setGestureSwipeDown(action).build() }
        }

        suspend fun setHapticsEnabled(enabled: Boolean) {
            dataStore.updateData { it.toBuilder().setHapticsEnabled(enabled).build() }
        }

        suspend fun setReduceMotion(reduce: Boolean) {
            dataStore.updateData { it.toBuilder().setReduceMotion(reduce).build() }
        }

        suspend fun setDensity(density: String) {
            dataStore.updateData { it.toBuilder().setDensity(density).build() }
        }

        suspend fun getSerializedSettings(): ByteArray = dataStore.data.first().toByteArray()

        suspend fun restoreSettings(byteArray: ByteArray) {
            dataStore.updateData {
                UserSettings.parseFrom(byteArray)
            }
        }
    }
