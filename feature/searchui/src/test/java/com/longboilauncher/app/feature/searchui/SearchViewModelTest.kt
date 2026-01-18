package com.longboilauncher.app.feature.searchui

import android.os.Build
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
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
        runTest {
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.searchResults).isEmpty()

            viewModel.onEvent(SearchEvent.UpdateSearchQuery(""))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.searchResults).isEmpty()
        }

    @Test
    fun `search filters apps by label`() =
        runTest {
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.searchResults).isEmpty()

            viewModel.onEvent(SearchEvent.UpdateSearchQuery("you"))
            advanceUntilIdle()

            val results = viewModel.uiState.value.searchResults
            assertThat(results).isNotEmpty()
            val appResults = results.filterIsInstance<SearchResult.AppResult>()
            assertThat(appResults).hasSize(1)
            assertThat(appResults[0].app.label).isEqualTo("YouTube")
        }

    @Test
    fun `search excludes hidden apps`() =
        runTest {
            every { favoritesRepository.hiddenApps } returns MutableStateFlow(setOf("com.google.android.youtube"))
            every { favoritesRepository.favorites } returns MutableStateFlow(emptyList())
            viewModel = SearchViewModel(appCatalogRepository, favoritesRepository)
            advanceUntilIdle()

            viewModel.onEvent(SearchEvent.UpdateSearchQuery("you"))
            advanceUntilIdle()

            val appResults =
                viewModel.uiState.value.searchResults
                    .filterIsInstance<SearchResult.AppResult>()
            assertThat(appResults).isEmpty()
        }

    @Test
    fun `fuzzy match handles small typos`() =
        runTest {
            advanceUntilIdle()

            // "youtub" is 6 chars, and fuzzy match allows 1 char difference
            viewModel.onEvent(SearchEvent.UpdateSearchQuery("youtub"))
            advanceUntilIdle()

            val appResults =
                viewModel.uiState.value.searchResults
                    .filterIsInstance<SearchResult.AppResult>()
            assertThat(appResults).isNotEmpty()
            assertThat(appResults[0].app.label).isEqualTo("YouTube")
        }

    @Test
    fun `acronym match works`() =
        runTest {
            advanceUntilIdle()

            // YouTube has acronym "YT" (uppercase letters)
            viewModel.onEvent(SearchEvent.UpdateSearchQuery("yt"))
            advanceUntilIdle()

            val appResults =
                viewModel.uiState.value.searchResults
                    .filterIsInstance<SearchResult.AppResult>()
            assertThat(appResults).isNotEmpty()
            assertThat(appResults[0].app.label).isEqualTo("YouTube")
        }

    @Test
    fun `calculator evaluates simple expressions`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SearchEvent.UpdateSearchQuery("123+456"))
            advanceUntilIdle()

            val results = viewModel.uiState.value.searchResults
            assertThat(results).isNotEmpty()
            val calcResult = results.filterIsInstance<SearchResult.CalculatorResult>().firstOrNull()
            assertThat(calcResult).isNotNull()
            assertThat(calcResult!!.expression).isEqualTo("123+456")
            assertThat(calcResult.result).isEqualTo("579")
        }

    @Test
    fun `calculator handles decimals`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SearchEvent.UpdateSearchQuery("10.5+2.3"))
            advanceUntilIdle()

            val results = viewModel.uiState.value.searchResults
            val calcResult = results.filterIsInstance<SearchResult.CalculatorResult>().firstOrNull()
            assertThat(calcResult).isNotNull()
            assertThat(calcResult!!.result).isEqualTo("12.8")
        }

    @Test
    fun `settings shortcuts appear`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SearchEvent.UpdateSearchQuery("wifi"))
            advanceUntilIdle()

            val results = viewModel.uiState.value.searchResults
            assertThat(results).isNotEmpty()
            val settings = results.filterIsInstance<SearchResult.SettingsShortcutResult>()
            assertThat(settings).isNotEmpty()
            assertThat(settings[0].title).isEqualTo("Wi-Fi")
        }

    @Test
    fun `mixed results prioritize calculator then settings then apps`() =
        runTest {
            advanceUntilIdle()

            // "1+1" will match calculator
            viewModel.onEvent(SearchEvent.UpdateSearchQuery("1+1"))
            advanceUntilIdle()

            val results = viewModel.uiState.value.searchResults
            assertThat(results).isNotEmpty()
            // Calculator should be first
            assertThat(results[0]).isInstanceOf(SearchResult.CalculatorResult::class.java)
        }
}
