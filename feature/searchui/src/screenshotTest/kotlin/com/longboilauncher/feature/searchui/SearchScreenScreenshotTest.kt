package com.longboilauncher.feature.searchui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.feature.searchui.SearchResult
import com.longboilauncher.app.feature.searchui.SearchScreen
import com.longboilauncher.app.feature.searchui.SearchState

class SearchScreenScreenshotTest {
    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun SearchScreen_WithResults_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            SearchScreen(
                uiState =
                    SearchState(
                        searchQuery = "calc",
                        searchResults =
                            listOf(
                                SearchResult.CalculatorResult("123+456", "579"),
                                SearchResult.SettingsShortcutResult("Wi-Fi", "wifi", "wifi"),
                                SearchResult.AppResult(
                                    AppEntry(
                                        packageName = "com.android.calculator2",
                                        className = "com.android.calculator2.Calculator",
                                        label = "Calculator",
                                    ),
                                ),
                            ),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun SearchScreen_WithResults_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            SearchScreen(
                uiState =
                    SearchState(
                        searchQuery = "calc",
                        searchResults =
                            listOf(
                                SearchResult.CalculatorResult("123+456", "579"),
                                SearchResult.AppResult(
                                    AppEntry(
                                        packageName = "com.android.calculator2",
                                        className = "com.android.calculator2.Calculator",
                                        label = "Calculator",
                                    ),
                                ),
                            ),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun SearchScreen_Empty_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            SearchScreen(
                uiState =
                    SearchState(
                        searchQuery = "",
                        searchResults = emptyList(),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun SearchScreen_NoResults() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            SearchScreen(
                uiState =
                    SearchState(
                        searchQuery = "xyznonexistent",
                        searchResults = emptyList(),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun SearchScreen_SettingsResults() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            SearchScreen(
                uiState =
                    SearchState(
                        searchQuery = "wifi",
                        searchResults =
                            listOf(
                                SearchResult.SettingsShortcutResult("Wi-Fi", "wifi", "wifi"),
                                SearchResult.SettingsShortcutResult("Bluetooth", "bluetooth", "bluetooth"),
                            ),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
            )
        }
    }
}
