package com.longboilauncher.app.feature.settingsui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.settings.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val theme: String = "system",
    val hapticsEnabled: Boolean = true,
    val showNotifications: Boolean = true,
    val gestureSwipeUp: String = "all_apps",
    val gestureSwipeDown: String = "notifications",
    val density: String = "default"
)

sealed class SettingsEvent {
    data class SetTheme(val theme: String) : SettingsEvent()
    data class SetHapticsEnabled(val enabled: Boolean) : SettingsEvent()
    data class SetShowNotifications(val show: Boolean) : SettingsEvent()
    data class SetGestureSwipeUp(val action: String) : SettingsEvent()
    data class SetGestureSwipeDown(val action: String) : SettingsEvent()
    data class SetDensity(val density: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = combine(
        preferencesRepository.theme,
        preferencesRepository.hapticsEnabled,
        preferencesRepository.showNotifications,
        preferencesRepository.gestureSwipeUp,
        preferencesRepository.gestureSwipeDown
    ) { theme, haptics, notifications, swipeUp, swipeDown ->
        SettingsState(
            theme = theme,
            hapticsEnabled = haptics,
            showNotifications = notifications,
            gestureSwipeUp = swipeUp,
            gestureSwipeDown = swipeDown,
            density = "default" // TODO: Add density when fixed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.SetTheme -> preferencesRepository.setTheme(event.theme)
                is SettingsEvent.SetHapticsEnabled -> preferencesRepository.setHapticsEnabled(event.enabled)
                is SettingsEvent.SetShowNotifications -> preferencesRepository.setShowNotifications(event.show)
                is SettingsEvent.SetGestureSwipeUp -> preferencesRepository.setGestureSwipeUp(event.action)
                is SettingsEvent.SetGestureSwipeDown -> preferencesRepository.setGestureSwipeDown(event.action)
                is SettingsEvent.SetDensity -> preferencesRepository.setDensity(event.density)
            }
        }
    }
}
