package com.longboilauncher.app.feature.searchui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.compose.runtime.Immutable
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed class SearchResult {
    data class AppResult(
        val app: AppEntry,
    ) : SearchResult()

    data class ShortcutResult(
        val app: AppEntry,
        val shortcut: String,
        val shortcutId: String,
    ) : SearchResult()

    data class CalculatorResult(
        val expression: String,
        val result: String,
    ) : SearchResult()

    data class SettingsShortcutResult(
        val title: String,
        val key: String,
        val destination: String,
    ) : SearchResult()
}

@Immutable
data class SearchState(
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
)

sealed class SearchEvent {
    data class UpdateSearchQuery(
        val query: String,
    ) : SearchEvent()

    data class LaunchApp(
        val app: AppEntry,
    ) : SearchEvent()

    data class LaunchShortcut(
        val app: AppEntry,
        val shortcutId: String,
    ) : SearchEvent()

    data class OpenSettings(
        val destination: String,
    ) : SearchEvent()
}

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val appCatalogRepository: AppCatalogRepository,
        private val favoritesRepository: FavoritesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchState())
        val uiState: StateFlow<SearchState> = _uiState.asStateFlow()

        init {
            @OptIn(FlowPreview::class)
            combine(
                appCatalogRepository.apps,
                favoritesRepository.hiddenApps,
                favoritesRepository.favorites,
                _uiState.map { it.searchQuery }.distinctUntilChanged().debounce(150),
            ) { apps, hiddenApps, favorites, query ->
                if (query.isBlank()) {
                    emptyList<SearchResult>()
                } else {
                    val results = mutableListOf<SearchResult>()
                    val favoritePackageNames = favorites.map { it.appEntry.packageName }.toSet()

                    // Calculator result (highest priority if valid)
                    evaluateCalculator(query)?.let { result ->
                        results.add(SearchResult.CalculatorResult(query, result))
                    }

                    // Settings shortcuts
                    results.addAll(findSettingsShortcuts(query))

                    // App results
                    val appResults =
                        apps
                            .filter { app ->
                                val isHidden = app.packageName in hiddenApps
                                val matchesQuery = matchesSearch(app, query)
                                !isHidden && matchesQuery && app.isEnabled
                            }.sortedWith(
                                compareByDescending<AppEntry> {
                                    it.label.lowercase().startsWith(query.lowercase())
                                }.thenByDescending {
                                    if (it.packageName in favoritePackageNames) 1 else 0
                                }.thenByDescending {
                                    calculateMatchScore(it.label, query)
                                }.thenBy {
                                    it.label.lowercase()
                                },
                            ).take(12)

                    results.addAll(appResults.map { SearchResult.AppResult(it) })

                    // TODO: App shortcuts (future)

                    results.take(20)
                }
            }.onEach { results ->
                _uiState.update { it.copy(searchResults = results) }
            }.launchIn(viewModelScope)
        }

        fun onEvent(event: SearchEvent) {
            when (event) {
                is SearchEvent.UpdateSearchQuery -> _uiState.update { it.copy(searchQuery = event.query) }
                is SearchEvent.LaunchApp -> appCatalogRepository.launchApp(event.app)
                is SearchEvent.LaunchShortcut -> appCatalogRepository.launchShortcut(event.app, event.shortcutId)
                is SearchEvent.OpenSettings -> appCatalogRepository.openSettings(event.destination)
            }
        }

        private fun matchesSearch(
            app: AppEntry,
            query: String,
        ): Boolean {
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

        private fun evaluateCalculator(query: String): String? {
            val clean = query.trim().replace(" ", "")
            // Simple arithmetic: +, -, *, /
            val regex = Regex("""^(-?\d+(?:\.\d+)?)([+\-*/])(-?\d+(?:\.\d+)?)$""")
            val match = regex.matchEntire(clean) ?: return null
            val (a, op, b) = match.destructured
            return try {
                val result =
                    when (op) {
                        "+" -> a.toDouble() + b.toDouble()
                        "-" -> a.toDouble() - b.toDouble()
                        "*" -> a.toDouble() * b.toDouble()
                        "/" -> a.toDouble() / b.toDouble()
                        else -> return null
                    }
                if (result == result.toLong().toDouble()) {
                    result.toLong().toString()
                } else {
                    "%.2f".format(result).trimEnd('0').trimEnd('.')
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun findSettingsShortcuts(query: String): List<SearchResult.SettingsShortcutResult> {
            val lowerQuery = query.lowercase()
            val shortcuts =
                listOf(
                    SearchResult.SettingsShortcutResult("Wi-Fi", "wifi", "wifi"),
                    SearchResult.SettingsShortcutResult("Bluetooth", "bluetooth", "bluetooth"),
                    SearchResult.SettingsShortcutResult("Display", "display", "display"),
                    SearchResult.SettingsShortcutResult("Sound", "sound", "sound"),
                    SearchResult.SettingsShortcutResult("Apps", "apps", "apps"),
                    SearchResult.SettingsShortcutResult("Battery", "battery", "battery"),
                    SearchResult.SettingsShortcutResult("Storage", "storage", "storage"),
                    SearchResult.SettingsShortcutResult("Security", "security", "security"),
                )
            return shortcuts.filter { it.title.lowercase().contains(lowerQuery) || it.key.contains(lowerQuery) }
        }

        private fun fuzzyMatch(
            text: String,
            query: String,
        ): Boolean {
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

        private fun calculateMatchScore(
            label: String,
            query: String,
        ): Int {
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
