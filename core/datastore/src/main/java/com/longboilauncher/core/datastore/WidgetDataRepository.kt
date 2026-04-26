package com.longboilauncher.core.datastore

import android.content.ComponentName
import androidx.datastore.core.DataStore
import com.longboilauncher.core.datastore_proto.PersistedWidget
import com.longboilauncher.core.datastore_proto.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class PersistedWidgetEntry(
    val appWidgetId: Int,
    val providerComponent: ComponentName,
    val minWidth: Int,
    val minHeight: Int,
    val positionX: Int,
    val positionY: Int,
    val spanX: Int,
    val spanY: Int,
)

@Singleton
class WidgetDataRepository
    @Inject
    constructor(
        private val dataStore: DataStore<UserSettings>,
    ) {
        val widgets: Flow<List<PersistedWidgetEntry>> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(UserSettings.getDefaultInstance())
                    } else {
                        throw exception
                    }
                }.map { settings ->
                    settings.widgetsList.map { proto ->
                        PersistedWidgetEntry(
                            appWidgetId = proto.appWidgetId,
                            providerComponent =
                                ComponentName(
                                    proto.providerPackage,
                                    proto.providerClass,
                                ),
                            minWidth = proto.minWidth,
                            minHeight = proto.minHeight,
                            positionX = proto.positionX,
                            positionY = proto.positionY,
                            spanX = proto.spanX,
                            spanY = proto.spanY,
                        )
                    }
                }

        suspend fun addWidget(entry: PersistedWidgetEntry) {
            dataStore.updateData { currentSettings ->
                val alreadyExists =
                    currentSettings.widgetsList.any { it.appWidgetId == entry.appWidgetId }
                if (alreadyExists) return@updateData currentSettings

                val newWidget =
                    PersistedWidget
                        .newBuilder()
                        .setAppWidgetId(entry.appWidgetId)
                        .setProviderPackage(entry.providerComponent.packageName)
                        .setProviderClass(entry.providerComponent.className)
                        .setMinWidth(entry.minWidth)
                        .setMinHeight(entry.minHeight)
                        .setPositionX(entry.positionX)
                        .setPositionY(entry.positionY)
                        .setSpanX(entry.spanX)
                        .setSpanY(entry.spanY)
                        .build()

                currentSettings.toBuilder().addWidgets(newWidget).build()
            }
        }

        suspend fun removeWidget(appWidgetId: Int) {
            dataStore.updateData { currentSettings ->
                val updatedWidgets =
                    currentSettings.widgetsList.filter { it.appWidgetId != appWidgetId }
                currentSettings
                    .toBuilder()
                    .clearWidgets()
                    .addAllWidgets(updatedWidgets)
                    .build()
            }
        }

        suspend fun updateWidgetPosition(
            appWidgetId: Int,
            positionX: Int,
            positionY: Int,
            spanX: Int,
            spanY: Int,
        ) {
            dataStore.updateData { currentSettings ->
                val updatedWidgets =
                    currentSettings.widgetsList.map { widget ->
                        if (widget.appWidgetId == appWidgetId) {
                            widget
                                .toBuilder()
                                .setPositionX(positionX)
                                .setPositionY(positionY)
                                .setSpanX(spanX)
                                .setSpanY(spanY)
                                .build()
                        } else {
                            widget
                        }
                    }
                currentSettings
                    .toBuilder()
                    .clearWidgets()
                    .addAllWidgets(updatedWidgets)
                    .build()
            }
        }

        suspend fun clearAllWidgets() {
            dataStore.updateData { currentSettings ->
                currentSettings.toBuilder().clearWidgets().build()
            }
        }
    }
