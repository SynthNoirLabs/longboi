package com.longboilauncher.app.feature.home

import android.content.pm.ShortcutInfo
import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.common.ClockTicker
import com.longboilauncher.app.core.common.NowProvider
import com.longboilauncher.app.core.common.SystemServiceHelper
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.model.ThemeType
import com.longboilauncher.app.core.settings.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var appCatalogRepository: AppCatalogRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var nowProvider: NowProvider
    private lateinit var clockTicker: ClockTicker
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var systemServiceHelper: SystemServiceHelper

    private val testApp =
        AppEntry(
            packageName = "com.test.app",
            className = "MainActivity",
            label = "Test App",
            userIdentifier = 0,
            profile = ProfileType.PERSONAL,
        )

    private val testFavorites =
        listOf(
            FavoriteEntry(
                id = "fav_1",
                appEntry = testApp,
                position = 0,
            ),
        )

    private fun createMocks() {
        appCatalogRepository = mockk<AppCatalogRepository>(relaxed = true)
        favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
        nowProvider = mockk<NowProvider>()
        clockTicker = mockk<ClockTicker>()
        preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        systemServiceHelper = mockk<SystemServiceHelper>(relaxed = true)

        every { appCatalogRepository.apps } returns MutableStateFlow(listOf(testApp))
        every { favoritesRepository.favorites } returns MutableStateFlow(testFavorites)
        every { favoritesRepository.hiddenApps } returns MutableStateFlow(emptySet())
        coEvery { appCatalogRepository.refreshAppCatalog() } returns Unit
        every { nowProvider.now() } returns Instant.parse("2026-01-17T12:00:00Z")
        every { clockTicker.tick(any()) } returns flowOf(Unit)
        every { preferencesRepository.themeType } returns MutableStateFlow(ThemeType.MATERIAL_YOU)
    }

    private fun createViewModel() =
        HomeViewModel(
            appCatalogRepository,
            favoritesRepository,
            nowProvider,
            clockTicker,
            preferencesRepository,
            systemServiceHelper,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial data load triggers app catalog refresh`() =
        runTest {
            createMocks()
            createViewModel()
            coVerify { appCatalogRepository.refreshAppCatalog() }
        }

    @Test
    fun `uiState emits favorites from repository`() =
        runTest {
            createMocks()
            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.favorites).hasSize(1)
                assertThat(state.favorites[0].id).isEqualTo("fav_1")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `glanceData emits clock updates`() =
        runTest {
            createMocks()
            val tickerFlow = MutableSharedFlow<Unit>(replay = 1)
            every { clockTicker.tick(any()) } returns tickerFlow
            tickerFlow.tryEmit(Unit)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.glanceData.currentTime).isNotEmpty()
                assertThat(state.glanceData.currentDate).contains("January 17")
                assertThat(state.glanceData.currentDate).contains("Saturday")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `launchApp calls repository`() =
        runTest(testDispatcher) {
            createMocks()
            val viewModel = createViewModel()

            viewModel.onEvent(HomeEvent.LaunchApp(testApp))
            coVerify { appCatalogRepository.launchApp(testApp) }
        }

    @Test
    fun `launchFavorite calls repository with appEntry`() =
        runTest(testDispatcher) {
            createMocks()
            val viewModel = createViewModel()

            viewModel.onEvent(HomeEvent.LaunchFavorite(testFavorites[0]))
            coVerify { appCatalogRepository.launchApp(testApp) }
        }

    @Test
    fun `addToFavorites calls repository`() =
        runTest(testDispatcher) {
            createMocks()
            val viewModel = createViewModel()

            viewModel.onEvent(HomeEvent.AddToFavorites(testApp))
            coVerify { favoritesRepository.addFavorite(testApp) }
        }

    @Test
    fun `removeFromFavorites calls repository`() =
        runTest(testDispatcher) {
            createMocks()
            val viewModel = createViewModel()

            viewModel.onEvent(HomeEvent.RemoveFromFavorites("fav_1"))
            coVerify { favoritesRepository.removeFavorite("fav_1") }
        }

    @Test
    fun `navigation updates currentSurface`() =
        runTest(testDispatcher) {
            createMocks()
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertThat(awaitItem().currentSurface).isEqualTo(LauncherSurface.HOME)

                viewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.ALL_APPS))
                assertThat(awaitItem().currentSurface).isEqualTo(LauncherSurface.ALL_APPS)

                viewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH))
                assertThat(awaitItem().currentSurface).isEqualTo(LauncherSurface.SEARCH)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `showPopup sets popupApp and fetches shortcuts`() =
        runTest(testDispatcher) {
            createMocks()
            val shortcut = mockk<ShortcutInfo>()
            coEvery { appCatalogRepository.getAppShortcuts(testApp) } returns listOf(shortcut)
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertThat(awaitItem().popupApp).isNull() // initial

                viewModel.onEvent(HomeEvent.ShowPopup(testApp))

                // First update: popupApp is set synchronously
                val withPopup = awaitItem()
                assertThat(withPopup.popupApp).isEqualTo(testApp)

                // Second update: shortcuts loaded asynchronously in a launched coroutine
                val withShortcuts = awaitItem()
                assertThat(withShortcuts.popupShortcuts).hasSize(1)

                coVerify { appCatalogRepository.getAppShortcuts(testApp) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `hidePopup clears popupApp and shortcuts`() =
        runTest(testDispatcher) {
            createMocks()
            coEvery { appCatalogRepository.getAppShortcuts(any()) } returns emptyList()
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial

                viewModel.onEvent(HomeEvent.ShowPopup(testApp))
                assertThat(awaitItem().popupApp).isNotNull()

                viewModel.onEvent(HomeEvent.HidePopup)
                val cleared = awaitItem()
                assertThat(cleared.popupApp).isNull()
                assertThat(cleared.popupShortcuts).isEmpty()

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `launchShortcut calls repository`() =
        runTest(testDispatcher) {
            createMocks()
            val viewModel = createViewModel()

            viewModel.onEvent(HomeEvent.LaunchShortcut(testApp, "shortcut_1"))
            coVerify { appCatalogRepository.launchShortcut(testApp, "shortcut_1") }
        }

    @Test
    fun `hideApp calls repository and closes popup`() =
        runTest(testDispatcher) {
            createMocks()
            coEvery { appCatalogRepository.getAppShortcuts(any()) } returns emptyList<ShortcutInfo>()
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial

                viewModel.onEvent(HomeEvent.ShowPopup(testApp))
                assertThat(awaitItem().popupApp).isNotNull()

                viewModel.onEvent(HomeEvent.HideApp(testApp))
                val cleared = awaitItem()
                coVerify { favoritesRepository.hideApp(testApp.packageName) }
                assertThat(cleared.popupApp).isNull()

                cancelAndIgnoreRemainingEvents()
            }
        }
}
