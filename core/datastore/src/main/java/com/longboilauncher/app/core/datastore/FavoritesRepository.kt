package com.longboilauncher.app.core.datastore

import androidx.datastore.core.DataStore
import com.longboilauncher.app.core.common.UserHandleManager
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.core.datastore_proto.FavoriteApp
import com.longboilauncher.core.datastore_proto.UserSettings
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class FavoritesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<UserSettings>,
        private val userHandleManager: UserHandleManager,
    ) {
        val favorites: Flow<List<FavoriteEntry>> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(UserSettings.getDefaultInstance())
                    } else {
                        throw exception
                    }
                }.map { settings ->
                    settings.favoritesList.mapIndexed { index, proto ->
                        FavoriteEntry(
                            id = "${proto.packageName}_${proto.userSerialNumber}",
                            appEntry =
                                AppEntry(
                                    packageName = proto.packageName,
                                    className = proto.className,
                                    label = proto.label,
                                    userSerialNumber = proto.userSerialNumber,
                                    profile = ProfileType.valueOf(proto.profile.ifBlank { "PERSONAL" }),
                                    user = userHandleManager.getUserForSerialNumber(proto.userSerialNumber),
                                ),
                            position = index,
                        )
                    }
                }

        val hiddenApps: Flow<Set<String>> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(UserSettings.getDefaultInstance())
                    } else {
                        throw exception
                    }
                }.map { it.hiddenAppsList.toSet() }

        suspend fun addFavorite(appEntry: AppEntry) {
            dataStore.updateData { currentSettings ->
                val alreadyExists =
                    currentSettings.favoritesList.any {
                        it.packageName == appEntry.packageName && it.userSerialNumber == appEntry.userSerialNumber
                    }
                if (alreadyExists) return@updateData currentSettings

                val newFavorite =
                    FavoriteApp
                        .newBuilder()
                        .setPackageName(appEntry.packageName)
                        .setClassName(appEntry.className)
                        .setLabel(appEntry.label)
                        .setUserSerialNumber(appEntry.userSerialNumber)
                        .setProfile(appEntry.profile.name)
                        .build()

                currentSettings
                    .toBuilder()
                    .addFavorites(newFavorite)
                    .build()
            }
        }

        suspend fun removeFavorite(favoriteId: String) {
            dataStore.updateData { currentSettings ->
                val updatedFavorites =
                    currentSettings.favoritesList.filter {
                        "${it.packageName}_${it.userSerialNumber}" != favoriteId
                    }
                currentSettings
                    .toBuilder()
                    .clearFavorites()
                    .addAllFavorites(updatedFavorites)
                    .build()
            }
        }

        suspend fun reorderFavorites(favoriteIds: List<String>) {
            dataStore.updateData { currentSettings ->
                val reordered =
                    favoriteIds.mapNotNull { id ->
                        currentSettings.favoritesList.find { "${it.packageName}_${it.userSerialNumber}" == id }
                    }
                currentSettings
                    .toBuilder()
                    .clearFavorites()
                    .addAllFavorites(reordered)
                    .build()
            }
        }

        suspend fun hideApp(packageName: String) {
            dataStore.updateData { currentSettings ->
                if (currentSettings.hiddenAppsList.contains(packageName)) return@updateData currentSettings
                currentSettings
                    .toBuilder()
                    .addHiddenApps(packageName)
                    .build()
            }
        }

        suspend fun unhideApp(packageName: String) {
            dataStore.updateData { currentSettings ->
                val updated = currentSettings.hiddenAppsList.filter { it != packageName }
                currentSettings
                    .toBuilder()
                    .clearHiddenApps()
                    .addAllHiddenApps(updated)
                    .build()
            }
        }
    }
