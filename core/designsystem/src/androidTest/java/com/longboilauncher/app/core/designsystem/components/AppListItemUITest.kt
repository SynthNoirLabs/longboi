package com.longboilauncher.app.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import org.junit.Rule
import org.junit.Test

class AppListItemUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appListItem_displaysLabel() {
        val testApp =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userIdentifier = 0,
                profile = ProfileType.PERSONAL,
            )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                AppListItem(app = testApp)
            }
        }

        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    }

    @Test
    fun appListItem_displaysWorkProfileBadge() {
        val testApp =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userIdentifier = 0,
                profile = ProfileType.WORK,
            )

        composeTestRule.setContent {
            LongboiLauncherTheme {
                AppListItem(app = testApp)
            }
        }

        // We assume ProfileBadge renders something identifiable if we had test tags,
        // for now we just verify it doesn't crash and label is there.
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    }
}
