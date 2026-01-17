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
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserSettings>
) {
    val theme: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.theme }

    val showNotifications: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.showNotifications }

    val gestureSwipeUp: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.gestureSwipeUp }

    val gestureSwipeDown: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.gestureSwipeDown }

    val hapticsEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.hapticsEnabled }

    val density: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.density.ifBlank { "default" } }

    val iconPackPackageName: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.iconPackPackageName }

    val perAppIconOverrides: Flow<Map<String, String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.perAppIconOverridesMap }

    val reduceMotion: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { it.reduceMotion }

    suspend fun setTheme(theme: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setTheme(theme)
                .build()
        }
    }

    suspend fun setShowNotifications(show: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setShowNotifications(show)
                .build()
        }
    }

    suspend fun setGestureSwipeUp(action: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setGestureSwipeUp(action)
                .build()
        }
    }

    suspend fun setGestureSwipeDown(action: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setGestureSwipeDown(action)
                .build()
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setHapticsEnabled(enabled)
                .build()
        }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setReduceMotion(reduce)
                .build()
        }
    }

    suspend fun setDensity(density: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setDensity(density)
                .build()
        }
    }

    suspend fun getSerializedSettings(): ByteArray {
        return dataStore.data.first().toByteArray()
    }

    suspend fun restoreSettings(byteArray: ByteArray) {
        dataStore.updateData {
            UserSettings.parseFrom(byteArray)
        }
    }
}
