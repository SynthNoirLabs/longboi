package com.longboilauncher.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class LauncherInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.longboilauncher.app", appContext.packageName)
    }

    @Test
    fun launcherAppsServiceAvailable() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val launcherApps = appContext.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE)
        assertNotNull(launcherApps)
    }
}
