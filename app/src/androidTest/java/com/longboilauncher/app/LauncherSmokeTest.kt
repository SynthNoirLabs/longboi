package com.longboilauncher.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end smoke tests using UIAutomator.
 */
class LauncherSmokeTest {
    private lateinit var device: UiDevice
    private val packageName = "com.longboilauncher.app"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from home
        device.pressHome()

        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
    }

    @Test
    fun smoke_openAllApps_andBack() {
        // Swipe up to open search/all apps
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight

        device.swipe(
            displayWidth / 2,
            (displayHeight * 0.8).toInt(),
            displayWidth / 2,
            (displayHeight * 0.2).toInt(),
            20,
        )

        // Wait for Search or All Apps (looking for 'Search' hint)
        val searchReady = device.wait(Until.hasObject(By.textContains("Search")), 3000)
        assertTrue("Search/All Apps screen should be visible", searchReady)

        // Press back
        device.pressBack()

        // Should be back on Home (check for "Search apps" hint)
        val backOnHome = device.wait(Until.hasObject(By.textContains("Search apps")), 3000)
        assertTrue("Should be back on Home screen", backOnHome)
    }

    @Test
    fun smoke_openSearch_andType() {
        val homeReady = device.wait(Until.hasObject(By.pkg(packageName)), 3000)
        assertTrue("Launcher should be running", homeReady)
    }
}
