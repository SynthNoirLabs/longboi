package com.longboilauncher.app.core.datastore

import androidx.datastore.core.DataStore
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.core.datastore_proto.AppUsageStat
import com.longboilauncher.core.datastore_proto.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageRepository
    @Inject
    constructor(
        private val dataStore: DataStore<UserSettings>,
    ) {
        val appUsageStats: Flow<Map<String, AppUsageStat>> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(UserSettings.getDefaultInstance())
                    } else {
                        throw exception
                    }
                }.map { it.appUsageStatsMap }

        suspend fun recordAppLaunch(appEntry: AppEntry) {
            val key = appKey(appEntry)
            val nowMs = System.currentTimeMillis()
            dataStore.updateData { currentSettings ->
                val existing =
                    currentSettings.getAppUsageStatsOrDefault(
                        key,
                        AppUsageStat.getDefaultInstance(),
                    )

                val updated =
                    existing
                        .toBuilder()
                        .setLastLaunchTimestampMs(nowMs)
                        .setLaunchCount(existing.launchCount + 1)
                        .build()

                currentSettings
                    .toBuilder()
                    .putAppUsageStats(key, updated)
                    .build()
            }
        }

        fun appKey(appEntry: AppEntry): String =
            "${appEntry.packageName}|${appEntry.className}|${appEntry.userSerialNumber}"
    }
