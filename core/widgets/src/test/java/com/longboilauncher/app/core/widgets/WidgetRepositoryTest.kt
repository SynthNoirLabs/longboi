package com.longboilauncher.app.core.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WidgetRepositoryTest {
    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var widgetHost: WidgetHost
    private lateinit var repository: WidgetRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        every { context.packageManager } returns packageManager

        widgetHost = mockk(relaxed = true)
        repository = WidgetRepository(context, widgetHost)
    }

    private fun stubProvider(
        packageName: String,
        className: String = "WidgetProvider",
        widgetLabel: String = "Widget",
        appLabel: String = packageName,
        minWidth: Int = 100,
        minHeight: Int = 100,
        resizeMode: Int = AppWidgetProviderInfo.RESIZE_NONE,
    ): AppWidgetProviderInfo {
        // spyk on a real instance so direct field access (provider, minWidth, etc.) returns the
        // values we set; only the load*() methods need to be stubbed.
        val provider = spyk(AppWidgetProviderInfo())
        provider.provider = ComponentName(packageName, className)
        provider.minWidth = minWidth
        provider.minHeight = minHeight
        provider.resizeMode = resizeMode

        every { provider.loadLabel(any()) } returns widgetLabel
        every { provider.loadPreviewImage(any(), any()) } returns null
        every { provider.loadIcon(any(), any()) } returns null

        val appInfo = ApplicationInfo().apply { this.packageName = packageName }
        every { packageManager.getApplicationInfo(packageName, 0) } returns appInfo
        every { packageManager.getApplicationLabel(appInfo) } returns appLabel
        return provider
    }

    @Test
    fun `availableWidgets is initially empty`() {
        assertThat(repository.availableWidgets.value).isEmpty()
    }

    @Test
    fun `refreshAvailableWidgets populates widgets sorted by appLabel`() =
        runTest {
            every { widgetHost.getInstalledProviders() } returns
                listOf(
                    stubProvider(packageName = "com.zeta", appLabel = "Zeta App"),
                    stubProvider(packageName = "com.alpha", appLabel = "Alpha App"),
                    stubProvider(packageName = "com.mike", appLabel = "Mike App"),
                )

            repository.refreshAvailableWidgets()

            val widgets = repository.availableWidgets.value
            assertThat(widgets.map { it.appLabel })
                .containsExactly("Alpha App", "Mike App", "Zeta App")
                .inOrder()
        }

    @Test
    fun `refreshAvailableWidgets falls back to package name when application info missing`() =
        runTest {
            val provider = spyk(AppWidgetProviderInfo())
            provider.provider = ComponentName("com.unknown", "UnknownProvider")
            provider.minWidth = 0
            provider.minHeight = 0
            provider.resizeMode = AppWidgetProviderInfo.RESIZE_NONE
            every { provider.loadLabel(any()) } returns "Unknown Widget"
            every { provider.loadPreviewImage(any(), any()) } returns null
            every { provider.loadIcon(any(), any()) } returns null
            every { packageManager.getApplicationInfo("com.unknown", 0) } throws
                PackageManager.NameNotFoundException()

            every { widgetHost.getInstalledProviders() } returns listOf(provider)

            repository.refreshAvailableWidgets()

            assertThat(repository.availableWidgets.value).hasSize(1)
            assertThat(repository.availableWidgets.value.first().appLabel).isEqualTo("com.unknown")
        }

    @Test
    fun `refreshAvailableWidgets skips providers that throw during loadLabel`() =
        runTest {
            val healthy = stubProvider(packageName = "com.healthy", appLabel = "Healthy")
            val broken = spyk(AppWidgetProviderInfo())
            broken.provider = ComponentName("com.broken", "BrokenProvider")
            every { broken.loadLabel(any()) } throws RuntimeException("kaboom")

            every { widgetHost.getInstalledProviders() } returns listOf(healthy, broken)

            repository.refreshAvailableWidgets()

            val widgets = repository.availableWidgets.value
            assertThat(widgets).hasSize(1)
            assertThat(widgets.first().appLabel).isEqualTo("Healthy")
        }

    @Test
    fun `refreshAvailableWidgets copies widget dimensions and resize mode`() =
        runTest {
            every { widgetHost.getInstalledProviders() } returns
                listOf(
                    stubProvider(
                        packageName = "com.foo",
                        widgetLabel = "Foo Widget",
                        appLabel = "Foo App",
                        minWidth = 250,
                        minHeight = 180,
                        resizeMode = AppWidgetProviderInfo.RESIZE_BOTH,
                    ),
                )

            repository.refreshAvailableWidgets()

            val info = repository.availableWidgets.value.single()
            assertThat(info.label).isEqualTo("Foo Widget")
            assertThat(info.appLabel).isEqualTo("Foo App")
            assertThat(info.minWidth).isEqualTo(250)
            assertThat(info.minHeight).isEqualTo(180)
            assertThat(info.resizeMode).isEqualTo(AppWidgetProviderInfo.RESIZE_BOTH)
        }

    @Test
    fun `getWidgetsByApp groups widgets sharing the same app`() =
        runTest {
            every { widgetHost.getInstalledProviders() } returns
                listOf(
                    stubProvider(
                        packageName = "com.foo",
                        className = "WidgetA",
                        widgetLabel = "A",
                        appLabel = "Foo",
                    ),
                    stubProvider(
                        packageName = "com.foo",
                        className = "WidgetB",
                        widgetLabel = "B",
                        appLabel = "Foo",
                    ),
                    stubProvider(
                        packageName = "com.bar",
                        className = "WidgetC",
                        widgetLabel = "C",
                        appLabel = "Bar",
                    ),
                )

            repository.refreshAvailableWidgets()

            val grouped = repository.getWidgetsByApp()
            assertThat(grouped.keys).containsExactly("Bar", "Foo")
            assertThat(grouped["Foo"]).hasSize(2)
            assertThat(grouped["Bar"]).hasSize(1)
        }

    @Test
    fun `refreshAvailableWidgets is empty when host returns no providers`() =
        runTest {
            every { widgetHost.getInstalledProviders() } returns emptyList()

            repository.refreshAvailableWidgets()

            assertThat(repository.availableWidgets.value).isEmpty()
            assertThat(repository.getWidgetsByApp()).isEmpty()
        }
}
