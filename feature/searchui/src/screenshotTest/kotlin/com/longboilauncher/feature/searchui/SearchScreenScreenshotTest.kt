package com.longboilauncher.feature.searchui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.searchui.SearchScreen
import com.longboilauncher.app.feature.searchui.SearchState
import com.longboilauncher.app.core.model.AppEntry

class SearchScreenScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun SearchScreenPreview() {
        LongboiLauncherTheme {
            SearchScreen(
                state = SearchState(
                    searchQuery = "Set",
                    searchResults = listOf(
                        AppEntry("com.android.settings", "Settings", "com.android.settings.Settings")
                    )
                ),
                onEvent = {}
            )
        }
    }
}
