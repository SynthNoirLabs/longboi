package com.longboilauncher.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
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
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented test to generate screenshots of the app.
 * Tests LauncherApp composable directly to avoid Activity lifecycle issues.
 */
class ScreenshotGeneratorTest {
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

    private lateinit var screenshotDir: File

    @Before
    fun setup() {
        every { homeViewModel.uiState } returns uiState
        every { roleHelper.shouldRequestRole } returns shouldRequestRole
        every { allAppsViewModel.uiState } returns MutableStateFlow(AllAppsState())
        every { searchViewModel.uiState } returns MutableStateFlow(SearchState())
        every { settingsViewModel.uiState } returns MutableStateFlow(SettingsState())
        every { onboardingViewModel.uiState } returns MutableStateFlow(OnboardingState())
        every { onboardingViewModel.effects } returns MutableSharedFlow<OnboardingEffect>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        screenshotDir = File(context.filesDir, "screenshots")
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs()
        }
    }

    @Test
    fun generateScreenshots() {
        // 1. Home Screen
        uiState.value = HomeState(isLoading = false, currentSurface = LauncherSurface.HOME)
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
        composeTestRule.waitForIdle()
        takeScreenshot("01_home_screen")

        // 2. All Apps Screen
        uiState.value = HomeState(isLoading = false, currentSurface = LauncherSurface.ALL_APPS)
        composeTestRule.waitForIdle()
        takeScreenshot("02_all_apps")

        // 3. Settings Screen
        uiState.value = HomeState(isLoading = false, currentSurface = LauncherSurface.SETTINGS)
        composeTestRule.waitForIdle()
        takeScreenshot("03_settings")

        // 4. Onboarding
        uiState.value = HomeState(isLoading = false, currentSurface = LauncherSurface.ONBOARDING)
        composeTestRule.waitForIdle()
        takeScreenshot("04_onboarding")
    }

    private fun takeScreenshot(name: String) {
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val file = File(screenshotDir, "$name.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
