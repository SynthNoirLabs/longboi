package com.longboilauncher.feature.searchui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.searchui.SearchScreen
import com.longboilauncher.app.feature.searchui.SearchState
import com.longboilauncher.app.feature.searchui.SearchResult
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.feature.searchui.SearchEvent

class SearchScreenScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun SearchScreenPreview() {
        LongboiLauncherTheme {
            SearchScreen(
                uiState = SearchState(
                    searchQuery = "calc",
                    searchResults = listOf(
                        SearchResult.CalculatorResult("123+456", "579"),
                        SearchResult.SettingsShortcutResult("Wi-Fi", "wifi", "wifi"),
                        SearchResult.AppResult(
                            AppEntry(
                                packageName = "com.android.calculator2",
                                className = "com.android.calculator2.Calculator",
                                label = "Calculator"
                            )
                        )
                    )
                ),
                onEvent = { _: SearchEvent -> },
                onAppSelected = {},
                onDismiss = {}
            )
        }
    }
}
