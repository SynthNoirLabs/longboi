package com.longboilauncher.app.feature.home

import android.content.pm.ShortcutInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.common.ClockTicker
import com.longboilauncher.app.core.common.NotificationState
import com.longboilauncher.app.core.common.NowProvider
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

enum class LauncherSurface {
    HOME,
    ALL_APPS,
    SEARCH,
    SETTINGS,
}

data class HomeState(
    val isLoading: Boolean = true,
    val currentSurface: LauncherSurface = LauncherSurface.HOME,
    val favorites: List<FavoriteEntry> = emptyList(),
    val apps: List<AppEntry> = emptyList(),
    val glanceData: GlanceHeaderData =
        GlanceHeaderData(
            currentTime = "",
            currentDate = "",
            nextEvent = null,
            weather = null,
            nextAlarm = null,
            nowPlaying = null,
        ),
    val popupApp: AppEntry? = null,
    val popupShortcuts: List<ShortcutInfo> = emptyList(),
)

sealed class HomeEvent {
    data class NavigateTo(
        val surface: LauncherSurface,
    ) : HomeEvent()

    data class LaunchApp(
        val app: AppEntry,
    ) : HomeEvent()

    data class LaunchFavorite(
        val favorite: FavoriteEntry,
    ) : HomeEvent()

    data class AddToFavorites(
        val app: AppEntry,
    ) : HomeEvent()

    data class RemoveFromFavorites(
        val favoriteId: String,
    ) : HomeEvent()

    data class ReorderFavorites(
        val favoriteIds: List<String>,
    ) : HomeEvent()

    object RefreshCatalog : HomeEvent()

    data class ShowPopup(
        val app: AppEntry,
    ) : HomeEvent()

    object HidePopup : HomeEvent()

    data class LaunchShortcut(
        val app: AppEntry,
        val shortcutId: String,
    ) : HomeEvent()

    object ShowAppInfo : HomeEvent()

    object UninstallApp : HomeEvent()

    object HideApp : HomeEvent()
}

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val appCatalogRepository: AppCatalogRepository,
        private val favoritesRepository: FavoritesRepository,
        private val nowProvider: NowProvider,
        private val clockTicker: ClockTicker,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeState())
        val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

        private val timeFormatter = DateTimeFormatter.ofPattern("h:mm")
        private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

        init {
            // Collect favorites and enrich with notification badge counts
            favoritesRepository.favorites
                .onEach { favs ->
                    val counts = NotificationState.counts.value
                    val enriched =
                        favs.map { fav ->
                            val count = counts[fav.appEntry.packageName] ?: 0
                            fav.copy(notificationCount = count, hasNotifications = count > 0)
                        }
                    _uiState.update { it.copy(favorites = enriched) }
                }.launchIn(viewModelScope)

            // When notification counts change, re-enrich the current favorites list
            NotificationState.counts
                .onEach { counts ->
                    _uiState.update { state ->
                        state.copy(
                            favorites =
                                state.favorites.map { fav ->
                                    val count = counts[fav.appEntry.packageName] ?: 0
                                    fav.copy(notificationCount = count, hasNotifications = count > 0)
                                },
                        )
                    }
                }.launchIn(viewModelScope)

            // Collect apps
            appCatalogRepository.apps
                .onEach { apps -> _uiState.update { it.copy(apps = apps) } }
                .launchIn(viewModelScope)

            // Clock updates
            this.clockTicker
                .tick(1.seconds)
                .onEach { _: Unit ->
                    val now = nowProvider.now().atZone(java.time.ZoneId.systemDefault())
                    _uiState.update { state: HomeState ->
                        state.copy(
                            glanceData =
                                state.glanceData.copy(
                                    currentTime = now.format(timeFormatter),
                                    currentDate = now.format(dateFormatter),
                                ),
                        )
                    }
                }.launchIn(viewModelScope)

            onEvent(HomeEvent.RefreshCatalog)
        }

        fun onEvent(event: HomeEvent) {
            when (event) {
                is HomeEvent.NavigateTo -> _uiState.update { it.copy(currentSurface = event.surface) }
                is HomeEvent.LaunchApp -> appCatalogRepository.launchApp(event.app)
                is HomeEvent.LaunchFavorite -> appCatalogRepository.launchApp(event.favorite.appEntry)
                is HomeEvent.AddToFavorites ->
                    viewModelScope.launch {
                        favoritesRepository.addFavorite(event.app)
                    }
                is HomeEvent.RemoveFromFavorites ->
                    viewModelScope.launch {
                        favoritesRepository.removeFavorite(event.favoriteId)
                    }
                is HomeEvent.ReorderFavorites ->
                    viewModelScope.launch {
                        favoritesRepository.reorderFavorites(event.favoriteIds)
                    }
                HomeEvent.RefreshCatalog ->
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true) }
                        appCatalogRepository.refreshAppCatalog()
                        _uiState.update { it.copy(isLoading = false) }
                    }
                is HomeEvent.ShowPopup -> {
                    _uiState.update { it.copy(popupApp = event.app) }
                    viewModelScope.launch {
                        val shortcuts = appCatalogRepository.getAppShortcuts(event.app)
                        _uiState.update { it.copy(popupShortcuts = shortcuts) }
                    }
                }
                HomeEvent.HidePopup -> {
                    _uiState.update { it.copy(popupApp = null, popupShortcuts = emptyList()) }
                }
                is HomeEvent.LaunchShortcut -> appCatalogRepository.launchShortcut(event.app, event.shortcutId)
                HomeEvent.ShowAppInfo -> {
                    // TODO: Open app info screen
                }
                HomeEvent.UninstallApp -> {
                    // TODO: Start uninstall intent
                }
                HomeEvent.HideApp -> {
                    _uiState.value.popupApp?.let { app ->
                        viewModelScope.launch {
                            favoritesRepository.hideApp(app.packageName)
                            _uiState.update { it.copy(popupApp = null, popupShortcuts = emptyList()) }
                        }
                    }
                }
            }
        }
    }
