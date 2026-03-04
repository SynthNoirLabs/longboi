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

    // -------------------------------------------------------------------------
    // equals / hashCode — data-class full-field semantics (fixes StateFlow bug)
    // -------------------------------------------------------------------------

    @Test
    fun `label change makes two entries unequal so StateFlow emits`() {
        // This is the regression guard for the MutableStateFlow silent-drop bug:
        // _apps.value = newList must fire when only a label changed.
        val original =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "Example App",
                userIdentifier = 0,
            )
        val renamed = original.copy(label = "Renamed App")

        assertNotEquals(original, renamed)
    }

    @Test
    fun `isEnabled change makes two entries unequal so StateFlow emits`() {
        val enabled =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "App",
                userIdentifier = 0,
                isEnabled = true,
            )
        val disabled = enabled.copy(isEnabled = false)

        assertNotEquals(enabled, disabled)
    }

    @Test
    fun `isSuspended change makes two entries unequal so StateFlow emits`() {
        val active =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "App",
                userIdentifier = 0,
            )
        val suspended = active.copy(isSuspended = true)

        assertNotEquals(active, suspended)
    }

    @Test
    fun `isArchived change makes two entries unequal so StateFlow emits`() {
        val live =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "App",
                userIdentifier = 0,
            )
        val archived = live.copy(isArchived = true)

        assertNotEquals(live, archived)
    }

    @Test
    fun `identical entries are equal`() {
        val entry =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "App",
                userIdentifier = 0,
            )

        assertEquals(entry, entry.copy())
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

    // -------------------------------------------------------------------------
    // sameApp — identity comparison (package + class + user only)
    // -------------------------------------------------------------------------

    @Test
    fun `sameApp returns true when package, class, and user match despite label change`() {
        val original =
            AppEntry(
                packageName = "com.example.app",
                className = "com.example.app.MainActivity",
                label = "Example App",
                userIdentifier = 0,
            )
        val renamed = original.copy(label = "Renamed App")

        // Different by equals (triggers StateFlow emission), but sameApp identity holds
        assertNotEquals(original, renamed)
        assert(original.sameApp(renamed))
    }

    @Test
    fun `sameApp returns false for different packages`() {
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

        assert(!entry1.sameApp(entry2))
    }
}
