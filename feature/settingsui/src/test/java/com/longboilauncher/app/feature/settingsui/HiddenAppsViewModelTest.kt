package com.longboilauncher.app.feature.settingsui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class HiddenAppsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var appCatalogRepository: AppCatalogRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var viewModel: HiddenAppsViewModel

    private val testApps =
        listOf(
            AppEntry(
                packageName = "com.app.one",
                className = "MainActivity",
                label = "App One",
                userSerialNumber = 0L,
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.app.two",
                className = "MainActivity",
                label = "App Two",
                userSerialNumber = 0L,
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.app.three",
                className = "MainActivity",
                label = "App Three",
                userSerialNumber = 0L,
                profile = ProfileType.PERSONAL,
            ),
        )

    private val appsFlow = MutableStateFlow(testApps)
    private val hiddenAppsFlow = MutableStateFlow<Set<String>>(emptySet())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appCatalogRepository =
            mockk(relaxed = true) {
                every { apps } returns appsFlow
            }
        favoritesRepository =
            mockk(relaxed = true) {
                every { hiddenApps } returns hiddenAppsFlow
            }
        viewModel = HiddenAppsViewModel(appCatalogRepository, favoritesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state shows all apps as visible when none are hidden`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                // Skip initial empty state
                skipItems(1)
                val state = awaitItem()
                assertThat(state.visibleApps).hasSize(3)
                assertThat(state.hiddenApps).isEmpty()
            }
        }

    @Test
    fun `state correctly separates hidden and visible apps`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                skipItems(1) // initial
                awaitItem() // first emission with apps

                hiddenAppsFlow.value = setOf("com.app.two")

                val state = awaitItem()
                assertThat(state.hiddenApps).hasSize(1)
                assertThat(state.hiddenApps.first().packageName).isEqualTo("com.app.two")
                assertThat(state.visibleApps).hasSize(2)
            }
        }

    @Test
    fun `hideApp calls repository`() =
        runTest(testDispatcher) {
            viewModel.hideApp("com.app.one")
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { favoritesRepository.hideApp("com.app.one") }
        }

    @Test
    fun `unhideApp calls repository`() =
        runTest(testDispatcher) {
            viewModel.unhideApp("com.app.two")
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { favoritesRepository.unhideApp("com.app.two") }
        }

    @Test
    fun `apps are sorted alphabetically`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                skipItems(1) // initial empty
                val state = awaitItem()
                val labels = state.visibleApps.map { it.label }
                assertThat(labels).isEqualTo(listOf("App One", "App Three", "App Two"))
            }
        }
}
