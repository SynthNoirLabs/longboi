package com.longboilauncher.feature.allapps

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.settings.NoOpHapticFeedbackManager
import com.longboilauncher.app.feature.allapps.AllAppsScreen
import com.longboilauncher.app.feature.allapps.AllAppsState

class AllAppsScreenScreenshotTest {
    private val testApps =
        listOf(
            AppEntry(
                packageName = "com.android.chrome",
                className = "com.google.android.apps.chrome.Main",
                label = "Chrome",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.android.contacts",
                className = "com.android.contacts.activities.PeopleActivity",
                label = "Contacts",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.discord",
                className = "com.discord.MainActivity",
                label = "Discord",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.work.email",
                className = "com.work.email.Main",
                label = "Email",
                profile = ProfileType.WORK,
            ),
            AppEntry(
                packageName = "com.android.settings",
                className = "com.android.settings.Settings",
                label = "Settings",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.slack.android",
                className = "com.slack.android.Main",
                label = "Slack",
                profile = ProfileType.WORK,
            ),
        )

    private val testSections =
        linkedMapOf(
            "C" to testApps.filter { it.label.startsWith("C") },
            "D" to testApps.filter { it.label.startsWith("D") },
            "E" to testApps.filter { it.label.startsWith("E") },
            "S" to testApps.filter { it.label.startsWith("S") },
        )

    private val testIndices =
        mapOf(
            "C" to 0,
            "D" to 3,
            "E" to 5,
            "S" to 7,
        )

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun AllAppsScreen_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            AllAppsScreen(
                uiState =
                    AllAppsState(
                        filteredApps = testApps,
                        appSections = testSections,
                        sectionIndices = testIndices,
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
                hapticFeedbackManager = NoOpHapticFeedbackManager(),
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun AllAppsScreen_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            AllAppsScreen(
                uiState =
                    AllAppsState(
                        filteredApps = testApps,
                        appSections = testSections,
                        sectionIndices = testIndices,
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
                hapticFeedbackManager = NoOpHapticFeedbackManager(),
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun AllAppsScreen_Empty() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            AllAppsScreen(
                uiState =
                    AllAppsState(
                        filteredApps = emptyList(),
                        appSections = emptyMap(),
                        sectionIndices = emptyMap(),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
                hapticFeedbackManager = NoOpHapticFeedbackManager(),
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun AllAppsScreen_WorkProfile() {
        val workApps = testApps.filter { it.profile == ProfileType.WORK }
        val workSections = linkedMapOf("E" to workApps.filter { it.label.startsWith("E") }, "S" to workApps.filter { it.label.startsWith("S") })
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            AllAppsScreen(
                uiState =
                    AllAppsState(
                        filteredApps = workApps,
                        appSections = workSections,
                        sectionIndices = mapOf("E" to 0, "S" to 2),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
                hapticFeedbackManager = NoOpHapticFeedbackManager(),
            )
        }
    }
}
