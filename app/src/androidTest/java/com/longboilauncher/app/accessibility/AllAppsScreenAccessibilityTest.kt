package com.longboilauncher.app.accessibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.settings.NoOpHapticFeedbackManager
import com.longboilauncher.app.feature.allapps.AllAppsScreen
import com.longboilauncher.app.feature.allapps.AllAppsState
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for the All Apps screen.
 * Verifies section headers, app items, and scrubber accessibility.
 */
class AllAppsScreenAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testApps =
        listOf(
            AppEntry(
                packageName = "com.test.app1",
                className = "MainActivity",
                label = "Apple",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.test.app2",
                className = "MainActivity",
                label = "Banana",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.test.app3",
                className = "MainActivity",
                label = "Calendar",
                profile = ProfileType.WORK,
            ),
        )

    private val testSections =
        linkedMapOf(
            "A" to listOf(testApps[0]),
            "B" to listOf(testApps[1]),
            "C" to listOf(testApps[2]),
        )

    private val testIndices = mapOf("A" to 0, "B" to 2, "C" to 4)

    @Test
    fun allAppsScreen_sectionHeaders_areReadable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    uiState =
                        AllAppsState(
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

        // Section letters should be readable
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertIsDisplayed()
    }

    @Test
    fun allAppsScreen_appItems_areClickable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    uiState =
                        AllAppsState(
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

        // App items should be clickable
        composeTestRule.onNode(hasText("Apple") and hasClickAction()).assertIsDisplayed()
        composeTestRule.onNode(hasText("Banana") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun allAppsScreen_alphabetScrubber_isPresent() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    uiState =
                        AllAppsState(
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

        // Alphabet scrubber should exist for navigation
        composeTestRule.onNodeWithTag("alphabet_scrubber").assertIsDisplayed()
    }

    @Test
    fun allAppsScreen_workProfileBadge_showsForWorkApps() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    uiState =
                        AllAppsState(
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

        // Work profile badge "W" should be visible for work apps
        composeTestRule.onNodeWithText("W").assertIsDisplayed()
    }
}
