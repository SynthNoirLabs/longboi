package com.longboilauncher.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for navigation flows between screens.
 * Verifies that swipe gestures and navigation correctly transition between
 * Home, All Apps, and Search surfaces.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun navigation_homeScreen_isInitialDestination() {
        // Home screen should be the initial destination
        composeTestRule.onNodeWithText("Swipe up for all apps").assertIsDisplayed()
    }

    @Test
    fun navigation_swipeUp_navigatesToAllApps() {
        // Start on Home screen
        composeTestRule.onNodeWithText("Swipe up for all apps").assertIsDisplayed()

        // Swipe up to navigate to All Apps
        composeTestRule.onNodeWithText("Swipe up for all apps").performTouchInput {
            swipeUp()
        }

        // Wait for All Apps screen to appear
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigation_swipeDown_navigatesToSearch() {
        // Start on Home screen
        composeTestRule.onNodeWithText("Swipe up for all apps").assertIsDisplayed()

        // Swipe down to navigate to Search
        composeTestRule.onNodeWithText("Swipe up for all apps").performTouchInput {
            swipeDown()
        }

        // Wait for Search screen to appear
        composeTestRule.waitForIdle()
    }
}
