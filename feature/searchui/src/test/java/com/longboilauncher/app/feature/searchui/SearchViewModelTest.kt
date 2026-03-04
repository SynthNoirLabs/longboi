package com.longboilauncher.app.feature.searchui

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Search VM tests use [UnconfinedTestDispatcher] so coroutines execute eagerly,
 * keeping the Turbine `test {}` flow assertions straightforward — no need to
 * call `advanceUntilIdle()` inside the Turbine context.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SearchViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val appCatalogRepository = mockk<AppCatalogRepository>(relaxed = true)
    private val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
    private lateinit var viewModel: SearchViewModel

    private val testApps =
        listOf(
            AppEntry(
                packageName = "com.android.settings",
                className = "Settings",
                label = "Settings",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.google.android.youtube",
                className = "YouTube",
                label = "YouTube",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.google.android.apps.maps",
                className = "Maps",
                label = "Maps",
                profile = ProfileType.PERSONAL,
            ),
            AppEntry(
                packageName = "com.whatsapp",
                className = "WhatsApp",
                label = "WhatsApp",
                profile = ProfileType.PERSONAL,
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { appCatalogRepository.apps } returns MutableStateFlow(testApps)
        every { favoritesRepository.hiddenApps } returns MutableStateFlow(emptySet<String>())
        every { favoritesRepository.favorites } returns MutableStateFlow(emptyList())
        viewModel = SearchViewModel(appCatalogRepository, favoritesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search returns empty results for empty query`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                assertThat(awaitItem().searchResults).isEmpty()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery(""))
                // Empty query — state unchanged, no new emission
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `search filters apps by label`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem() // initial empty

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("you"))

                val state = awaitItem()
                val appResults = state.searchResults.filterIsInstance<SearchResult.AppResult>()
                assertThat(appResults).hasSize(1)
                assertThat(appResults[0].app.label).isEqualTo("YouTube")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `search excludes hidden apps`() =
        runTest(testDispatcher) {
            every { favoritesRepository.hiddenApps } returns
                MutableStateFlow(setOf("com.google.android.youtube"))
            viewModel = SearchViewModel(appCatalogRepository, favoritesRepository)

            viewModel.uiState.test {
                awaitItem() // initial

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("you"))

                val state = awaitItem()
                val appResults = state.searchResults.filterIsInstance<SearchResult.AppResult>()
                assertThat(appResults).isEmpty()

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fuzzy match handles small typos`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("youtub"))

                val state = awaitItem()
                val appResults = state.searchResults.filterIsInstance<SearchResult.AppResult>()
                assertThat(appResults).isNotEmpty()
                assertThat(appResults[0].app.label).isEqualTo("YouTube")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `acronym match works`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("yt"))

                val state = awaitItem()
                val appResults = state.searchResults.filterIsInstance<SearchResult.AppResult>()
                assertThat(appResults).isNotEmpty()
                assertThat(appResults[0].app.label).isEqualTo("YouTube")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `calculator evaluates simple expressions`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("123+456"))

                val state = awaitItem()
                val calcResult =
                    state.searchResults.filterIsInstance<SearchResult.CalculatorResult>().firstOrNull()
                assertThat(calcResult).isNotNull()
                assertThat(calcResult!!.expression).isEqualTo("123+456")
                assertThat(calcResult.result).isEqualTo("579")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `calculator handles decimals`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("10.5+2.3"))

                val state = awaitItem()
                val calcResult =
                    state.searchResults.filterIsInstance<SearchResult.CalculatorResult>().firstOrNull()
                assertThat(calcResult).isNotNull()
                assertThat(calcResult!!.result).isEqualTo("12.8")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `settings shortcuts appear`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("wifi"))

                val state = awaitItem()
                val settings =
                    state.searchResults.filterIsInstance<SearchResult.SettingsShortcutResult>()
                assertThat(settings).isNotEmpty()
                assertThat(settings[0].title).isEqualTo("Wi-Fi")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `mixed results prioritize calculator then settings then apps`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(SearchEvent.UpdateSearchQuery("1+1"))

                val state = awaitItem()
                assertThat(state.searchResults).isNotEmpty()
                assertThat(state.searchResults[0]).isInstanceOf(SearchResult.CalculatorResult::class.java)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
