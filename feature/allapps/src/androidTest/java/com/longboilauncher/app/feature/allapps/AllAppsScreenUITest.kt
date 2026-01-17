package com.longboilauncher.app.feature.allapps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.feature.home.AllAppsScreen
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.home.AllAppsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AllAppsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<AllAppsViewModel>(relaxed = true)

    private val testApps = listOf(
        AppEntry(packageName = "com.test.app1", className = "MainActivity", label = "Apple", profile = ProfileType.PERSONAL),
        AppEntry(packageName = "com.test.app2", className = "MainActivity", label = "Banana", profile = ProfileType.PERSONAL)
    )

    private val testSections = mapOf(
        "A" to listOf(testApps[0]),
        "B" to listOf(testApps[1])
    )

    private val testIndices = mapOf(
        "A" to 0,
        "B" to 2
    )

    @Before
    fun setup() {
        every { viewModel.appSections } returns MutableStateFlow(testSections)
        every { viewModel.sectionIndices } returns MutableStateFlow(testIndices)
    }

    @Test
    fun allAppsScreen_displaysSectionsAndApps() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    viewModel = viewModel,
                    onAppSelected = {},
                    onDismiss = {}
                )
            }
        }

        // Verify section headers
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertIsDisplayed()

        // Verify apps
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banana").assertIsDisplayed()
    }
}
