package com.longboilauncher.app.core.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import com.longboilauncher.app.core.common.di.ApplicationScope
import com.longboilauncher.core.datastore.PersistedWidgetEntry
import com.longboilauncher.core.datastore.WidgetDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetHost
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val widgetDataRepository: WidgetDataRepository,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) {
        companion object {
            private const val HOST_ID = 1024
            const val REQUEST_BIND_APPWIDGET = 1001
            const val REQUEST_CONFIGURE_APPWIDGET = 1002
        }

        private val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

        private val appWidgetHost: AppWidgetHost = AppWidgetHost(context, HOST_ID)

        private val _boundWidgets = MutableStateFlow<List<BoundWidget>>(emptyList())
        val boundWidgets: StateFlow<List<BoundWidget>> = _boundWidgets.asStateFlow()

        init {
            applicationScope.launch(Dispatchers.IO) {
                widgetDataRepository.widgets.collect { persistedWidgets ->
                    val widgets =
                        persistedWidgets.mapNotNull { entry ->
                            val providerInfo = appWidgetManager.getAppWidgetInfo(entry.appWidgetId)
                            if (providerInfo != null) {
                                BoundWidget(
                                    appWidgetId = entry.appWidgetId,
                                    providerInfo = providerInfo,
                                    positionX = entry.positionX,
                                    positionY = entry.positionY,
                                    spanX = entry.spanX,
                                    spanY = entry.spanY,
                                )
                            } else {
                                null
                            }
                        }
                    _boundWidgets.value = widgets
                }
            }
        }

        fun startListening() {
            appWidgetHost.startListening()
        }

        fun stopListening() {
            appWidgetHost.stopListening()
        }

        fun allocateAppWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

        fun deleteAppWidgetId(appWidgetId: Int) {
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            _boundWidgets.value = _boundWidgets.value.filter { it.appWidgetId != appWidgetId }
            applicationScope.launch(Dispatchers.IO) {
                widgetDataRepository.removeWidget(appWidgetId)
            }
        }

        fun createView(appWidgetId: Int): AppWidgetHostView? {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return null
            return appWidgetHost.createView(context, appWidgetId, info)
        }

        /**
         * Attempts to bind a widget. Returns a [WidgetBindResult] indicating:
         * - [WidgetBindResult.Success] if binding succeeded
         * - [WidgetBindResult.NeedsPermission] if user permission is required (launch the intent)
         * - [WidgetBindResult.NeedsConfiguration] if widget needs configuration (launch the intent)
         */
        fun bindWidget(
            appWidgetId: Int,
            providerInfo: AppWidgetProviderInfo,
        ): WidgetBindResult {
            val bound =
                appWidgetManager.bindAppWidgetIdIfAllowed(
                    appWidgetId,
                    providerInfo.provider,
                )
            return if (bound) {
                addBoundWidget(appWidgetId, providerInfo)
                if (providerInfo.configure != null) {
                    WidgetBindResult.NeedsConfiguration(
                        createConfigureIntent(appWidgetId),
                    )
                } else {
                    WidgetBindResult.Success
                }
            } else {
                WidgetBindResult.NeedsPermission(
                    createBindPermissionIntent(appWidgetId, providerInfo),
                )
            }
        }

        /** Call this after the user grants bind permission via the system dialog. */
        fun onBindPermissionGranted(
            appWidgetId: Int,
            providerInfo: AppWidgetProviderInfo,
        ): WidgetBindResult {
            addBoundWidget(appWidgetId, providerInfo)
            return if (providerInfo.configure != null) {
                WidgetBindResult.NeedsConfiguration(
                    createConfigureIntent(appWidgetId),
                )
            } else {
                WidgetBindResult.Success
            }
        }

        /** Call this after widget configuration completes successfully. */
        fun onConfigurationComplete(appWidgetId: Int) {
            // Widget is now fully configured and ready to display
        }

        private fun addBoundWidget(
            appWidgetId: Int,
            providerInfo: AppWidgetProviderInfo,
        ) {
            val widget =
                BoundWidget(
                    appWidgetId = appWidgetId,
                    providerInfo = providerInfo,
                    positionX = 0,
                    positionY = 0,
                    spanX = 1,
                    spanY = 1,
                )
            _boundWidgets.value = _boundWidgets.value + widget

            applicationScope.launch(Dispatchers.IO) {
                widgetDataRepository.addWidget(
                    PersistedWidgetEntry(
                        appWidgetId = appWidgetId,
                        providerComponent = providerInfo.provider,
                        minWidth = providerInfo.minWidth,
                        minHeight = providerInfo.minHeight,
                        positionX = 0,
                        positionY = 0,
                        spanX = 1,
                        spanY = 1,
                    ),
                )
            }
        }

        private fun createBindPermissionIntent(
            appWidgetId: Int,
            providerInfo: AppWidgetProviderInfo,
        ): Intent =
            Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
            }

        private fun createConfigureIntent(appWidgetId: Int): Intent {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val configureComponent =
                info?.configure
                    ?: throw IllegalStateException("No configuration activity for widget $appWidgetId")
            return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = configureComponent
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        fun getInstalledProviders(): List<AppWidgetProviderInfo> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appWidgetManager.getInstalledProvidersForProfile(android.os.Process.myUserHandle())
            } else {
                @Suppress("DEPRECATION")
                appWidgetManager.installedProviders
            }

        fun getProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(appWidgetId)
    }

sealed class WidgetBindResult {
    data object Success : WidgetBindResult()

    data class NeedsPermission(
        val intent: Intent,
    ) : WidgetBindResult()

    data class NeedsConfiguration(
        val intent: Intent,
    ) : WidgetBindResult()
}

data class BoundWidget(
    val appWidgetId: Int,
    val providerInfo: AppWidgetProviderInfo,
    val positionX: Int = 0,
    val positionY: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
)
