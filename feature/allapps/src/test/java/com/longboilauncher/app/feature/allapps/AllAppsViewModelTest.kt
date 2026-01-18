package com.longboilauncher.app.feature.allapps

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.settings.PreferencesRepository
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
class AllAppsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val appCatalogRepository = mockk<AppCatalogRepository>()
    private val favoritesRepository = mockk<FavoritesRepository>()
    private val preferencesRepository = mockk<PreferencesRepository>()
    private lateinit var viewModel: AllAppsViewModel

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
                packageName = "com.work.app",
                className = "WorkApp",
                label = "WorkApp",
                profile = ProfileType.WORK,
            ),
            AppEntry(
                packageName = "com.private.app",
                className = "PrivateApp",
                label = "PrivateApp",
                profile = ProfileType.PRIVATE,
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { appCatalogRepository.apps } returns MutableStateFlow(testApps)
        every { favoritesRepository.hiddenApps } returns MutableStateFlow(emptySet())
        every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
        viewModel = AllAppsViewModel(appCatalogRepository, favoritesRepository, preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `apps are filtered by personal profile`() =
        runTest {
            viewModel.onEvent(AllAppsEvent.SelectProfile(ProfileFilter.PERSONAL))

            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.filteredApps.all { it.profile == ProfileType.PERSONAL }).isTrue()
                assertThat(state.filteredApps).hasSize(3)
            }
        }

    @Test
    fun `apps are filtered by work profile`() =
        runTest {
            viewModel.onEvent(AllAppsEvent.SelectProfile(ProfileFilter.WORK))

            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.filteredApps.all { it.profile == ProfileType.WORK }).isTrue()
                assertThat(state.filteredApps).hasSize(1)
                assertThat(state.filteredApps[0].label).isEqualTo("WorkApp")
            }
        }

    @Test
    fun `apps are grouped into sections correctly`() =
        runTest {
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.appSections.keys).containsExactly("M", "P", "S", "W", "Y")
                assertThat(state.appSections["M"]!![0].label).isEqualTo("Maps")
                assertThat(state.appSections["Y"]!![0].label).isEqualTo("YouTube")
            }
        }

    @Test
    fun `section indices are calculated correctly`() =
        runTest {
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                // Sections: M (1 app), P (1 app), S (1 app), W (1 app), Y (1 app)
                // Header M: index 0, App Maps: index 1
                // Header P: index 2, App PrivateApp: index 3
                // Header S: index 4, App Settings: index 5
                assertThat(state.sectionIndices["M"]).isEqualTo(0)
                assertThat(state.sectionIndices["P"]).isEqualTo(2)
                assertThat(state.sectionIndices["S"]).isEqualTo(4)
                assertThat(state.sectionIndices["W"]).isEqualTo(6)
                assertThat(state.sectionIndices["Y"]).isEqualTo(8)
            }
        }

    @Test
    fun `search query filters apps`() =
        runTest {
            viewModel.onEvent(AllAppsEvent.UpdateSearchQuery("set"))

            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.filteredApps).hasSize(1)
                assertThat(state.filteredApps[0].label).isEqualTo("Settings")
            }
        }

    @Test
    fun `hidden apps are excluded from list`() =
        runTest {
            every { favoritesRepository.hiddenApps } returns MutableStateFlow(setOf("com.google.android.youtube"))
            every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
            viewModel = AllAppsViewModel(appCatalogRepository, favoritesRepository, preferencesRepository)

            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.filteredApps.any { it.packageName == "com.google.android.youtube" }).isFalse()
            }
        }

    @Test
    fun `scrubberIndexForY maps y to valid index`() {
        assertThat(scrubberIndexForY(y = -10f, height = 100f, itemCount = 26)).isEqualTo(0)
        assertThat(scrubberIndexForY(y = 0f, height = 100f, itemCount = 26)).isEqualTo(0)
        assertThat(scrubberIndexForY(y = 99.9f, height = 100f, itemCount = 26)).isEqualTo(25)
        assertThat(scrubberIndexForY(y = 100f, height = 100f, itemCount = 26)).isEqualTo(25)
        assertThat(scrubberIndexForY(y = 200f, height = 100f, itemCount = 26)).isEqualTo(25)

        assertThat(scrubberIndexForY(y = 50f, height = 100f, itemCount = 10)).isEqualTo(5)
        assertThat(scrubberIndexForY(y = 0f, height = 0f, itemCount = 10)).isEqualTo(0)
        assertThat(scrubberIndexForY(y = 10f, height = 100f, itemCount = 0)).isEqualTo(0)
    }
}
