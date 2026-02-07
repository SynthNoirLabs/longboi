package com.longboilauncher.app.feature.allapps

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.settings.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class AllAppsState(
    val searchQuery: String = "",
    val selectedProfile: ProfileFilter = ProfileFilter.ALL,
    val filteredApps: List<AppEntry> = emptyList(),
    val appSections: Map<String, List<AppEntry>> = emptyMap(),
    val sectionIndices: Map<String, Int> = emptyMap(),
)

sealed class AllAppsEvent {
    data class UpdateSearchQuery(
        val query: String,
    ) : AllAppsEvent()

    data class SelectProfile(
        val profile: ProfileFilter,
    ) : AllAppsEvent()

    data class AddToFavorites(
        val app: AppEntry,
    ) : AllAppsEvent()

    data class HideApp(
        val packageName: String,
    ) : AllAppsEvent()

    data class LaunchApp(
        val app: AppEntry,
    ) : AllAppsEvent()
}

@HiltViewModel
class AllAppsViewModel
    @Inject
    constructor(
        private val appCatalogRepository: AppCatalogRepository,
        private val favoritesRepository: FavoritesRepository,
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AllAppsState())
        val uiState: StateFlow<AllAppsState> = _uiState.asStateFlow()

        init {
            combine(
                appCatalogRepository.apps,
                favoritesRepository.hiddenApps,
                _uiState.map { it.searchQuery }.distinctUntilChanged(),
                _uiState.map { it.selectedProfile }.distinctUntilChanged(),
            ) { apps, hiddenApps, query, profile ->
                val filtered =
                    apps.filter { app ->
                        val isHidden = app.packageName in hiddenApps
                        val matchesProfile =
                            when (profile) {
                                ProfileFilter.ALL -> true
                                ProfileFilter.PERSONAL -> app.profile == ProfileType.PERSONAL
                                ProfileFilter.WORK -> app.profile == ProfileType.WORK
                                ProfileFilter.PRIVATE -> app.profile == ProfileType.PRIVATE
                            }
                        val matchesQuery =
                            query.isBlank() ||
                                app.label.contains(query, ignoreCase = true) ||
                                app.packageName.contains(query, ignoreCase = true)

                        !isHidden && matchesProfile && matchesQuery && app.isEnabled
                    }

                val sections =
                    filtered
                        .groupBy { app ->
                            val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
                            if (firstChar.isLetter()) firstChar.toString() else "#"
                        }.toSortedMap()

                var index = 0
                val indices = mutableMapOf<String, Int>()
                sections.forEach { (letter, appsInSection) ->
                    if (appsInSection.isNotEmpty()) {
                        indices[letter] = index
                        index += 1 + appsInSection.size // header + apps
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        filteredApps = filtered,
                        appSections = sections,
                        sectionIndices = indices,
                    )
                }
            }.launchIn(viewModelScope)
        }

        fun onEvent(event: AllAppsEvent) {
            when (event) {
                is AllAppsEvent.UpdateSearchQuery -> _uiState.update { it.copy(searchQuery = event.query) }
                is AllAppsEvent.SelectProfile -> _uiState.update { it.copy(selectedProfile = event.profile) }
                is AllAppsEvent.AddToFavorites ->
                    viewModelScope.launch {
                        try {
                            favoritesRepository.addFavorite(event.app)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add favorite", e)
                        }
                    }
                is AllAppsEvent.HideApp ->
                    viewModelScope.launch {
                        try {
                            favoritesRepository.hideApp(event.packageName)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to hide app", e)
                        }
                    }
                is AllAppsEvent.LaunchApp -> appCatalogRepository.launchApp(event.app)
            }
        }

        companion object {
            private const val TAG = "AllAppsViewModel"
        }
    }

enum class ProfileFilter {
    ALL,
    PERSONAL,
    WORK,
    PRIVATE,
}

internal fun scrubberIndexForY(
    y: Float,
    height: Float,
    itemCount: Int,
): Int {
    if (itemCount <= 0) return 0
    if (height <= 0f) return 0

    val clampedY = y.coerceIn(0f, height)
    val ratio = (clampedY / height).coerceIn(0f, 1f)
    return (ratio * itemCount).toInt().coerceIn(0, itemCount - 1)
}
