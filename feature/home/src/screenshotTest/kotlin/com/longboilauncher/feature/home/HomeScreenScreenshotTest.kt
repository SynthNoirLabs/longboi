package com.longboilauncher.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.feature.home.HomeState
import com.longboilauncher.app.core.model.AppEntry

class HomeScreenScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun HomeScreenPreview() {
        LongboiLauncherTheme {
            HomeScreen(
                state = HomeState(
                    favorites = listOf(
                        AppEntry(
                            packageName = "com.android.settings",
                            label = "Settings",
                            className = "com.android.settings.Settings"
                        ),
                        AppEntry(
                            packageName = "com.android.chrome",
                            label = "Chrome",
                            className = "com.google.android.apps.chrome.Main"
                        )
                    )
                ),
                onEvent = {}
            )
        }
    }
}
