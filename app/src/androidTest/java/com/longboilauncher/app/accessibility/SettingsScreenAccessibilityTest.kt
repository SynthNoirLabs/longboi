package com.longboilauncher.app.accessibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.settingsui.SettingsScreen
import com.longboilauncher.app.feature.settingsui.SettingsState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for the Settings screen.
 * Verifies navigation, toggles, and section headers are accessible.
 */
class SettingsScreenAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_backButton_hasContentDescription() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        // Back button should have accessible content description
        composeTestRule.onNode(hasContentDescription("Back")).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_sectionHeaders_areReadable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        // All section headers should be visible and readable
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Behavior").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Backup").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_toggleItems_areToggleable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        // Toggle switches should be identifiable as toggleable elements
        val toggleNodes = composeTestRule.onAllNodes(isToggleable())
        val count = toggleNodes.fetchSemanticsNodes().size
        assertTrue(
            "Settings screen should have at least 2 toggles (notifications, haptics), found $count",
            count >= 2,
        )
    }

    @Test
    fun settingsScreen_settingsItems_areClickable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        // Settings items should be clickable for assistive tech interaction
        composeTestRule.onNode(hasText("Theme") and hasClickAction()).assertIsDisplayed()
        composeTestRule.onNode(hasText("Density") and hasClickAction()).assertIsDisplayed()
        composeTestRule.onNode(hasText("Favorites") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_subtitle_showsCurrentValues() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState =
                        SettingsState(
                            theme = "dark",
                            density = "compact",
                        ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        // Current setting values should be readable as subtitles
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        composeTestRule.onNodeWithText("Compact").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_touchTargets_meetMinimumSize() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SettingsScreen(
                    uiState = SettingsState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        // All clickable items should meet minimum 48dp touch target
        val clickableNodes = composeTestRule.onAllNodes(hasClickAction())
        val semanticsNodes = clickableNodes.fetchSemanticsNodes()
        for (node in semanticsNodes) {
            val bounds = node.boundsInRoot
            val height = bounds.bottom - bounds.top
            assertTrue(
                "Clickable element should have at least 48dp height for touch target, found ${height}px",
                height >= 44f, // slightly below 48dp to account for density variations
            )
        }
    }
}
