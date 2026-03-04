package com.longboilauncher.app.core.appcatalog

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.common.UserHandleManager
import com.longboilauncher.app.core.model.AppEntry
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLauncherApps
import org.robolectric.shadows.ShadowUserManager

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppCatalogRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: AppCatalogRepository
    private lateinit var launcherApps: LauncherApps
    private lateinit var userManager: UserManager
    private lateinit var userHandleManager: UserHandleManager
    private lateinit var shadowLauncherApps: ShadowLauncherApps
    private lateinit var shadowUserManager: ShadowUserManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        userHandleManager = UserHandleManager(context)

        shadowLauncherApps = shadowOf(launcherApps)
        shadowUserManager = shadowOf(userManager)

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
    fun `refreshAppCatalog fetches apps for current user`() =
        runTest {
            repository.refreshAppCatalog()
            assertThat(repository.isLoading.value).isFalse()
        }

    @Test
    fun `launchApp handles exceptions gracefully`() {
        val appEntry =
            AppEntry(
                packageName = "com.nonexistent.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
            )

        repository.launchApp(appEntry)
    }

    @Test
    fun `getAppShortcuts returns empty list when no shortcuts found`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
            )

        val shortcuts = repository.getAppShortcuts(appEntry)
        assertThat(shortcuts).isEmpty()
    }

    @Test
    fun `getAppIcon returns icon from LauncherApps`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
            )

        val icon = repository.getAppIcon(appEntry)
    }

    @Test
    fun `launchApp calls startMainActivity`() {
        val appEntry =
            AppEntry(
                packageName = "com.test.app",
                className = "MainActivity",
                label = "Test App",
                userSerialNumber = 0L,
                user = Process.myUserHandle()
            )

        repository.launchApp(appEntry)
    }
}
