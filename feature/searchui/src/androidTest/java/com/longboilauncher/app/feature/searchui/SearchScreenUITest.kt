package com.longboilauncher.app.feature.searchui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.feature.home.SearchScreen
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.home.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<SearchViewModel>(relaxed = true)

    private val testResults = listOf(
        AppEntry(
            packageName = "com.test.youtube",
            className = "MainActivity",
            label = "YouTube",
            userIdentifier = 0,
            profile = ProfileType.PERSONAL
        )
    )

    @Before
    fun setup() {
        every { viewModel.searchQuery } returns MutableStateFlow("")
        every { viewModel.searchResults } returns MutableStateFlow(emptyList())
    }

    @Test
    fun searchScreen_displaysHint_whenQueryEmpty() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    viewModel = viewModel,
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Type to search apps").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysResults_whenQueryMatches() {
        every { viewModel.searchQuery } returns MutableStateFlow("you")
        every { viewModel.searchResults } returns MutableStateFlow(testResults)

        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    viewModel = viewModel,
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("YouTube").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysNoResults_whenNoMatches() {
        every { viewModel.searchQuery } returns MutableStateFlow("xyz")
        every { viewModel.searchResults } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            LongboiLauncherTheme {
                SearchScreen(
                    viewModel = viewModel,
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No apps found").assertIsDisplayed()
    }
}
