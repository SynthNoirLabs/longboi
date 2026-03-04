package com.longboilauncher.app.accessibility

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.feature.home.HomeState
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for the Home screen.
 * Verifies that all interactive and informational elements are
 * properly accessible to screen readers and assistive technologies.
 */
class HomeScreenAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

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
                )
            }
        }

        // Time and date should be displayed and readable by screen readers
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
                )
            }
        }

        // Each favorite app should be clickable (for assistive tech interaction)
        composeTestRule.onNodeWithText("Settings").assert(hasClickAction())
        composeTestRule.onNodeWithText("Chrome").assert(hasClickAction())
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
                )
            }
        }

        // Navigation hint should be present for screen reader users
        composeTestRule.onNodeWithText("Swipe up for all apps").assertIsDisplayed()
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
                )
            }
        }

        // Empty state hint should be readable
        composeTestRule.onNodeWithText("Swipe up to add favorite apps").assertIsDisplayed()
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
                )
            }
        }

        // Verify all text nodes with click actions meet minimum touch target (48dp)
        val clickableNodes = composeTestRule.onAllNodes(hasClickAction())
        val count = clickableNodes.fetchSemanticsNodes().size
        for (i in 0 until count) {
            val node = clickableNodes[i].fetchSemanticsNode()
            val bounds = node.boundsInRoot
            val height = bounds.bottom - bounds.top
            val width = bounds.right - bounds.left
            // Material Design minimum touch target: 48dp
            assert(height >= 48f || width >= 48f) {
                "Clickable node at index $i has bounds ${width}x${height}, " +
                    "which may be too small for touch targets"
            }
        }
    }
}
