package com.longboilauncher.app.feature.widgetpicker

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.widgets.WidgetBindResult
import com.longboilauncher.app.core.widgets.WidgetHost
import com.longboilauncher.app.core.widgets.WidgetInfo
import com.longboilauncher.app.core.widgets.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WidgetPickerState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val widgetsByApp: Map<String, List<WidgetInfo>> = emptyMap(),
    val expandedApps: Set<String> = emptySet(),
    val pendingWidgetId: Int? = null,
    val pendingWidgetInfo: WidgetInfo? = null,
)

sealed class WidgetPickerEvent {
    data class UpdateSearchQuery(
        val query: String,
    ) : WidgetPickerEvent()

    data class ToggleAppExpanded(
        val appLabel: String,
    ) : WidgetPickerEvent()

    data class SelectWidget(
        val widget: WidgetInfo,
    ) : WidgetPickerEvent()

    data class OnBindPermissionResult(
        val granted: Boolean,
    ) : WidgetPickerEvent()

    data class OnConfigurationResult(
        val success: Boolean,
    ) : WidgetPickerEvent()
}

sealed class WidgetPickerEffect {
    data class LaunchBindPermission(
        val intent: Intent,
        val requestCode: Int,
    ) : WidgetPickerEffect()

    data class LaunchConfiguration(
        val intent: Intent,
        val requestCode: Int,
    ) : WidgetPickerEffect()

    data class WidgetAdded(
        val appWidgetId: Int,
    ) : WidgetPickerEffect()

    data object WidgetAddFailed : WidgetPickerEffect()
}

@HiltViewModel
class WidgetPickerViewModel
    @Inject
    constructor(
        private val widgetRepository: WidgetRepository,
        private val widgetHost: WidgetHost,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WidgetPickerState())
        val uiState: StateFlow<WidgetPickerState> = _uiState.asStateFlow()

        private val _effects = Channel<WidgetPickerEffect>(Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        init {
            loadWidgets()
        }

        private fun loadWidgets() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                widgetRepository.refreshAvailableWidgets()
                val widgetsByApp = widgetRepository.getWidgetsByApp()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        widgetsByApp = widgetsByApp,
                    )
                }
            }
        }

        fun onEvent(event: WidgetPickerEvent) {
            when (event) {
                is WidgetPickerEvent.UpdateSearchQuery -> {
                    _uiState.update { it.copy(searchQuery = event.query) }
                }
                is WidgetPickerEvent.ToggleAppExpanded -> {
                    _uiState.update { state ->
                        val newExpanded =
                            if (event.appLabel in state.expandedApps) {
                                state.expandedApps - event.appLabel
                            } else {
                                state.expandedApps + event.appLabel
                            }
                        state.copy(expandedApps = newExpanded)
                    }
                }
                is WidgetPickerEvent.SelectWidget -> {
                    handleWidgetSelection(event.widget)
                }
                is WidgetPickerEvent.OnBindPermissionResult -> {
                    handleBindPermissionResult(event.granted)
                }
                is WidgetPickerEvent.OnConfigurationResult -> {
                    handleConfigurationResult(event.success)
                }
            }
        }

        private fun handleWidgetSelection(widget: WidgetInfo) {
            val appWidgetId = widgetHost.allocateAppWidgetId()
            _uiState.update {
                it.copy(
                    pendingWidgetId = appWidgetId,
                    pendingWidgetInfo = widget,
                )
            }

            when (val result = widgetHost.bindWidget(appWidgetId, widget.provider)) {
                is WidgetBindResult.Success -> {
                    viewModelScope.launch { _effects.send(WidgetPickerEffect.WidgetAdded(appWidgetId)) }
                    clearPendingWidget()
                }
                is WidgetBindResult.NeedsPermission -> {
                    viewModelScope.launch {
                        _effects.send(
                            WidgetPickerEffect.LaunchBindPermission(
                                intent = result.intent,
                                requestCode = WidgetHost.REQUEST_BIND_APPWIDGET,
                            ),
                        )
                    }
                }
                is WidgetBindResult.NeedsConfiguration -> {
                    viewModelScope.launch {
                        _effects.send(
                            WidgetPickerEffect.LaunchConfiguration(
                                intent = result.intent,
                                requestCode = WidgetHost.REQUEST_CONFIGURE_APPWIDGET,
                            ),
                        )
                    }
                }
            }
        }

        private fun handleBindPermissionResult(granted: Boolean) {
            val pendingId = _uiState.value.pendingWidgetId
            val pendingInfo = _uiState.value.pendingWidgetInfo

            if (pendingId == null || pendingInfo == null) {
                return
            }

            if (granted) {
                when (
                    val result = widgetHost.onBindPermissionGranted(pendingId, pendingInfo.provider)
                ) {
                    is WidgetBindResult.Success -> {
                        viewModelScope.launch {
                            _effects.send(WidgetPickerEffect.WidgetAdded(pendingId))
                        }
                        clearPendingWidget()
                    }
                    is WidgetBindResult.NeedsConfiguration -> {
                        viewModelScope.launch {
                            _effects.send(
                                WidgetPickerEffect.LaunchConfiguration(
                                    intent = result.intent,
                                    requestCode = WidgetHost.REQUEST_CONFIGURE_APPWIDGET,
                                ),
                            )
                        }
                    }
                    is WidgetBindResult.NeedsPermission -> {
                        // Should not happen after permission granted
                        handleWidgetAddFailed()
                    }
                }
            } else {
                handleWidgetAddFailed()
            }
        }

        private fun handleConfigurationResult(success: Boolean) {
            val pendingId = _uiState.value.pendingWidgetId

            if (pendingId == null) {
                return
            }

            if (success) {
                widgetHost.onConfigurationComplete(pendingId)
                viewModelScope.launch { _effects.send(WidgetPickerEffect.WidgetAdded(pendingId)) }
            } else {
                handleWidgetAddFailed()
            }
            clearPendingWidget()
        }

        private fun handleWidgetAddFailed() {
            val pendingId = _uiState.value.pendingWidgetId
            if (pendingId != null) {
                widgetHost.deleteAppWidgetId(pendingId)
            }
            viewModelScope.launch { _effects.send(WidgetPickerEffect.WidgetAddFailed) }
            clearPendingWidget()
        }

        private fun clearPendingWidget() {
            _uiState.update {
                it.copy(
                    pendingWidgetId = null,
                    pendingWidgetInfo = null,
                )
            }
        }

        fun getFilteredWidgets(): Map<String, List<WidgetInfo>> {
            val query = _uiState.value.searchQuery.lowercase()
            if (query.isBlank()) return _uiState.value.widgetsByApp

            return _uiState.value.widgetsByApp
                .mapValues { (_, widgets) ->
                    widgets.filter {
                        it.label.lowercase().contains(query) ||
                            it.appLabel.lowercase().contains(query)
                    }
                }.filterValues { it.isNotEmpty() }
        }
    }
