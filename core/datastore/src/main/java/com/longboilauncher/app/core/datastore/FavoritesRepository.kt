package com.longboilauncher.app.core.datastore

import androidx.datastore.core.DataStore
import com.longboilauncher.app.FavoriteApp
import com.longboilauncher.app.UserSettings
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ProfileType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<UserSettings>,
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
                            id = "${proto.packageName}_${proto.userIdentifier}",
                            appEntry =
                                AppEntry(
                                    packageName = proto.packageName,
                                    className = proto.className,
                                    label = proto.label,
                                    userIdentifier = proto.userIdentifier,
                                    profile = ProfileType.valueOf(proto.profile.ifBlank { "PERSONAL" }),
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
                        it.packageName == appEntry.packageName && it.userIdentifier == appEntry.userIdentifier
                    }
                if (alreadyExists) return@updateData currentSettings

                val newFavorite =
                    FavoriteApp
                        .newBuilder()
                        .setPackageName(appEntry.packageName)
                        .setClassName(appEntry.className)
                        .setLabel(appEntry.label)
                        .setUserIdentifier(appEntry.userIdentifier)
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
                        "${it.packageName}_${it.userIdentifier}" != favoriteId
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
                        currentSettings.favoritesList.find { "${it.packageName}_${it.userIdentifier}" == id }
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
