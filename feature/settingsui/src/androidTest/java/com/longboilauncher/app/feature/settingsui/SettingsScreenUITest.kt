package com.longboilauncher.app.feature.settingsui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.longboilauncher.app.feature.home.SettingsScreen
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.home.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<SettingsViewModel>(relaxed = true)

    @Before
    fun setup() {
        every { viewModel.theme } returns MutableStateFlow("system")
        every { viewModel.hapticsEnabled } returns MutableStateFlow(true)
        every { viewModel.showNotifications } returns MutableStateFlow(true)
    }

    @Test
    fun settingsScreen_displaysItems() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Haptic feedback").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_toggleHaptics_callsViewModel() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Haptic feedback").performClick()
        verify { viewModel.setHapticsEnabled(false) }
    }
}
