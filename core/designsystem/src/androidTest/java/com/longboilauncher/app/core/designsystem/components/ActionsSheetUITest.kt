package com.longboilauncher.app.core.designsystem

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.longboilauncher.app.core.designsystem.components.ActionsSheet
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class ActionsSheetUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testApp =
        AppEntry(
            packageName = "com.test.app",
            className = "MainActivity",
            label = "Test App",
            profile = ProfileType.PERSONAL,
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun actionsSheet_displaysOptions_whenFavorite() {
        val onAddToFavorites = mockk<() -> Unit>(relaxed = true)
        val onRemoveFromFavorites = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            LongboiLauncherTheme {
                ActionsSheet(
                    app = testApp,
                    isFavorite = true,
                    onDismiss = {},
                    onAddToFavorites = onAddToFavorites,
                    onRemoveFromFavorites = onRemoveFromFavorites,
                    onHideApp = {},
                    onAppInfo = {},
                    onUninstall = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Remove from favorites", substring = true, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("App info", substring = true, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Hide app", substring = true, ignoreCase = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("Remove from favorites", substring = true, ignoreCase = true).performClick()
        verify { onRemoveFromFavorites() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun actionsSheet_displaysOptions_whenNotFavorite() {
        val onAddToFavorites = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            LongboiLauncherTheme {
                ActionsSheet(
                    app = testApp,
                    isFavorite = false,
                    onDismiss = {},
                    onAddToFavorites = onAddToFavorites,
                    onRemoveFromFavorites = {},
                    onHideApp = {},
                    onAppInfo = {},
                    onUninstall = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Add to favorites", substring = true, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Add to favorites", substring = true, ignoreCase = true).performClick()
        verify { onAddToFavorites() }
    }
}
