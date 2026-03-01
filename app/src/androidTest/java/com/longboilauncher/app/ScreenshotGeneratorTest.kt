package com.longboilauncher.app

import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented test to generate screenshots of the app.
 * Run this test and then pull the screenshots from /sdcard/LongboiScreenshots/
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScreenshotGeneratorTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var screenshotDir: File

    @Before
    fun setup() {
        hiltRule.inject()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        screenshotDir = File(context.filesDir, "screenshots")
        println("DEBUG: Screenshot directory: ${screenshotDir.absolutePath}")
        if (!screenshotDir.exists()) {
            val created = screenshotDir.mkdirs()
            println("DEBUG: Directory created: $created")
        }
    }

    @Test
    fun generateScreenshots() {
        // 1. Home Screen
        takeScreenshot("01_home_screen")

        // 2. All Apps Screen
        // Using the hint text from strings.xml
        val swipeHint = composeTestRule.activity.getString(R.string.swipe_up_hint)
        composeTestRule.onNodeWithText(swipeHint).performTouchInput {
            swipeUp()
        }
        composeTestRule.waitForIdle()
        takeScreenshot("02_all_apps")

        // 3. Search Screen
        // Assuming there is a search placeholder
        val searchPlaceholder = composeTestRule.activity.getString(R.string.search_apps_placeholder)
        // Back to home first
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        
        // Find Search by clicking on the search bar area if it exists, 
        // or navigate via ViewModel event if we had access.
        // For now, let's just try to find the search placeholder if it's visible on Home or All Apps
        // In this app, Search is an overlay.
    }

    private fun takeScreenshot(name: String) {
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val file = File(screenshotDir, "$name.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
