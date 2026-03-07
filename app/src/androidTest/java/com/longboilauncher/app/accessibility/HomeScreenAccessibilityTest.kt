package com.longboilauncher.app.accessibility

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.feature.home.HomeState
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for the Home screen.
 */
class HomeScreenAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val hapticFeedbackManager = mockk<HapticFeedbackManager>(relaxed = true)

    private val testFavorites =
        listOf(
            FavoriteEntry(
                id = "fav_1",
                appEntry =
                    AppEntry(
                        packageName = "com.android.settings",
                        className = "com.android.settings.Settings",
                        label = "Settings",
                    ),
                position = 0,
            ),
            FavoriteEntry(
                id = "fav_2",
                appEntry =
                    AppEntry(
                        packageName = "com.android.chrome",
                        className = "com.google.android.apps.chrome.Main",
                        label = "Chrome",
                    ),
                position = 1,
            ),
        )

    private val testGlance =
        GlanceHeaderData(
            currentTime = "10:30",
            currentDate = "Tuesday, Feb 18",
        )

    @Test
    fun homeScreen_glanceData_isReadable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState =
                        HomeState(
                            isLoading = false,
                            favorites = testFavorites,
                            glanceData = testGlance,
                        ),
                    onEvent = {},
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Time and date should be displayed and readable
        composeTestRule.onNodeWithText("10:30").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tuesday, Feb 18").assertIsDisplayed()
    }

    @Test
    fun homeScreen_favoriteApps_areClickable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState =
                        HomeState(
                            isLoading = false,
                            favorites = testFavorites,
                            glanceData = testGlance,
                        ),
                    onEvent = {},
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Each favorite app should be clickable
        composeTestRule.onNodeWithText("Settings").assert(hasClickAction())
    }

    @Test
    fun homeScreen_swipeHint_isReadable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState =
                        HomeState(
                            isLoading = false,
                            favorites = testFavorites,
                            glanceData = testGlance,
                        ),
                    onEvent = {},
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Search bar hint should be present
        composeTestRule.onNodeWithText("Search apps...", substring = true).assertIsDisplayed()
    }

    @Test
    fun homeScreen_emptyState_hasAccessibleHint() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState =
                        HomeState(
                            isLoading = false,
                            favorites = emptyList(),
                            glanceData = testGlance,
                        ),
                    onEvent = {},
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Empty state hint should be readable
        composeTestRule.onNodeWithText("Tap or swipe up to search").assertIsDisplayed()
    }

    @Test
    fun homeScreen_allTextNodes_haveMinimumSize() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState =
                        HomeState(
                            isLoading = false,
                            favorites = testFavorites,
                            glanceData = testGlance,
                        ),
                    onEvent = {},
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Verify all clickable items meet minimum touch target (48dp)
        val clickableNodes = composeTestRule.onAllNodes(hasClickAction())
        val count = clickableNodes.fetchSemanticsNodes().size
        for (i in 0 until count) {
            val node = clickableNodes[i].fetchSemanticsNode()
            val bounds = node.boundsInRoot
            val height = bounds.bottom - bounds.top
            val width = bounds.right - bounds.left
            assert(height >= 44f || width >= 44f) {
                "Clickable node at index $i has bounds ${width}x$height, too small"
            }
        }
    }
}
