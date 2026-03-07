package com.longboilauncher.app.accessibility

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.feature.searchui.SearchResult
import com.longboilauncher.app.feature.searchui.SearchScreen
import com.longboilauncher.app.feature.searchui.SearchState
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for the Search screen.
 */
class SearchScreenAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchScreen_searchField_hasSearchIcon() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(searchQuery = "", searchResults = emptyList()),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                )
            }
        }

        // Search icon should have content description
        composeTestRule.onNode(hasContentDescription("Search")).assertIsDisplayed()
    }

    @Test
    fun searchScreen_clearButton_hasSearchIconWhenEmpty() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(searchQuery = "", searchResults = emptyList()),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                )
            }
        }
        // No clear button when empty - use onAllNodes to verify it doesn't exist
        composeTestRule.onAllNodes(hasContentDescription("Clear")).assertCountEquals(0)
    }

    @Test
    fun searchScreen_noResults_isReadable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState =
                        SearchState(
                            searchQuery = "xyznonexistent",
                            searchResults = emptyList(),
                        ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No results found").assertIsDisplayed()
    }

    @Test
    fun searchScreen_appResults_areClickable() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState =
                        SearchState(
                            searchQuery = "chrome",
                            searchResults =
                                listOf(
                                    SearchResult.AppResult(
                                        AppEntry(
                                            packageName = "com.android.chrome",
                                            className = "com.google.android.apps.chrome.Main",
                                            label = "Chrome",
                                        ),
                                    ),
                                ),
                        ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                )
            }
        }

        // App results should be interactive
        composeTestRule.onNode(hasText("Chrome") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun searchScreen_calculatorResult_displaysEquation() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState =
                        SearchState(
                            searchQuery = "2+2",
                            searchResults =
                                listOf(
                                    SearchResult.CalculatorResult("2+2", "4"),
                                ),
                        ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                )
            }
        }

        // Both expression and result should be readable
        composeTestRule.onAllNodesWithText("2+2").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("= 4").assertIsDisplayed()
    }
}
