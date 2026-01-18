package com.longboilauncher.app.core.model

import android.os.Build
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class AppEntryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `AppEntry serialization roundtrip works`() {
        val entry =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "Example App",
                userIdentifier = 0,
                profile = ProfileType.PERSONAL,
                lastUpdateTime = 1234567890L,
                firstInstallTime = 1234567800L,
                isSystemApp = false,
                isEnabled = true,
            )

        val jsonString = json.encodeToString(entry)
        val decoded = json.decodeFromString<AppEntry>(jsonString)

        assertEquals(entry.packageName, decoded.packageName)
        assertEquals(entry.className, decoded.className)
        assertEquals(entry.label, decoded.label)
        assertEquals(entry.profile, decoded.profile)
    }

    @Test
    fun `AppEntry equality based on package, class, and user`() {
        val entry1 =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "Example App",
                userIdentifier = 0,
            )

        val entry2 =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "Different Label",
                userIdentifier = 0,
            )

        assertEquals(entry1, entry2)
    }

    @Test
    fun `different packages are not equal`() {
        val entry1 =
            AppEntry(
                packageName = "com.example.app1",
                className = "com.example.app.MainActivity",
                label = "App",
                userIdentifier = 0,
            )

        val entry2 =
            AppEntry(
                packageName = "com.example.app2",
                className = "com.example.app.MainActivity",
                label = "App",
                userIdentifier = 0,
            )

        assertNotEquals(entry1, entry2)
    }
}
