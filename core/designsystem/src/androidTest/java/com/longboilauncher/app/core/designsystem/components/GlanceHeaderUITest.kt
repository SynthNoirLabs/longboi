package com.longboilauncher.app.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.designsystem.components.GlanceHeader
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import org.junit.Rule
import org.junit.Test

class GlanceHeaderUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun glanceHeader_displaysTimeAndDate() {
        val testData = GlanceHeaderData(
            currentTime = "14:30",
            currentDate = "Friday, January 17",
            nextEvent = null,
            weather = null,
            nextAlarm = null,
            nowPlaying = null
        )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                GlanceHeader(data = testData)
            }
        }

        composeTestRule.onNodeWithText("14:30").assertIsDisplayed()
        composeTestRule.onNodeWithText("Friday, January 17").assertIsDisplayed()
    }

    @Test
    fun glanceHeader_displaysNextEvent() {
        val testData = GlanceHeaderData(
            currentTime = "14:30",
            currentDate = "Friday, January 17",
            nextEvent = GlanceHeaderData.CalendarEvent("Meeting with Team", "15:00"),
            weather = null,
            nextAlarm = null,
            nowPlaying = null
        )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                GlanceHeader(data = testData)
            }
        }

        composeTestRule.onNodeWithText("Meeting with Team").assertIsDisplayed()
        composeTestRule.onNodeWithText("15:00").assertIsDisplayed()
    }
}
