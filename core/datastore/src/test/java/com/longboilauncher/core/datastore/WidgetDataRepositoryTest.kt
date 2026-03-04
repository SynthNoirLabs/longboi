package com.longboilauncher.core.datastore

import android.content.ComponentName
import androidx.datastore.core.DataStore
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.core.datastore.testing.FakeUserSettingsDataStore
import com.longboilauncher.core.datastore_proto.UserSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WidgetDataRepositoryTest {
    private lateinit var dataStore: DataStore<UserSettings>
    private lateinit var repository: WidgetDataRepository

    private fun createMockComponentName(
        packageName: String,
        className: String,
    ): ComponentName =
        mockk<ComponentName>(relaxed = true) {
            every { getPackageName() } returns packageName
            every { getClassName() } returns className
            every { this@mockk.packageName } returns packageName
            every { this@mockk.className } returns className
        }

    @Before
    fun setup() {
        dataStore = FakeUserSettingsDataStore()
        repository = WidgetDataRepository(dataStore)
    }

    @Test
    fun `initial state has no widgets`() =
        runTest {
            val widgets = repository.widgets.first()
            assertThat(widgets).isEmpty()
        }

    @Test
    fun `addWidget adds widget to list`() =
        runTest {
            val widget =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example", "Widget"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )

            repository.addWidget(widget)

            val widgets = repository.widgets.first()
            assertThat(widgets).hasSize(1)
            assertThat(widgets[0].appWidgetId).isEqualTo(1)
        }

    @Test
    fun `addWidget does not add duplicate widget`() =
        runTest {
            val widget =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example", "Widget"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )

            repository.addWidget(widget)
            repository.addWidget(widget)

            val widgets = repository.widgets.first()
            assertThat(widgets).hasSize(1)
        }

    @Test
    fun `removeWidget removes widget from list`() =
        runTest {
            val widget =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example", "Widget"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )
            repository.addWidget(widget)

            repository.removeWidget(1)

            val widgets = repository.widgets.first()
            assertThat(widgets).isEmpty()
        }

    @Test
    fun `updateWidgetPosition updates position and span`() =
        runTest {
            val widget =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example", "Widget"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )
            repository.addWidget(widget)

            repository.updateWidgetPosition(
                appWidgetId = 1,
                positionX = 10,
                positionY = 20,
                spanX = 3,
                spanY = 4,
            )

            val widgets = repository.widgets.first()
            assertThat(widgets[0].positionX).isEqualTo(10)
            assertThat(widgets[0].positionY).isEqualTo(20)
            assertThat(widgets[0].spanX).isEqualTo(3)
            assertThat(widgets[0].spanY).isEqualTo(4)
        }

    @Test
    fun `updateWidgetPosition does not affect other widgets`() =
        runTest {
            val widget1 =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example", "Widget1"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )
            val widget2 =
                PersistedWidgetEntry(
                    appWidgetId = 2,
                    providerComponent = createMockComponentName("com.example", "Widget2"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 5,
                    positionY = 5,
                    spanX = 1,
                    spanY = 1,
                )
            repository.addWidget(widget1)
            repository.addWidget(widget2)

            repository.updateWidgetPosition(
                appWidgetId = 1,
                positionX = 10,
                positionY = 20,
                spanX = 3,
                spanY = 4,
            )

            val widgets = repository.widgets.first()
            val unchangedWidget = widgets.find { it.appWidgetId == 2 }
            assertThat(unchangedWidget?.positionX).isEqualTo(5)
            assertThat(unchangedWidget?.positionY).isEqualTo(5)
        }

    @Test
    fun `clearAllWidgets removes all widgets`() =
        runTest {
            val widget1 =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example", "Widget1"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )
            val widget2 =
                PersistedWidgetEntry(
                    appWidgetId = 2,
                    providerComponent = createMockComponentName("com.example", "Widget2"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 5,
                    positionY = 5,
                    spanX = 1,
                    spanY = 1,
                )
            repository.addWidget(widget1)
            repository.addWidget(widget2)

            repository.clearAllWidgets()

            val widgets = repository.widgets.first()
            assertThat(widgets).isEmpty()
        }

    @Test
    fun `widgets flow preserves component name`() =
        runTest {
            val widget =
                PersistedWidgetEntry(
                    appWidgetId = 1,
                    providerComponent = createMockComponentName("com.example.app", "com.example.app.MyWidget"),
                    minWidth = 100,
                    minHeight = 100,
                    positionX = 0,
                    positionY = 0,
                    spanX = 2,
                    spanY = 2,
                )

            repository.addWidget(widget)

            val widgets = repository.widgets.first()
            assertThat(widgets[0].providerComponent.packageName).isEqualTo("com.example.app")
            assertThat(widgets[0].providerComponent.className).isEqualTo("com.example.app.MyWidget")
        }
}
