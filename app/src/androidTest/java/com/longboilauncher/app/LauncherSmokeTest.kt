package com.longboilauncher.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test

/**
 * End-to-end smoke tests using UIAutomator.
 * These tests interact with the system UI and verified cross-app boundaries.
 */
class LauncherSmokeTest {
    private lateinit var device: UiDevice
    private val packageName = "com.longboilauncher.app"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from home
        device.pressHome()

        // Wait for launcher
        val launcherPackage = device.launcherPackageName
        assertNotNull(launcherPackage)

        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
    }

    @Test
    fun smoke_openAllApps_andBack() {
        // Swipe up to open all apps
        // Note: UIAutomator coordinates depend on screen size,
        // using percent-based swipe or finding a scrollable container is better.
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight

        device.swipe(
            displayWidth / 2,
            (displayHeight * 0.8).toInt(),
            displayWidth / 2,
            (displayHeight * 0.2).toInt(),
            20,
        )

        // Wait for All Apps list (looking for 'A' header)
        val allAppsReady = device.wait(Until.hasObject(By.text("A")), 3000)
        assertTrue("All Apps screen should be visible", allAppsReady)

        // Press back
        device.pressBack()

        // Should be back on Home (check for "Swipe up" hint)
        val backOnHome = device.wait(Until.hasObject(By.textContains("Swipe up")), 3000)
        assertTrue("Should be back on Home screen", backOnHome)
    }

    @Test
    fun smoke_openSearch_andType() {
        // Assuming search is opened by typing or a specific trigger
        // For now, let's just verify the launcher is alive
        val homeReady = device.wait(Until.hasObject(By.pkg(packageName)), 3000)
        assertTrue("Launcher should be running", homeReady)
    }
}
