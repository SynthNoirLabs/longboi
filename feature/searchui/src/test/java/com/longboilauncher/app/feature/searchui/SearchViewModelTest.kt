package com.longboilauncher.app.feature.searchui

import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.feature.home.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val appCatalogRepository = mockk<AppCatalogRepository>()
    private val favoritesRepository = mockk<FavoritesRepository>()
    private lateinit var viewModel: SearchViewModel

    private val testApps = listOf(
        AppEntry(packageName = "com.android.settings", className = "Settings", label = "Settings", profile = ProfileType.PERSONAL),
        AppEntry(packageName = "com.google.android.youtube", className = "YouTube", label = "YouTube", profile = ProfileType.PERSONAL),
        AppEntry(packageName = "com.google.android.apps.maps", className = "Maps", label = "Maps", profile = ProfileType.PERSONAL),
        AppEntry(packageName = "com.whatsapp", className = "WhatsApp", label = "WhatsApp", profile = ProfileType.PERSONAL)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { appCatalogRepository.apps } returns MutableStateFlow(testApps)
        every { favoritesRepository.hiddenApps } returns MutableStateFlow(emptySet())
        viewModel = SearchViewModel(appCatalogRepository, favoritesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search returns empty results for empty query`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().searchResults).isEmpty()
            viewModel.onEvent(SearchEvent.UpdateSearchQuery(""))
            // The flow emits when uiState changes.
        }
    }

    @Test
    fun `search filters apps by label`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().searchResults).isEmpty() // Initial empty state

            viewModel.onEvent(SearchEvent.UpdateSearchQuery("you"))
            val state = awaitItem()
            assertThat(state.searchResults).hasSize(1)
            assertThat(state.searchResults[0].label).isEqualTo("YouTube")
        }
    }

    @Test
    fun `search excludes hidden apps`() = runTest {
        every { favoritesRepository.hiddenApps } returns MutableStateFlow(setOf("com.google.android.youtube"))
        viewModel = SearchViewModel(appCatalogRepository, favoritesRepository)

        viewModel.uiState.test {
            assertThat(awaitItem().searchResults).isEmpty()
            viewModel.onEvent(SearchEvent.UpdateSearchQuery("you"))
            assertThat(awaitItem().searchResults).isEmpty()
        }
    }

    @Test
    fun `fuzzy match handles small typos`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().searchResults).isEmpty()
            viewModel.onEvent(SearchEvent.UpdateSearchQuery("youtub")) // missing 'e'
            val state = awaitItem()
            assertThat(state.searchResults).isNotEmpty()
            assertThat(state.searchResults[0].label).isEqualTo("YouTube")
        }
    }

    @Test
    fun `acronym match works`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().searchResults).isEmpty()
            viewModel.onEvent(SearchEvent.UpdateSearchQuery("yt")) // YouTube -> YT
            val state = awaitItem()
            assertThat(state.searchResults).isNotEmpty()
            assertThat(state.searchResults[0].label).isEqualTo("YouTube")
        }
    }
}
