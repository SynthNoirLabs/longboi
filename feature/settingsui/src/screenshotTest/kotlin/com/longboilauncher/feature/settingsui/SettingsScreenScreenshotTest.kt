package com.longboilauncher.feature.settingsui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.settingsui.SettingsScreen
import com.longboilauncher.app.feature.settingsui.SettingsState

class SettingsScreenScreenshotTest {
    @Preview(showBackground = true, widthDp = 360, heightDp = 800)
    @Composable
    fun SettingsScreen_Default_Light() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            SettingsScreen(
                uiState = SettingsState(),
                onEvent = {},
                onNavigateBack = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 800)
    @Composable
    fun SettingsScreen_Default_Dark() {
        LongboiLauncherTheme(darkTheme = true, dynamicColor = false) {
            SettingsScreen(
                uiState = SettingsState(),
                onEvent = {},
                onNavigateBack = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 800)
    @Composable
    fun SettingsScreen_HapticsDisabled() {
        LongboiLauncherTheme(darkTheme = false, dynamicColor = false) {
            SettingsScreen(
                uiState =
                    SettingsState(
                        hapticsEnabled = false,
                        showNotifications = false,
                        theme = "dark",
                        density = "compact",
                    ),
                onEvent = {},
                onNavigateBack = {},
            )
        }
    }
}
