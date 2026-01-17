package com.longboilauncher.app.feature.searchui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.searchui.SearchResult
import org.junit.Rule
import org.junit.Test

class SearchScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testResults = listOf(
        SearchResult.CalculatorResult("123+456", "579"),
        SearchResult.SettingsShortcutResult("Wi-Fi", "wifi", "wifi"),
        SearchResult.AppResult(
            AppEntry(
                packageName = "com.test.youtube",
                className = "MainActivity",
                label = "YouTube",
                userIdentifier = 0,
                profile = ProfileType.PERSONAL
            )
        )
    )

    @Test
    fun searchScreen_displaysHint_whenQueryEmpty() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(
                        searchQuery = "",
                        searchResults = emptyList()
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Type to search apps, settings, or calculate").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysResults_whenQueryMatches() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(
                        searchQuery = "you",
                        searchResults = testResults
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("YouTube").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysCalculatorResult() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(
                        searchQuery = "123+456",
                        searchResults = listOf(SearchResult.CalculatorResult("123+456", "579"))
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("= 579").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysSettingsShortcut() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(
                        searchQuery = "wifi",
                        searchResults = listOf(SearchResult.SettingsShortcutResult("Wi-Fi", "wifi", "wifi"))
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Wi-Fi").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysNoResults_whenNoMatches() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    uiState = SearchState(
                        searchQuery = "xyz",
                        searchResults = emptyList()
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No results").assertIsDisplayed()
    }
}
