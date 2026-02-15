package com.longboilauncher.app.feature.home

import android.content.pm.ShortcutInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ProfileType
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class HomeScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testApp =
        AppEntry(
            packageName = "com.test.app",
            className = "MainActivity",
            label = "Test App",
            userIdentifier = 0,
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

    private fun defaultState() =
        HomeState(
            isLoading = false,
            favorites = testFavorites,
            glanceData = testGlance,
        )

    @Test
    fun homeScreen_displaysGlanceAndFavorites() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState = defaultState(),
                    onEvent = {},
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
                    uiState = defaultState().copy(isLoading = true),
                    onEvent = {},
                )
            }
        }

        // CircularProgressIndicator doesn't have text, but we can check if content is not there
        composeTestRule.onNodeWithText("Test App").assertDoesNotExist()
    }

    @Test
    fun homeScreen_showsPopup_whenPopupAppSet() {
        val shortcut = mockk<ShortcutInfo>()
        every { shortcut.shortLabel } returns "Test Shortcut"
        every { shortcut.id } returns "shortcut_1"

        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState =
                        defaultState().copy(
                            popupApp = testApp,
                            popupShortcuts = listOf(shortcut),
                        ),
                    onEvent = {},
                )
            }
        }

        // Verify popup panel content
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
        composeTestRule.onNodeWithText("com.test.app").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shortcuts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Shortcut").assertIsDisplayed()
        composeTestRule.onNodeWithText("App Info").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uninstall").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hide").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsNotificationBadge_whenFavoriteHasNotifications() {
        val favWithNotifs =
            listOf(
                FavoriteEntry(
                    id = "fav_1",
                    appEntry = testApp,
                    position = 0,
                    notificationCount = 5,
                    hasNotifications = true,
                ),
            )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(
                    uiState = defaultState().copy(favorites = favWithNotifs),
                    onEvent = {},
                )
            }
        }

        // Verify notification count is displayed
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    }
}
