package com.longboilauncher.app.feature.allapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllAppsState(
    val searchQuery: String = "",
    val selectedProfile: ProfileFilter = ProfileFilter.ALL,
    val filteredApps: List<AppEntry> = emptyList(),
    val appSections: Map<String, List<AppEntry>> = emptyMap(),
    val sectionIndices: Map<String, Int> = emptyMap()
)

sealed class AllAppsEvent {
    data class UpdateSearchQuery(val query: String) : AllAppsEvent()
    data class SelectProfile(val profile: ProfileFilter) : AllAppsEvent()
    data class AddToFavorites(val app: AppEntry) : AllAppsEvent()
    data class HideApp(val packageName: String) : AllAppsEvent()
    data class LaunchApp(val app: AppEntry) : AllAppsEvent()
}

@HiltViewModel
class AllAppsViewModel @Inject constructor(
    private val appCatalogRepository: AppCatalogRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllAppsState())
    val uiState: StateFlow<AllAppsState> = _uiState.asStateFlow()

    init {
        combine(
            appCatalogRepository.apps,
            favoritesRepository.hiddenApps,
            _uiState.map { it.searchQuery }.distinctUntilChanged(),
            _uiState.map { it.selectedProfile }.distinctUntilChanged()
        ) { apps, hiddenApps, query, profile ->
            val filtered = apps.filter { app ->
                val isHidden = app.packageName in hiddenApps
                val matchesProfile = when (profile) {
                    ProfileFilter.ALL -> true
                    ProfileFilter.PERSONAL -> app.profile == ProfileType.PERSONAL
                    ProfileFilter.WORK -> app.profile == ProfileType.WORK
                    ProfileFilter.PRIVATE -> app.profile == ProfileType.PRIVATE
                }
                val matchesQuery = query.isBlank() ||
                    app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

                !isHidden && matchesProfile && matchesQuery && app.isEnabled
            }

            val sections = filtered.groupBy { app ->
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
                    sectionIndices = indices
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: AllAppsEvent) {
        when (event) {
            is AllAppsEvent.UpdateSearchQuery -> _uiState.update { it.copy(searchQuery = event.query) }
            is AllAppsEvent.SelectProfile -> _uiState.update { it.copy(selectedProfile = event.profile) }
            is AllAppsEvent.AddToFavorites -> viewModelScope.launch {
                favoritesRepository.addFavorite(event.app)
            }
            is AllAppsEvent.HideApp -> viewModelScope.launch {
                favoritesRepository.hideApp(event.packageName)
            }
            is AllAppsEvent.LaunchApp -> appCatalogRepository.launchApp(event.app)
        }
    }
}

enum class ProfileFilter {
    ALL,
    PERSONAL,
    WORK,
    PRIVATE
}
