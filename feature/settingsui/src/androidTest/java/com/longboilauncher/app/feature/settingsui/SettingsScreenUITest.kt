package com.longboilauncher.app.feature.settingsui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysItems() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Haptic feedback").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_toggleHaptics_callsViewModel() {
        val events = mutableListOf<SettingsEvent>()

        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(hapticsEnabled = true),
                    onEvent = { events.add(it) },
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Haptic feedback").performClick()

        assertTrue(events.contains(SettingsEvent.SetHapticsEnabled(false)))
    }
}
