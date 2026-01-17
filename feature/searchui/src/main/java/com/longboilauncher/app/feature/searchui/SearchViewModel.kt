package com.longboilauncher.app.feature.searchui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchState(
    val searchQuery: String = "",
    val searchResults: List<AppEntry> = emptyList()
)

sealed class SearchEvent {
    data class UpdateSearchQuery(val query: String) : SearchEvent()
    data class LaunchApp(val app: AppEntry) : SearchEvent()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val appCatalogRepository: AppCatalogRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchState())
    val uiState: StateFlow<SearchState> = _uiState.asStateFlow()

    init {
        combine(
            appCatalogRepository.apps,
            favoritesRepository.hiddenApps,
            favoritesRepository.favorites,
            _uiState.map { it.searchQuery }.distinctUntilChanged()
        ) { apps, hiddenApps, favorites, query ->
            if (query.isBlank()) {
                emptyList()
            } else {
                val favoritePackageNames = favorites.map { it.appEntry.packageName }.toSet()
                apps.filter { app ->
                    val isHidden = app.packageName in hiddenApps
                    val matchesQuery = matchesSearch(app, query)
                    !isHidden && matchesQuery && app.isEnabled
                }.sortedWith(
                    compareByDescending<AppEntry> {
                        // Exact prefix match first
                        it.label.lowercase().startsWith(query.lowercase())
                    }.thenByDescending {
                        // Then favorites boost
                        if (it.packageName in favoritePackageNames) 1 else 0
                    }.thenByDescending {
                        // Then fuzzy match score
                        calculateMatchScore(it.label, query)
                    }.thenBy {
                        // Then alphabetically
                        it.label.lowercase()
                    }
                ).take(20)
            }
        }.onEach { results ->
            _uiState.update { it.copy(searchResults = results) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.UpdateSearchQuery -> _uiState.update { it.copy(searchQuery = event.query) }
            is SearchEvent.LaunchApp -> appCatalogRepository.launchApp(event.app)
        }
    }

    private fun matchesSearch(app: AppEntry, query: String): Boolean {
        val lowerQuery = query.lowercase()
        val lowerLabel = app.label.lowercase()

        // Direct match
        if (lowerLabel.contains(lowerQuery)) return true

        // Acronym match (e.g., "yt" matches "YouTube")
        val acronym = app.label.filter { it.isUpperCase() || it.isDigit() }.lowercase()
        if (acronym.startsWith(lowerQuery)) return true

        // Word start match (e.g., "tube" matches "YouTube")
        val words = lowerLabel.split(" ", "-", "_")
        if (words.any { it.startsWith(lowerQuery) }) return true

        // Fuzzy match for typos
        if (fuzzyMatch(lowerLabel, lowerQuery)) return true

        return false
    }

    private fun fuzzyMatch(text: String, query: String): Boolean {
        if (query.length < 3) return false

        // Simple fuzzy matching: allow one character difference
        var queryIndex = 0
        var textIndex = 0
        var mismatches = 0

        while (queryIndex < query.length && textIndex < text.length) {
            if (query[queryIndex] == text[textIndex]) {
                queryIndex++
                textIndex++
            } else {
                mismatches++
                if (mismatches > 1) return false
                textIndex++
            }
        }

        return queryIndex == query.length
    }

    private fun calculateMatchScore(label: String, query: String): Int {
        val lowerLabel = label.lowercase()
        val lowerQuery = query.lowercase()

        return when {
            lowerLabel == lowerQuery -> 100
            lowerLabel.startsWith(lowerQuery) -> 90
            lowerLabel.contains(" $lowerQuery") -> 80
            lowerLabel.contains(lowerQuery) -> 70
            else -> 0
        }
    }
}
