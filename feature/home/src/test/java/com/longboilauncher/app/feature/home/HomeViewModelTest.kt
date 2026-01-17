package com.longboilauncher.app.feature.home

import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.feature.home.HomeViewModel
import com.longboilauncher.app.feature.home.HomeEvent
import com.longboilauncher.app.feature.home.LauncherSurface
import com.longboilauncher.app.core.common.NowProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import java.time.Instant
import android.content.pm.ShortcutInfo

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val appCatalogRepository = mockk<AppCatalogRepository>()
    private val favoritesRepository = mockk<FavoritesRepository>()
    private val nowProvider = mockk<NowProvider>()
    private lateinit var viewModel: HomeViewModel

    private val testApp = AppEntry(
        packageName = "com.test.app",
        className = "MainActivity",
        label = "Test App",
        userIdentifier = 0,
        profile = ProfileType.PERSONAL
    )

    private val testFavorites = listOf(
        FavoriteEntry(
            id = "fav_1",
            appEntry = testApp,
            position = 0
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { appCatalogRepository.apps } returns MutableStateFlow(listOf(testApp))
        every { favoritesRepository.favorites } returns MutableStateFlow(testFavorites)
        coEvery { appCatalogRepository.refreshAppCatalog() } returns Unit
        every { nowProvider.now() } returns Instant.parse("2026-01-17T12:00:00Z")

        viewModel = HomeViewModel(appCatalogRepository, favoritesRepository, nowProvider)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial data load triggers app catalog refresh`() = runTest {
        coVerify { appCatalogRepository.refreshAppCatalog() }
    }

    @Test
    fun `uiState emits values from repository`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.favorites).hasSize(1)
            assertThat(state.favorites[0].id).isEqualTo("fav_1")
        }
    }

    @Test
    fun `glanceData emits clock updates every second`() = runTest {
        viewModel.uiState.test {
            val first = awaitItem()
            assertThat(first.glanceData.currentTime).isNotEmpty()

            advanceTimeBy(1001) // Wait more than a second
            val second = awaitItem()
            assertThat(second.glanceData.currentTime).isNotEmpty()
        }
    }

    @Test
    fun `launchApp calls repository`() {
        every { appCatalogRepository.launchApp(any()) } returns Unit
        viewModel.onEvent(HomeEvent.LaunchApp(testApp))
        coVerify { appCatalogRepository.launchApp(testApp) }
    }

    @Test
    fun `launchFavorite calls repository with appEntry`() {
        every { appCatalogRepository.launchApp(any()) } returns Unit
        viewModel.onEvent(HomeEvent.LaunchFavorite(testFavorites[0]))
        coVerify { appCatalogRepository.launchApp(testApp) }
    }

    @Test
    fun `addToFavorites calls repository`() = runTest {
        coEvery { favoritesRepository.addFavorite(any()) } returns Unit
        viewModel.onEvent(HomeEvent.AddToFavorites(testApp))
        coVerify { favoritesRepository.addFavorite(testApp) }
    }

    @Test
    fun `removeFromFavorites calls repository`() = runTest {
        coEvery { favoritesRepository.removeFavorite(any()) } returns Unit
        viewModel.onEvent(HomeEvent.RemoveFromFavorites("fav_1"))
        coVerify { favoritesRepository.removeFavorite("fav_1") }
    }

    @Test
    fun `navigation updates currentSurface`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().currentSurface).isEqualTo(LauncherSurface.HOME)

            viewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.ALL_APPS))
            assertThat(awaitItem().currentSurface).isEqualTo(LauncherSurface.ALL_APPS)

            viewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH))
            assertThat(awaitItem().currentSurface).isEqualTo(LauncherSurface.SEARCH)
        }
    }

    @Test
    fun `showPopup sets popupApp and fetches shortcuts`() = runTest {
        val shortcut = mockk<ShortcutInfo>()
        coEvery { appCatalogRepository.getAppShortcuts(testApp) } returns listOf(shortcut)

        viewModel.uiState.test {
            assertThat(awaitItem().popupApp).isNull()

            viewModel.onEvent(HomeEvent.ShowPopup(testApp))
            val state = awaitItem()
            assertThat(state.popupApp).isEqualTo(testApp)
            // Shortcuts are fetched asynchronously; advance to ensure they arrive
            advanceTimeBy(100)
            assertThat(awaitItem().popupShortcuts).hasSize(1)
        }
        coVerify { appCatalogRepository.getAppShortcuts(testApp) }
    }

    @Test
    fun `hidePopup clears popupApp and shortcuts`() = runTest {
        // First show a popup
        coEvery { appCatalogRepository.getAppShortcuts(any()) } returns emptyList()
        viewModel.onEvent(HomeEvent.ShowPopup(testApp))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.popupApp).isNotNull()

            viewModel.onEvent(HomeEvent.HidePopup)
            val state = awaitItem()
            assertThat(state.popupApp).isNull()
            assertThat(state.popupShortcuts).isEmpty()
        }
    }

    @Test
    fun `launchShortcut calls repository`() {
        every { appCatalogRepository.launchShortcut(any(), any()) } returns Unit
        viewModel.onEvent(HomeEvent.LaunchShortcut(testApp, "shortcut_1"))
        coVerify { appCatalogRepository.launchShortcut(testApp, "shortcut_1") }
    }

    @Test
    fun `hideApp calls repository and closes popup`() = runTest {
        coEvery { favoritesRepository.hideApp(any()) } returns Unit
        // First show popup
        coEvery { appCatalogRepository.getAppShortcuts(any()) } returns emptyList()
        viewModel.onEvent(HomeEvent.ShowPopup(testApp))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertThat(awaitItem().popupApp).isNotNull()

            viewModel.onEvent(HomeEvent.HideApp)
            coVerify { favoritesRepository.hideApp(testApp.packageName)
            val state = awaitItem()
            assertThat(state.popupApp).isNull()
        }
    }
}
