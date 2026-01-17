package com.longboilauncher.app.feature.allapps

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.fetchSemanticsNodes
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.settings.NoOpHapticFeedbackManager
import org.junit.Rule
import org.junit.Test

class AllAppsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    @Test
    fun allAppsScreen_displaysSectionsAndApps() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    uiState = AllAppsState(
                        appSections = testSections,
                        sectionIndices = testIndices
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                    hapticFeedbackManager = NoOpHapticFeedbackManager()
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

    @Test
    fun allAppsScreen_scrubberDragScrollsToZSection() {
        val aApps = (0 until 120).map { index ->
            AppEntry(
                packageName = "com.test.a$index",
                className = "MainActivity",
                label = "A$index",
                profile = ProfileType.PERSONAL
            )
        }

        val zApp = AppEntry(
            packageName = "com.test.zebra",
            className = "MainActivity",
            label = "Zebra",
            profile = ProfileType.PERSONAL
        )

        val sections = linkedMapOf(
            "A" to aApps,
            "Z" to listOf(zApp)
        )

        val indices = mapOf(
            "A" to 0,
            "Z" to 1 + aApps.size
        )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                AllAppsScreen(
                    uiState = AllAppsState(
                        appSections = sections,
                        sectionIndices = indices
                    ),
                    onEvent = {},
                    onAppSelected = {},
                    onDismiss = {},
                    hapticFeedbackManager = NoOpHapticFeedbackManager()
                )
            }
        }

        composeTestRule.onNodeWithText("Zebra").assertDoesNotExist()

        composeTestRule.onNodeWithTag("alphabet_scrubber")
            .performTouchInput {
                swipe(
                    start = center,
                    end = bottomCenter,
                    durationMillis = 200
                )
            }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Zebra")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Zebra").assertIsDisplayed()
    }
}
