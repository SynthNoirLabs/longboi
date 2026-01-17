package com.longboilauncher.app.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.feature.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class HomeScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<HomeViewModel>(relaxed = true)

    private val testApp = AppEntry(
        packageName = "com.test.app",
        className = "MainActivity",
        label = "Test App",
        userIdentifier = 0,
        profile = ProfileType.PERSONAL
    )

    private val testFavorites = listOf(
        FavoriteEntry(
            id = "fav_1",
            appEntry = testApp,
            position = 0
        )
    )

    private val testGlance = GlanceHeaderData(
        currentTime = "12:00",
        currentDate = "Monday, Jan 1",
        nextEvent = null,
        weather = null,
        nextAlarm = null,
        nowPlaying = null
    )

    @Before
    fun setup() {
        every { viewModel.isLoading } returns MutableStateFlow(false)
        every { viewModel.glanceData } returns MutableStateFlow(testGlance)
        every { viewModel.favorites } returns MutableStateFlow(testFavorites)
    }

    @Test
    fun homeScreen_displaysGlanceAndFavorites() {
        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(viewModel = viewModel)
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
        every { viewModel.isLoading } returns MutableStateFlow(true)

        composeTestRule.setContent {
            LongboiLauncherTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        // CircularProgressIndicator doesn't have text, but we can check if content is not there
        composeTestRule.onNodeWithText("Test App").assertDoesNotExist()
    }
}
