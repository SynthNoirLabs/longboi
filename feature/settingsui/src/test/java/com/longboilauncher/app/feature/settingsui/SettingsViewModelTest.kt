package com.longboilauncher.app.feature.settingsui

import com.longboilauncher.app.core.settings.PreferencesRepository
import io.mockk.coEvery
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
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesRepository.theme } returns MutableStateFlow("system")
        every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
        every { preferencesRepository.showNotifications } returns MutableStateFlow(true)

        viewModel = SettingsViewModel(preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits from repository`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.theme).isEqualTo("system")
            assertThat(state.hapticsEnabled).isTrue()
        }
    }

    @Test
    fun `setTheme calls repository`() = runTest {
        coEvery { preferencesRepository.setTheme(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.SetTheme("dark"))
        coVerify { preferencesRepository.setTheme("dark") }
    }

    @Test
    fun `setHapticsEnabled calls repository`() = runTest {
        coEvery { preferencesRepository.setHapticsEnabled(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.SetHapticsEnabled(false))
        coVerify { preferencesRepository.setHapticsEnabled(false) }
    }

    @Test
    fun `setShowNotifications calls repository`() = runTest {
        coEvery { preferencesRepository.setShowNotifications(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.SetShowNotifications(false))
        coVerify { preferencesRepository.setShowNotifications(false) }
    }
}
