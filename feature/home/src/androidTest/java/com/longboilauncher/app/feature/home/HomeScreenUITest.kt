package com.longboilauncher.app.feature.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ProfileType
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class HomeScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val hapticFeedbackManager = mockk<HapticFeedbackManager>(relaxed = true)

    private val testApp =
        AppEntry(
            packageName = "com.test.app",
            className = "MainActivity",
            label = "Test App",
            profile = ProfileType.PERSONAL,
        )

    private val testFavorites =
        listOf(
            FavoriteEntry(
                id = "fav_1",
                appEntry = testApp,
                position = 0,
            ),
        )

    private val testGlance =
        GlanceHeaderData(
            currentTime = "12:00",
            currentDate = "Monday, Jan 1",
            nextEvent = null,
            weather = null,
            nextAlarm = null,
            nowPlaying = null,
        )

    @Test
    fun homeScreen_displaysGlanceAndFavorites() {
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

        // Verify glance data
        composeTestRule.onNodeWithText("12:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monday, Jan 1").assertIsDisplayed()

        // Verify favorite app
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsLoading() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState = HomeState(isLoading = true),
                    onEvent = {},
                    hapticFeedbackManager = hapticFeedbackManager,
                )
            }
        }

        // Use onAllNodes to verify it doesn't exist
        composeTestRule.onAllNodesWithText("Test App").assertCountEquals(0)
    }
}
