package com.longboilauncher.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.components.FavoriteAppItem
import com.longboilauncher.app.core.designsystem.components.GlanceHeader
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AlarmInfo
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.CalendarEvent
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.model.WeatherInfo

class ComponentScreenshotTest {
    private val personalApp =
        AppEntry(
            packageName = "com.example.app",
            className = "com.example.app.Main",
            label = "Example App",
            profile = ProfileType.PERSONAL,
        )

    private val workApp =
        AppEntry(
            packageName = "com.work.slack",
            className = "com.work.slack.Main",
            label = "Slack",
            profile = ProfileType.WORK,
        )

    private val privateApp =
        AppEntry(
            packageName = "com.private.vault",
            className = "com.private.vault.Main",
            label = "Private Vault",
            profile = ProfileType.PRIVATE,
        )

    private val archivedApp =
        AppEntry(
            packageName = "com.old.app",
            className = "com.old.app.Main",
            label = "Old App",
            profile = ProfileType.PERSONAL,
            isArchived = true,
        )

    // --- AppListItem ---

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun AppListItem_AllProfiles_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            Column(modifier = Modifier.padding(8.dp)) {
                AppListItem(app = personalApp, modifier = Modifier.fillMaxWidth())
                AppListItem(app = workApp, modifier = Modifier.fillMaxWidth())
                AppListItem(app = privateApp, modifier = Modifier.fillMaxWidth())
                AppListItem(app = archivedApp, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun AppListItem_AllProfiles_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            Column(modifier = Modifier.padding(8.dp)) {
                AppListItem(app = personalApp, modifier = Modifier.fillMaxWidth())
                AppListItem(app = workApp, modifier = Modifier.fillMaxWidth())
                AppListItem(app = privateApp, modifier = Modifier.fillMaxWidth())
                AppListItem(app = archivedApp, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    // --- FavoriteAppItem ---

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun FavoriteAppItem_Variations_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            Column(modifier = Modifier.padding(8.dp)) {
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_1",
                            appEntry = personalApp,
                            position = 0,
                        ),
                )
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_2",
                            appEntry = personalApp,
                            position = 1,
                            isPlaying = true,
                        ),
                )
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_3",
                            appEntry = personalApp,
                            position = 2,
                            hasNotifications = true,
                            notificationCount = 5,
                        ),
                )
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_4",
                            appEntry = personalApp,
                            position = 3,
                            customLabel = "Custom Label",
                            hasNotifications = true,
                            notificationCount = 120,
                        ),
                )
            }
        }
    }

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun FavoriteAppItem_Variations_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            Column(modifier = Modifier.padding(8.dp)) {
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_1",
                            appEntry = personalApp,
                            position = 0,
                        ),
                )
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_2",
                            appEntry = personalApp,
                            position = 1,
                            isPlaying = true,
                        ),
                )
                FavoriteAppItem(
                    favorite =
                        FavoriteEntry(
                            id = "fav_3",
                            appEntry = personalApp,
                            position = 2,
                            hasNotifications = true,
                            notificationCount = 5,
                        ),
                )
            }
        }
    }

    // --- GlanceHeader ---

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun GlanceHeader_Full_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            GlanceHeader(
                data =
                    GlanceHeaderData(
                        currentTime = "10:30",
                        currentDate = "Tuesday, Feb 18",
                        nextEvent = CalendarEvent(title = "Team standup", time = "11:00 AM", color = 0xFF4285F4.toInt()),
                        weather = WeatherInfo(temperature = "72°F", condition = "Sunny"),
                        nextAlarm = AlarmInfo(time = "7:00 AM", label = "Wake up"),
                    ),
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun GlanceHeader_Full_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            GlanceHeader(
                data =
                    GlanceHeaderData(
                        currentTime = "10:30",
                        currentDate = "Tuesday, Feb 18",
                        nextEvent = CalendarEvent(title = "Team standup", time = "11:00 AM", color = 0xFF4285F4.toInt()),
                        weather = WeatherInfo(temperature = "72°F", condition = "Sunny"),
                        nextAlarm = AlarmInfo(time = "7:00 AM", label = "Wake up"),
                    ),
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun GlanceHeader_Minimal() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            GlanceHeader(
                data =
                    GlanceHeaderData(
                        currentTime = "10:30",
                        currentDate = "Tuesday, Feb 18",
                    ),
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360)
    @Composable
    fun GlanceHeader_WithWeather() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            GlanceHeader(
                data =
                    GlanceHeaderData(
                        currentTime = "14:45",
                        currentDate = "Wednesday, Feb 19",
                        weather = WeatherInfo(temperature = "28°F", condition = "Snowing"),
                    ),
            )
        }
    }
}
