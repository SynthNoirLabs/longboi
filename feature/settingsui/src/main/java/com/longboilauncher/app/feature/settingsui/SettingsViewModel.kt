package com.longboilauncher.app.feature.settingsui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.model.ThemeType
import com.longboilauncher.app.core.settings.PreferencesRepository
import com.longboilauncher.app.core.icons.IconPackInfo
import com.longboilauncher.app.core.icons.IconPackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val theme: ThemeType = ThemeType.MATERIAL_YOU,
    val iconPackPackageName: String = "",
    val installedIconPacks: List<IconPackInfo> = emptyList(),
    val hapticsEnabled: Boolean = true,
    val showNotifications: Boolean = true,
    val gestureSwipeUp: String = "all_apps",
    val gestureSwipeDown: String = "notifications",
    val density: String = "default",
)

sealed class SettingsEvent {
    data class SetTheme(
        val theme: ThemeType,
    ) : SettingsEvent()

    data class SetIconPack(
        val packageName: String,
    ) : SettingsEvent()

    data class SetHapticsEnabled(
        val enabled: Boolean,
    ) : SettingsEvent()

    data class SetShowNotifications(
        val show: Boolean,
    ) : SettingsEvent()

    data class SetGestureSwipeUp(
        val action: String,
    ) : SettingsEvent()

    data class SetGestureSwipeDown(
        val action: String,
    ) : SettingsEvent()

    data class SetDensity(
        val density: String,
    ) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
        private val iconPackManager: IconPackManager,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsState> =
            combine(
                combine(
                    preferencesRepository.themeType,
                    preferencesRepository.iconPackPackageName,
                    ::Pair
                ),
                preferencesRepository.hapticsEnabled,
                preferencesRepository.showNotifications,
                preferencesRepository.gestureSwipeUp,
                preferencesRepository.gestureSwipeDown,
            ) { (theme, iconPack), haptics, notifications, swipeUp, swipeDown ->
                SettingsState(
                    theme = theme,
                    iconPackPackageName = iconPack,
                    installedIconPacks = iconPackManager.getInstalledIconPacks(),
                    hapticsEnabled = haptics,
                    showNotifications = notifications,
                    gestureSwipeUp = swipeUp,
                    gestureSwipeDown = swipeDown,
                    density = "default",
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsState(),
            )

        fun onEvent(event: SettingsEvent) {
            viewModelScope.launch {
                when (event) {
                    is SettingsEvent.SetTheme -> preferencesRepository.setTheme(event.theme.key)
                    is SettingsEvent.SetIconPack -> preferencesRepository.setIconPack(event.packageName)
                    is SettingsEvent.SetHapticsEnabled -> preferencesRepository.setHapticsEnabled(event.enabled)
                    is SettingsEvent.SetShowNotifications -> preferencesRepository.setShowNotifications(event.show)
                    is SettingsEvent.SetGestureSwipeUp -> preferencesRepository.setGestureSwipeUp(event.action)
                    is SettingsEvent.SetGestureSwipeDown -> preferencesRepository.setGestureSwipeDown(event.action)
                    is SettingsEvent.SetDensity -> preferencesRepository.setDensity(event.density)
                }
            }
        }
    }
