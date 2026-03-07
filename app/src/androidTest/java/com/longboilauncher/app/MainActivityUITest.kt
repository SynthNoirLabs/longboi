package com.longboilauncher.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.common.LauncherRoleHelper
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.allapps.AllAppsState
import com.longboilauncher.app.feature.allapps.AllAppsViewModel
import com.longboilauncher.app.feature.home.HomeState
import com.longboilauncher.app.feature.home.HomeViewModel
import com.longboilauncher.app.feature.home.LauncherSurface
import com.longboilauncher.app.feature.onboarding.OnboardingEffect
import com.longboilauncher.app.feature.onboarding.OnboardingState
import com.longboilauncher.app.feature.onboarding.OnboardingViewModel
import com.longboilauncher.app.feature.searchui.SearchState
import com.longboilauncher.app.feature.searchui.SearchViewModel
import com.longboilauncher.app.feature.settingsui.SettingsState
import com.longboilauncher.app.feature.settingsui.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test for the main app structure.
 * Tests LauncherApp composable directly to avoid Activity launch issues.
 */
class MainActivityUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val hapticFeedbackManager = mockk<HapticFeedbackManager>(relaxed = true)
    private val roleHelper = mockk<LauncherRoleHelper>(relaxed = true)
    private val homeViewModel = mockk<HomeViewModel>(relaxed = true)
    private val allAppsViewModel = mockk<AllAppsViewModel>(relaxed = true)
    private val searchViewModel = mockk<SearchViewModel>(relaxed = true)
    private val settingsViewModel = mockk<SettingsViewModel>(relaxed = true)
    private val onboardingViewModel = mockk<OnboardingViewModel>(relaxed = true)

    private val uiState = MutableStateFlow(HomeState(isLoading = false))
    private val shouldRequestRole = MutableStateFlow(false)

    @Before
    fun setup() {
        every { homeViewModel.uiState } returns uiState
        every { roleHelper.shouldRequestRole } returns shouldRequestRole
        every { allAppsViewModel.uiState } returns MutableStateFlow(AllAppsState())
        every { searchViewModel.uiState } returns MutableStateFlow(SearchState())
        every { settingsViewModel.uiState } returns MutableStateFlow(SettingsState())
        every { onboardingViewModel.uiState } returns MutableStateFlow(OnboardingState())
        every { onboardingViewModel.effects } returns MutableSharedFlow<OnboardingEffect>()
    }

    @Test
    fun app_startsOnHomeScreen() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                LauncherApp(
                    hapticFeedbackManager = hapticFeedbackManager,
                    homeViewModel = homeViewModel,
                    roleHelper = roleHelper,
                    allAppsViewModel = allAppsViewModel,
                    searchViewModel = searchViewModel,
                    settingsViewModel = settingsViewModel,
                    onboardingViewModel = onboardingViewModel,
                )
            }
        }

        // Verify we are on Home Screen
        composeTestRule.onNodeWithText("Search apps...", substring = true).assertIsDisplayed()
    }

    @Test
    fun app_showsOnboarding_whenNotCompleted() {
        uiState.value = HomeState(isLoading = false, currentSurface = LauncherSurface.ONBOARDING)
        every { onboardingViewModel.uiState } returns MutableStateFlow(
            OnboardingState(currentPage = 0, isLastPage = false)
        )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                LauncherApp(
                    hapticFeedbackManager = hapticFeedbackManager,
                    homeViewModel = homeViewModel,
                    roleHelper = roleHelper,
                    allAppsViewModel = allAppsViewModel,
                    searchViewModel = searchViewModel,
                    settingsViewModel = settingsViewModel,
                    onboardingViewModel = onboardingViewModel,
                )
            }
        }

        // Verify Onboarding screen content (Welcome title)
        composeTestRule.onNodeWithText("Welcome", substring = true, ignoreCase = true).assertIsDisplayed()
    }
}
