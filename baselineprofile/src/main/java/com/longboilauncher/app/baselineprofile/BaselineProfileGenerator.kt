package com.longboilauncher.app.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates Baseline Profiles for Longboi Launcher.
 *
 * Baseline Profiles improve app startup time by pre-compiling critical code paths.
 * Run this test on a physical device or emulator to generate profiles.
 *
 * To generate profiles:
 * ./gradlew :baselineprofile:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = "com.longboilauncher.app",
            includeInStartupProfile = true,
        ) {
            // Cold start the app
            pressHome()
            startActivityAndWait()

            // Wait for home screen to load
            device.wait(Until.hasObject(By.res("home_screen")), 5000)

            // Scroll through favorites (if any)
            device.findObject(By.scrollable(true))?.let { scrollable ->
                scrollable.scroll(androidx.test.uiautomator.Direction.DOWN, 0.5f)
                scrollable.scroll(androidx.test.uiautomator.Direction.UP, 0.5f)
            }

            // Open app drawer
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                device.displayWidth / 2,
                device.displayHeight / 4,
                10,
            )
            device.wait(Until.hasObject(By.res("all_apps_screen")), 3000)

            // Scroll through app list
            device.findObject(By.scrollable(true))?.let { scrollable ->
                scrollable.scroll(androidx.test.uiautomator.Direction.DOWN, 1f)
                scrollable.scroll(androidx.test.uiautomator.Direction.UP, 1f)
            }

            // Use alphabet scrubber
            device.findObject(By.res("alphabet_scrubber"))?.let { scrubber ->
                scrubber.click()
            }

            // Go back to home
            pressHome()
        }
    }
}
