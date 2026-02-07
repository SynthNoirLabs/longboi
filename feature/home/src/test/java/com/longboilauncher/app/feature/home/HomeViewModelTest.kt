package com.longboilauncher.app.feature.home

import android.content.pm.ShortcutInfo
import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.common.ClockTicker
import com.longboilauncher.app.core.common.NowProvider
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ProfileType
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

/**
 * Tests for [HomeViewModel].
 *
 * We use a mocked ClockTicker to control time-based updates and avoid infinite loops.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var appCatalogRepository: AppCatalogRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var nowProvider: NowProvider
    private lateinit var clockTicker: ClockTicker

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

        every { appCatalogRepository.apps } returns MutableStateFlow(listOf(testApp))
        every { favoritesRepository.favorites } returns MutableStateFlow(testFavorites)
        coEvery { appCatalogRepository.refreshAppCatalog() } returns Unit
        every { nowProvider.now() } returns Instant.parse("2026-01-17T12:00:00Z")
        // Default clock ticker mock that emits once and then hangs (to avoid infinite loop in tests)
        every { clockTicker.tick(any()) } returns flowOf(Unit)
    }

    private fun createViewModel() =
        HomeViewModel(
            appCatalogRepository,
            favoritesRepository,
            nowProvider,
            clockTicker,
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
    fun `uiState emits values from repository`() =
        runTest {
            createMocks()
            val viewModel = createViewModel()

            val state = viewModel.uiState.value
            assertThat(state.favorites).hasSize(1)
            assertThat(state.favorites[0].id).isEqualTo("fav_1")
        }

    @Test
    fun `glanceData emits clock updates`() =
        runTest {
            createMocks()
            // Override clock ticker to emit Unit
            val tickerFlow = MutableSharedFlow<Unit>(replay = 1)
            every { clockTicker.tick(any()) } returns tickerFlow
            tickerFlow.tryEmit(Unit)

            val viewModel = createViewModel()

            val state = viewModel.uiState.value
            assertThat(state.glanceData.currentTime).isNotEmpty()
            // 2026-01-17 is a Saturday
            assertThat(state.glanceData.currentDate).contains("January 17")
            assertThat(state.glanceData.currentDate).contains("Saturday")
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

            assertThat(viewModel.uiState.value.currentSurface).isEqualTo(LauncherSurface.HOME)

            viewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.ALL_APPS))
            assertThat(viewModel.uiState.value.currentSurface).isEqualTo(LauncherSurface.ALL_APPS)

            viewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH))
            assertThat(viewModel.uiState.value.currentSurface).isEqualTo(LauncherSurface.SEARCH)
        }

    @Test
    fun `showPopup sets popupApp and fetches shortcuts`() =
        runTest(testDispatcher) {
            createMocks()
            val shortcut = mockk<ShortcutInfo> {
                every { id } returns "test_shortcut"
                every { shortLabel } returns "Test Shortcut"
                every { iconUri } returns null
                every { intent } returns null
            }
            coEvery { appCatalogRepository.getAppShortcuts(testApp) } returns listOf(shortcut)
            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.popupApp).isNull()

            viewModel.onEvent(HomeEvent.ShowPopup(testApp))

            assertThat(viewModel.uiState.value.popupApp).isEqualTo(testApp)
            assertThat(viewModel.uiState.value.popupShortcuts).hasSize(1)
            assertThat(viewModel.uiState.value.popupShortcuts[0].id).isEqualTo("test_shortcut")
            assertThat(viewModel.uiState.value.popupShortcuts[0].label).isEqualTo("Test Shortcut")
            coVerify { appCatalogRepository.getAppShortcuts(testApp) }
        }

    @Test
    fun `hidePopup clears popupApp and shortcuts`() =
        runTest(testDispatcher) {
            createMocks()
            coEvery { appCatalogRepository.getAppShortcuts(any()) } returns emptyList()
            val viewModel = createViewModel()

            viewModel.onEvent(HomeEvent.ShowPopup(testApp))
            assertThat(viewModel.uiState.value.popupApp).isNotNull()

            viewModel.onEvent(HomeEvent.HidePopup)

            assertThat(viewModel.uiState.value.popupApp).isNull()
            assertThat(viewModel.uiState.value.popupShortcuts).isEmpty()
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

            viewModel.onEvent(HomeEvent.ShowPopup(testApp))
            assertThat(viewModel.uiState.value.popupApp).isNotNull()

            viewModel.onEvent(HomeEvent.HideApp)

            coVerify { favoritesRepository.hideApp(testApp.packageName) }
            assertThat(viewModel.uiState.value.popupApp).isNull()
        }
}
