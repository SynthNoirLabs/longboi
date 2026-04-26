package com.longboilauncher.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.File
import java.io.FileOutputStream
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
        // Wait for app to be ready and idle
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // Give animations time to settle

        // 1. Home Screen
        takeScreenshot("01_home_screen")

        // 2. All Apps Screen (Niagara style: just scroll down a bit)
        composeTestRule.onRoot().performTouchInput {
            swipeUp(startY = 1500f, endY = 500f, durationMillis = 500)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        takeScreenshot("02_all_apps")

        // 3. Settings Screen
        // We can't easily click through to settings in a generic way if the icon is dynamic,
        // but we can navigate via activity if we wanted.
        // For now, let's focus on the main UI parts.
    }

    private fun takeScreenshot(name: String) {
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val file = File(screenshotDir, "$name.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
