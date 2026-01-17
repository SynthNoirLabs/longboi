package com.longboilauncher.feature.settingsui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.settingsui.SettingsScreen
import com.longboilauncher.app.feature.settingsui.SettingsState
import com.longboilauncher.app.feature.settingsui.SettingsEvent

class SettingsScreenScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun SettingsScreenPreview() {
        LongboiLauncherTheme {
            SettingsScreen(
                uiState = SettingsState(),
                onEvent = { _: SettingsEvent -> },
                onNavigateBack = {}
            )
        }
    }
}
