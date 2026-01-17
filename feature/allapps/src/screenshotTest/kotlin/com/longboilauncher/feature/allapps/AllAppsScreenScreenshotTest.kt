package com.longboilauncher.feature.allapps

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.allapps.AllAppsScreen
import com.longboilauncher.app.feature.allapps.AllAppsState
import com.longboilauncher.app.core.model.AppEntry

class AllAppsScreenScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun AllAppsScreenPreview() {
        LongboiLauncherTheme {
            AllAppsScreen(
                state = AllAppsState(
                    apps = listOf(
                        AppEntry("com.android.settings", "Settings", "com.android.settings.Settings"),
                        AppEntry("com.android.chrome", "Chrome", "com.google.android.apps.chrome.Main"),
                        AppEntry("com.android.contacts", "Contacts", "com.android.contacts.activities.PeopleActivity")
                    )
                ),
                onEvent = {}
            )
        }
    }
}
