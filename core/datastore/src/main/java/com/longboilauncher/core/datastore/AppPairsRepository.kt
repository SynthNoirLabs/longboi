package com.longboilauncher.core.datastore

import androidx.datastore.core.DataStore
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.core.datastore_proto.AppPair
import com.longboilauncher.core.datastore_proto.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class AppPairEntry(
    val id: String,
    val firstApp: AppEntry,
    val secondApp: AppEntry,
    val label: String,
)

@Singleton
class AppPairsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<UserSettings>,
    ) {
        val appPairs: Flow<List<AppPairEntry>> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(UserSettings.getDefaultInstance())
                    } else {
                        throw exception
                    }
                }.map { settings ->
                    settings.appPairsList.map { proto ->
                        AppPairEntry(
                            id = proto.id,
                            firstApp =
                                AppEntry(
                                    packageName = proto.firstPackage,
                                    className = proto.firstClass,
                                    label = "",
                                    userSerialNumber = proto.firstUserSerialNumber,
                                ),
                            secondApp =
                                AppEntry(
                                    packageName = proto.secondPackage,
                                    className = proto.secondClass,
                                    label = "",
                                    userSerialNumber = proto.secondUserSerialNumber,
                                ),
                            label = proto.label,
                        )
                    }
                }

        suspend fun addAppPair(
            firstApp: AppEntry,
            secondApp: AppEntry,
            label: String,
        ): String {
            val id = UUID.randomUUID().toString()
            dataStore.updateData { currentSettings ->
                val newAppPair =
                    AppPair
                        .newBuilder()
                        .setId(id)
                        .setFirstPackage(firstApp.packageName)
                        .setFirstClass(firstApp.className)
                        .setFirstUserSerialNumber(firstApp.userSerialNumber)
                        .setSecondPackage(secondApp.packageName)
                        .setSecondClass(secondApp.className)
                        .setSecondUserSerialNumber(secondApp.userSerialNumber)
                        .setLabel(label)
                        .build()

                currentSettings.toBuilder().addAppPairs(newAppPair).build()
            }
            return id
        }

        suspend fun removeAppPair(id: String) {
            dataStore.updateData { currentSettings ->
                val updatedPairs = currentSettings.appPairsList.filter { it.id != id }
                currentSettings
                    .toBuilder()
                    .clearAppPairs()
                    .addAllAppPairs(updatedPairs)
                    .build()
            }
        }

        suspend fun updateAppPairLabel(
            id: String,
            label: String,
        ) {
            dataStore.updateData { currentSettings ->
                val updatedPairs =
                    currentSettings.appPairsList.map { pair ->
                        if (pair.id == id) {
                            pair.toBuilder().setLabel(label).build()
                        } else {
                            pair
                        }
                    }
                currentSettings
                    .toBuilder()
                    .clearAppPairs()
                    .addAllAppPairs(updatedPairs)
                    .build()
            }
        }
    }
