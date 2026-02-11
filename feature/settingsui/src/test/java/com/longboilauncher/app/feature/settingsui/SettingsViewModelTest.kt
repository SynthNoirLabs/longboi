package com.longboilauncher.app.feature.settingsui

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.model.ThemeType
import com.longboilauncher.app.core.settings.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesRepository.themeType } returns MutableStateFlow(ThemeType.MATERIAL_YOU)
        every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
        every { preferencesRepository.showNotifications } returns MutableStateFlow(true)

        viewModel = SettingsViewModel(preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits from repository`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.theme).isEqualTo(ThemeType.MATERIAL_YOU)
                assertThat(state.hapticsEnabled).isTrue()
            }
        }

    @Test
    fun `setTheme calls repository`() =
        runTest {
            coEvery { preferencesRepository.setTheme(any()) } returns Unit
            viewModel.onEvent(SettingsEvent.SetTheme(ThemeType.SOPHISTICATED_SLEEK))
            advanceUntilIdle()
            coVerify { preferencesRepository.setTheme("sophisticated_sleek") }
        }

    @Test
    fun `setHapticsEnabled calls repository`() =
        runTest {
            coEvery { preferencesRepository.setHapticsEnabled(any()) } returns Unit
            viewModel.onEvent(SettingsEvent.SetHapticsEnabled(false))
            advanceUntilIdle()
            coVerify { preferencesRepository.setHapticsEnabled(false) }
        }

    @Test
    fun `setShowNotifications calls repository`() =
        runTest {
            coEvery { preferencesRepository.setShowNotifications(any()) } returns Unit
            viewModel.onEvent(SettingsEvent.SetShowNotifications(false))
            advanceUntilIdle()
            coVerify { preferencesRepository.setShowNotifications(false) }
        }
}
