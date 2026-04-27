package com.longboilauncher.app.core.icons

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.model.AppEntry
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconPackManagerTest {
    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var manager: IconPackManager

    private val sampleApp =
        AppEntry(
            packageName = "com.example.app",
            className = "com.example.app.MainActivity",
            label = "Example",
            userSerialNumber = 0L,
        )

    @Before
    fun setup() {
        // Use a real Robolectric context so createPackageContext / resources don't crash, but
        // wrap a mocked PackageManager so we can control queryIntentActivities cleanly.
        val realContext: Context = ApplicationProvider.getApplicationContext()
        packageManager = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { context.packageManager } returns packageManager
        every { context.resources } returns realContext.resources
        manager = IconPackManager(context)
    }

    @Test
    fun `getIconDrawable returns null when icon pack name is blank`() {
        val drawable = manager.getIconDrawable(iconPackPackageName = "", appEntry = sampleApp)

        assertThat(drawable).isNull()
    }

    @Test
    fun `getIconDrawable returns null for built-in internal sets`() {
        assertThat(manager.getIconDrawable("internal://glass", sampleApp)).isNull()
        assertThat(manager.getIconDrawable("internal://minimalist", sampleApp)).isNull()
    }

    @Test
    fun `getIconDrawable returns null when package context cannot be created`() {
        every { context.createPackageContext(any(), any()) } throws
            PackageManager.NameNotFoundException()

        val drawable = manager.getIconDrawable("com.missing.iconpack", sampleApp)

        assertThat(drawable).isNull()
    }

    @Test
    fun `getInstalledIconPacks always includes built-in entries`() {
        every { packageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns emptyList()

        val packs = manager.getInstalledIconPacks()

        val labels = packs.map { it.label }
        assertThat(labels).contains("Glass (Built-in)")
        assertThat(labels).contains("Minimalist (Built-in)")
    }

    @Test
    fun `getInstalledIconPacks discovers packs across known intent filters`() {
        val novaResolve = resolveInfo("com.nova.theme")
        val adwResolve = resolveInfo("com.adw.theme")

        every {
            packageManager.queryIntentActivities(
                match<Intent> { it.action == "com.novalauncher.THEME" },
                any<Int>(),
            )
        } returns listOf(novaResolve)
        every {
            packageManager.queryIntentActivities(
                match<Intent> { it.action == "org.adw.launcher.THEMES" },
                any<Int>(),
            )
        } returns listOf(adwResolve)
        every {
            packageManager.queryIntentActivities(
                match<Intent> { it.action == "com.dlto.atom.launcher.THEME" },
                any<Int>(),
            )
        } returns emptyList()

        val novaInfo = ApplicationInfo()
        val adwInfo = ApplicationInfo()
        every { packageManager.getApplicationInfo("com.nova.theme", 0) } returns novaInfo
        every { packageManager.getApplicationInfo("com.adw.theme", 0) } returns adwInfo
        every { packageManager.getApplicationLabel(novaInfo) } returns "Nova Pack"
        every { packageManager.getApplicationLabel(adwInfo) } returns "ADW Pack"

        val packs = manager.getInstalledIconPacks()
        val packageNames = packs.map { it.packageName }

        assertThat(packageNames).contains("com.nova.theme")
        assertThat(packageNames).contains("com.adw.theme")
    }

    @Test
    fun `getInstalledIconPacks returns labels sorted alphabetically`() {
        val zResolve = resolveInfo("com.z.theme")
        val aResolve = resolveInfo("com.a.theme")
        every {
            packageManager.queryIntentActivities(any<Intent>(), any<Int>())
        } returns listOf(zResolve, aResolve)

        val zInfo = ApplicationInfo()
        val aInfo = ApplicationInfo()
        every { packageManager.getApplicationInfo("com.z.theme", 0) } returns zInfo
        every { packageManager.getApplicationInfo("com.a.theme", 0) } returns aInfo
        every { packageManager.getApplicationLabel(zInfo) } returns "Zinc Theme"
        every { packageManager.getApplicationLabel(aInfo) } returns "Apex Theme"

        val labels = manager.getInstalledIconPacks().map { it.label }

        assertThat(labels).isInOrder()
    }

    @Test
    fun `getInstalledIconPacks tolerates getApplicationInfo failures`() {
        val resolve = resolveInfo("com.missing.pack")
        every { packageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns
            listOf(resolve)
        every { packageManager.getApplicationInfo("com.missing.pack", 0) } throws
            PackageManager.NameNotFoundException()

        val packs = manager.getInstalledIconPacks()

        assertThat(packs.map { it.packageName }).doesNotContain("com.missing.pack")
        // Built-ins are still present
        assertThat(packs.map { it.label }).contains("Glass (Built-in)")
    }

    private fun resolveInfo(packageName: String): ResolveInfo =
        ResolveInfo().apply {
            activityInfo =
                ActivityInfo().apply {
                    this.packageName = packageName
                    name = "$packageName.ThemeActivity"
                    applicationInfo = ApplicationInfo().apply { this.packageName = packageName }
                }
        }
}
