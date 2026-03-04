package com.longboilauncher.app.feature.widgetpicker

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.widgets.WidgetBindResult
import com.longboilauncher.app.core.widgets.WidgetHost
import com.longboilauncher.app.core.widgets.WidgetInfo
import com.longboilauncher.app.core.widgets.WidgetRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WidgetPickerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var widgetRepository: WidgetRepository
    private lateinit var widgetHost: WidgetHost
    private lateinit var viewModel: WidgetPickerViewModel

    private val testWidget: WidgetInfo =
        mockk(relaxed = true) {
            every { label } returns "Test Widget"
            every { appLabel } returns "Test App"
        }

    private val testWidgetsByApp =
        mapOf(
            "Test App" to listOf(testWidget),
            "Another App" to
                listOf(
                    mockk<WidgetInfo>(relaxed = true) {
                        every { label } returns "Another Widget"
                        every { appLabel } returns "Another App"
                    },
                ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        widgetRepository =
            mockk(relaxed = true) {
                coEvery { getWidgetsByApp() } returns testWidgetsByApp
            }
        widgetHost = mockk(relaxed = true)
        viewModel = WidgetPickerViewModel(widgetRepository, widgetHost)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `loadWidgets populates widgetsByApp`() =
        runTest(testDispatcher) {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.widgetsByApp).hasSize(2)
        }

    @Test
    fun `UpdateSearchQuery updates state`() =
        runTest(testDispatcher) {
            viewModel.onEvent(WidgetPickerEvent.UpdateSearchQuery("test"))
            assertThat(viewModel.uiState.value.searchQuery).isEqualTo("test")
        }

    @Test
    fun `ToggleAppExpanded adds app to expanded set`() =
        runTest(testDispatcher) {
            viewModel.onEvent(WidgetPickerEvent.ToggleAppExpanded("Test App"))
            assertThat(viewModel.uiState.value.expandedApps).contains("Test App")
        }

    @Test
    fun `ToggleAppExpanded removes app from expanded set when already expanded`() =
        runTest(testDispatcher) {
            viewModel.onEvent(WidgetPickerEvent.ToggleAppExpanded("Test App"))
            viewModel.onEvent(WidgetPickerEvent.ToggleAppExpanded("Test App"))
            assertThat(viewModel.uiState.value.expandedApps).doesNotContain("Test App")
        }

    @Test
    fun `getFilteredWidgets returns all when search is empty`() =
        runTest(testDispatcher) {
            testDispatcher.scheduler.advanceUntilIdle()

            val filtered = viewModel.getFilteredWidgets()
            assertThat(filtered).hasSize(2)
        }

    @Test
    fun `getFilteredWidgets filters by search query`() =
        runTest(testDispatcher) {
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onEvent(WidgetPickerEvent.UpdateSearchQuery("Test"))

            val filtered = viewModel.getFilteredWidgets()
            assertThat(filtered).hasSize(1)
            assertThat(filtered.keys).contains("Test App")
        }

    @Test
    fun `SelectWidget allocates widget id and attempts bind`() =
        runTest(testDispatcher) {
            every { widgetHost.allocateAppWidgetId() } returns 123
            every { widgetHost.bindWidget(any(), any()) } returns WidgetBindResult.Success

            viewModel.effects.test {
                viewModel.onEvent(WidgetPickerEvent.SelectWidget(testWidget))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = awaitItem()
                assertThat(effect).isInstanceOf(WidgetPickerEffect.WidgetAdded::class.java)
                assertThat((effect as WidgetPickerEffect.WidgetAdded).appWidgetId).isEqualTo(123)
            }

            verify { widgetHost.allocateAppWidgetId() }
        }

    @Test
    fun `OnBindPermissionResult granted completes widget add`() =
        runTest(testDispatcher) {
            every { widgetHost.allocateAppWidgetId() } returns 456
            every { widgetHost.bindWidget(any(), any()) } returns WidgetBindResult.NeedsPermission(mockk())
            every { widgetHost.onBindPermissionGranted(any(), any()) } returns WidgetBindResult.Success

            // First select widget to set pending state
            viewModel.onEvent(WidgetPickerEvent.SelectWidget(testWidget))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(WidgetPickerEvent.OnBindPermissionResult(true))
                testDispatcher.scheduler.advanceUntilIdle()

                // Skip the permission intent effect
                skipItems(1)

                val effect = awaitItem()
                assertThat(effect).isInstanceOf(WidgetPickerEffect.WidgetAdded::class.java)
            }
        }

    @Test
    fun `OnBindPermissionResult denied emits failure`() =
        runTest(testDispatcher) {
            every { widgetHost.allocateAppWidgetId() } returns 789
            every { widgetHost.bindWidget(any(), any()) } returns WidgetBindResult.NeedsPermission(mockk())

            viewModel.onEvent(WidgetPickerEvent.SelectWidget(testWidget))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(WidgetPickerEvent.OnBindPermissionResult(false))
                testDispatcher.scheduler.advanceUntilIdle()

                // Skip permission intent
                skipItems(1)

                val effect = awaitItem()
                assertThat(effect).isEqualTo(WidgetPickerEffect.WidgetAddFailed)
            }

            verify { widgetHost.deleteAppWidgetId(789) }
        }
}
