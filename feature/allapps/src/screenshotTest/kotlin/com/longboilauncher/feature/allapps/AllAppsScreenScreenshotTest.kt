package com.longboilauncher.feature.allapps

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.settings.NoOpHapticFeedbackManager
import com.longboilauncher.app.feature.allapps.AllAppsScreen
import com.longboilauncher.app.feature.allapps.AllAppsState

class AllAppsScreenScreenshotTest {
    @Preview(showBackground = true)
    @Composable
    fun AllAppsScreenPreview() {
        val apps =
            listOf(
                AppEntry(
                    packageName = "com.android.chrome",
                    className = "com.google.android.apps.chrome.Main",
                    label = "Chrome",
                ),
                AppEntry(
                    packageName = "com.android.contacts",
                    className = "com.android.contacts.activities.PeopleActivity",
                    label = "Contacts",
                ),
                AppEntry(
                    packageName = "com.android.settings",
                    className = "com.android.settings.Settings",
                    label = "Settings",
                ),
            )

        val sections =
            sortedMapOf(
                "C" to apps.take(2),
                "S" to apps.takeLast(1),
            )

        LongboiLauncherTheme {
            AllAppsScreen(
                uiState =
                    AllAppsState(
                        filteredApps = apps,
                        appSections = sections,
                        sectionIndices =
                            mapOf(
                                "C" to 0,
                                "S" to 3,
                            ),
                    ),
                onEvent = {},
                onAppSelected = {},
                onDismiss = {},
                hapticFeedbackManager = NoOpHapticFeedbackManager(),
            )
        }
    }
}
