package com.longboilauncher.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.CalendarEvent
import com.longboilauncher.app.core.model.WeatherInfo
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.feature.home.HomeState

class HomeScreenScreenshotTest {
    private val testFavorites =
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
            FavoriteEntry(
                id = "fav_spotify",
                appEntry =
                    AppEntry(
                        packageName = "com.spotify.music",
                        className = "com.spotify.music.Main",
                        label = "Spotify",
                    ),
                position = 2,
                isPlaying = true,
            ),
            FavoriteEntry(
                id = "fav_messages",
                appEntry =
                    AppEntry(
                        packageName = "com.google.android.apps.messaging",
                        className = "com.google.android.apps.messaging.Main",
                        label = "Messages",
                    ),
                position = 3,
                hasNotifications = true,
                notificationCount = 3,
            ),
        )

    private val testGlance =
        GlanceHeaderData(
            currentTime = "10:30",
            currentDate = "Tuesday, Feb 18",
            nextEvent = CalendarEvent(title = "Team standup", time = "11:00 AM", color = 0xFF4285F4.toInt()),
            weather = WeatherInfo(temperature = "72°F", condition = "Sunny"),
        )

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun HomeScreen_WithFavorites_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            HomeScreen(
                uiState =
                    HomeState(
                        isLoading = false,
                        favorites = testFavorites,
                        glanceData = testGlance,
                    ),
                onEvent = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun HomeScreen_WithFavorites_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            HomeScreen(
                uiState =
                    HomeState(
                        isLoading = false,
                        favorites = testFavorites,
                        glanceData = testGlance,
                    ),
                onEvent = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun HomeScreen_Empty_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            HomeScreen(
                uiState =
                    HomeState(
                        isLoading = false,
                        favorites = emptyList(),
                        glanceData =
                            GlanceHeaderData(
                                currentTime = "10:30",
                                currentDate = "Tuesday, Feb 18",
                            ),
                    ),
                onEvent = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun HomeScreen_Loading() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            HomeScreen(
                uiState = HomeState(isLoading = true),
                onEvent = {},
            )
        }
    }
}
