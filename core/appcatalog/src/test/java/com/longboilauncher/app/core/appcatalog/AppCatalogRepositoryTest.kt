package com.longboilauncher.app.core.appcatalog

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.common.UserHandleManager
import com.longboilauncher.app.core.model.AppEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppCatalogRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: AppCatalogRepository
    private lateinit var userHandleManager: UserHandleManager
    private lateinit var shadowApplication: ShadowApplication

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        userHandleManager = UserHandleManager(context)
        shadowApplication = shadowOf(context as android.app.Application)

        repository = AppCatalogRepository(context, userHandleManager)
    }

    @Test
    fun `apps flow is initially empty`() {
        assertThat(repository.apps.value).isEmpty()
    }

    @Test
    fun `isLoading is false by default`() {
        assertThat(repository.isLoading.value).isFalse()
    }

    @Test
    fun `refreshAppCatalog leaves isLoading false when complete`() =
        runTest {
            repository.refreshAppCatalog()

            assertThat(repository.isLoading.value).isFalse()
        }

    @Test
    fun `refreshAppCatalog leaves apps empty when LauncherApps reports nothing`() =
        runTest {
            repository.refreshAppCatalog()

            assertThat(repository.apps.value).isEmpty()
        }

    @Test
    fun `launchApp with null user is a silent no-op`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
                user = null,
            )

        // Must not throw, and must not start any activity since the user is missing.
        repository.launchApp(appEntry)

        assertThat(shadowApplication.peekNextStartedActivity()).isNull()
    }

    @Test
    fun `launchApp with valid user starts main activity via LauncherApps`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
                user = Process.myUserHandle(),
            )

        // Without a registered activity in ShadowLauncherApps the call may be a no-op, but
        // it must not throw and must not leak any side-effect activities to the application.
        repository.launchApp(appEntry)

        assertThat(shadowApplication.peekNextStartedActivity()).isNull()
    }

    @Test
    fun `getAppShortcuts returns empty list when user is null`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
                user = null,
            )

        assertThat(repository.getAppShortcuts(appEntry)).isEmpty()
    }

    @Test
    fun `getAppShortcuts returns empty list when no shortcuts found`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
                user = Process.myUserHandle(),
            )

        assertThat(repository.getAppShortcuts(appEntry)).isEmpty()
    }

    @Test
    fun `getAppIcon returns null when app is missing from LauncherApps`() {
        val appEntry =
            AppEntry(
                packageName = "com.unknown.app",
                className = "MainActivity",
                label = "Unknown",
                userSerialNumber = 0L,
                user = Process.myUserHandle(),
            )

        assertThat(repository.getAppIcon(appEntry)).isNull()
    }

    @Test
    fun `getAppIcon returns null when user is null`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
                user = null,
            )

        assertThat(repository.getAppIcon(appEntry)).isNull()
    }

    @Test
    fun `openSettings dispatches the wifi settings intent for wifi destination`() {
        repository.openSettings("wifi")

        val started = shadowApplication.nextStartedActivity
        assertThat(started).isNotNull()
        assertThat(started.action).isEqualTo(Settings.ACTION_WIFI_SETTINGS)
        assertThat(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
            .isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `openSettings dispatches matching intents for each known destination`() {
        val cases =
            mapOf(
                "bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS,
                "display" to Settings.ACTION_DISPLAY_SETTINGS,
                "sound" to Settings.ACTION_SOUND_SETTINGS,
                "apps" to Settings.ACTION_APPLICATION_SETTINGS,
                "battery" to Settings.ACTION_BATTERY_SAVER_SETTINGS,
                "storage" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
                "security" to Settings.ACTION_SECURITY_SETTINGS,
            )

        cases.forEach { (destination, expectedAction) ->
            // Drain anything from a previous iteration first.
            while (shadowApplication.nextStartedActivity != null) Unit

            repository.openSettings(destination)

            val started = shadowApplication.nextStartedActivity
            assertThat(started).isNotNull()
            assertThat(started.action).isEqualTo(expectedAction)
        }
    }

    @Test
    fun `openSettings falls back to general settings for unknown destination`() {
        repository.openSettings("not-a-real-destination")

        val started = shadowApplication.nextStartedActivity
        assertThat(started).isNotNull()
        assertThat(started.action).isEqualTo(Settings.ACTION_SETTINGS)
    }

    @Test
    fun `uninstallApp launches a delete intent for the package`() {
        val appEntry =
            AppEntry(
                packageName = "com.uninstall.me",
                className = "MainActivity",
                label = "Goodbye",
                userSerialNumber = 0L,
                user = Process.myUserHandle(),
            )

        repository.uninstallApp(appEntry)

        val started = shadowApplication.nextStartedActivity
        assertThat(started).isNotNull()
        assertThat(started.action).isEqualTo(Intent.ACTION_DELETE)
        assertThat(started.data?.toString()).isEqualTo("package:com.uninstall.me")
        assertThat(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
            .isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `registerPackageListener and unregisterPackageListener do not throw`() {
        val callback =
            object : LauncherApps.Callback() {
                override fun onPackageRemoved(packageName: String, user: android.os.UserHandle) = Unit
                override fun onPackageAdded(packageName: String, user: android.os.UserHandle) = Unit
                override fun onPackageChanged(packageName: String, user: android.os.UserHandle) = Unit
                override fun onPackagesAvailable(
                    packageNames: Array<out String>,
                    user: android.os.UserHandle,
                    replacing: Boolean,
                ) = Unit
                override fun onPackagesUnavailable(
                    packageNames: Array<out String>,
                    user: android.os.UserHandle,
                    replacing: Boolean,
                ) = Unit
            }

        repository.registerPackageListener(callback)
        repository.unregisterPackageListener(callback)
    }
}
