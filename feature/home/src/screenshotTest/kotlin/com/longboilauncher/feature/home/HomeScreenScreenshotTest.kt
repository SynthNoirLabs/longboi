package com.longboilauncher.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.feature.home.HomeEvent
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.feature.home.HomeState

class HomeScreenScreenshotTest {
    @Preview(showBackground = true)
    @Composable
    fun HomeScreenPreview() {
        LongboiLauncherTheme {
            HomeScreen(
                uiState =
                    HomeState(
                        isLoading = false,
                        favorites =
                            listOf(
                                FavoriteEntry(
                                    id = "fav_settings",
                                    appEntry =
                                        AppEntry(
                                            packageName = "com.android.settings",
                                            className = "com.android.settings.Settings",
                                            label = "Settings",
                                        ),
                                    position = 0,
                                ),
                                FavoriteEntry(
                                    id = "fav_chrome",
                                    appEntry =
                                        AppEntry(
                                            packageName = "com.android.chrome",
                                            className = "com.google.android.apps.chrome.Main",
                                            label = "Chrome",
                                        ),
                                    position = 1,
                                ),
                            ),
                    ),
                onEvent = { _: HomeEvent -> },
            )
        }
    }

    @Preview(showBackground = true, name = "Home Screen with Notification Badges")
    @Composable
    fun HomeScreenWithNotificationsPreview() {
        LongboiLauncherTheme {
            HomeScreen(
                uiState =
                    HomeState(
                        isLoading = false,
                        favorites =
                            listOf(
                                FavoriteEntry(
                                    id = "fav_settings",
                                    appEntry =
                                        AppEntry(
                                            packageName = "com.android.settings",
                                            className = "com.android.settings.Settings",
                                            label = "Settings",
                                        ),
                                    position = 0,
                                    notificationCount = 3,
                                    hasNotifications = true,
                                ),
                                FavoriteEntry(
                                    id = "fav_chrome",
                                    appEntry =
                                        AppEntry(
                                            packageName = "com.android.chrome",
                                            className = "com.google.android.apps.chrome.Main",
                                            label = "Chrome",
                                        ),
                                    position = 1,
                                ),
                                FavoriteEntry(
                                    id = "fav_messages",
                                    appEntry =
                                        AppEntry(
                                            packageName = "com.google.android.apps.messaging",
                                            className = "com.google.android.apps.messaging.Main",
                                            label = "Messages",
                                        ),
                                    position = 2,
                                    notificationCount = 12,
                                    hasNotifications = true,
                                ),
                            ),
                    ),
                onEvent = { _: HomeEvent -> },
            )
        }
    }
}
