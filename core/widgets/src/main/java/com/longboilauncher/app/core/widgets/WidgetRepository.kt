package com.longboilauncher.app.core.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val widgetHost: WidgetHost,
    ) {
        private val packageManager: PackageManager = context.packageManager

        private val _availableWidgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
        val availableWidgets: StateFlow<List<WidgetInfo>> = _availableWidgets.asStateFlow()

        suspend fun refreshAvailableWidgets() =
            withContext(Dispatchers.IO) {
                val providers = widgetHost.getInstalledProviders()
                val widgets =
                    providers
                        .mapNotNull { provider ->
                            try {
                                WidgetInfo(
                                    provider = provider,
                                    label = provider.loadLabel(packageManager),
                                    appLabel = getAppLabel(provider),
                                    previewImage =
                                        provider.loadPreviewImage(context, 0),
                                    icon = provider.loadIcon(context, 0),
                                    minWidth = provider.minWidth,
                                    minHeight = provider.minHeight,
                                    resizeMode = provider.resizeMode,
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedBy { it.appLabel }

                _availableWidgets.value = widgets
            }

        private fun getAppLabel(provider: AppWidgetProviderInfo): String =
            try {
                val appInfo =
                    packageManager.getApplicationInfo(
                        provider.provider.packageName,
                        0,
                    )
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                provider.provider.packageName
            }

        fun getWidgetsByApp(): Map<String, List<WidgetInfo>> = _availableWidgets.value.groupBy { it.appLabel }
    }

data class WidgetInfo(
    val provider: AppWidgetProviderInfo,
    val label: String,
    val appLabel: String,
    val previewImage: Drawable?,
    val icon: Drawable?,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
)
