package com.longboilauncher.app.feature.home

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.common.ClockTicker
import com.longboilauncher.app.core.common.NowProvider
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ShortcutUiModel
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

@Immutable
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
    val popupShortcuts: List<ShortcutUiModel> = emptyList(),
    val error: String? = null,
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

    data object RefreshCatalog : HomeEvent()

    data class ShowPopup(
        val app: AppEntry,
    ) : HomeEvent()

    data object HidePopup : HomeEvent()

    data class LaunchShortcut(
        val app: AppEntry,
        val shortcutId: String,
    ) : HomeEvent()

    data object ShowAppInfo : HomeEvent()

    data object UninstallApp : HomeEvent()

    data object HideApp : HomeEvent()
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
            // Collect favorites
            favoritesRepository.favorites
                .onEach { favs -> _uiState.update { it.copy(favorites = favs) } }
                .launchIn(viewModelScope)

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
                        try {
                            favoritesRepository.addFavorite(event.app)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add favorite", e)
                        }
                    }
                is HomeEvent.RemoveFromFavorites ->
                    viewModelScope.launch {
                        try {
                            favoritesRepository.removeFavorite(event.favoriteId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to remove favorite", e)
                        }
                    }
                is HomeEvent.ReorderFavorites ->
                    viewModelScope.launch {
                        try {
                            favoritesRepository.reorderFavorites(event.favoriteIds)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to reorder favorites", e)
                        }
                    }
                HomeEvent.RefreshCatalog ->
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true) }
                        try {
                            appCatalogRepository.refreshAppCatalog()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to refresh catalog", e)
                            _uiState.update { it.copy(error = e.message) }
                        } finally {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                is HomeEvent.ShowPopup -> {
                    _uiState.update { it.copy(popupApp = event.app) }
                    viewModelScope.launch {
                        try {
                            val shortcuts = appCatalogRepository.getAppShortcuts(event.app)
                            _uiState.update {
                                it.copy(
                                    popupShortcuts = shortcuts.map { s ->
                                        ShortcutUiModel(
                                            id = s.id,
                                            label = s.shortLabel?.toString() ?: "",
                                            iconUri = s.iconUri,
                                            intent = s.intent,
                                        )
                                    },
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to load shortcuts", e)
                        }
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
                            try {
                                favoritesRepository.hideApp(app.packageName)
                                _uiState.update { it.copy(popupApp = null, popupShortcuts = emptyList()) }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to hide app", e)
                            }
                        }
                    }
                }
            }
        }

        companion object {
            private const val TAG = "HomeViewModel"
        }
    }
