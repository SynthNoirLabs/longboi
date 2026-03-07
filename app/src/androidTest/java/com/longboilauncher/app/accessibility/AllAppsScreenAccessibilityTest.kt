package com.longboilauncher.app.accessibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.feature.allapps.AllAppsScreen
import com.longboilauncher.app.feature.allapps.AllAppsState
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for the All Apps screen.
 */
class AllAppsScreenAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val hapticFeedbackManager = mockk<HapticFeedbackManager>(relaxed = true)

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
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Section letters should be readable - use onAllNodes since scrubber has them too
        composeTestRule.onAllNodesWithText("A").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("B").onFirst().assertIsDisplayed()
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
                    hapticFeedbackManager = hapticFeedbackManager,
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
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Alphabet scrubber should exist for navigation
        composeTestRule.onNodeWithTag("alphabet_scrubber").assertIsDisplayed()
    }
}
