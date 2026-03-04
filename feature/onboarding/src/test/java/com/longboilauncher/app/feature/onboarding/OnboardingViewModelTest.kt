package com.longboilauncher.app.feature.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.settings.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = mockk(relaxed = true)
        viewModel = OnboardingViewModel(preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct values`() {
        val state = viewModel.uiState.value
        assertThat(state.currentPage).isEqualTo(0)
        assertThat(state.totalPages).isEqualTo(4)
        assertThat(state.isLastPage).isFalse()
    }

    @Test
    fun `NextPage increments currentPage`() {
        viewModel.onEvent(OnboardingEvent.NextPage)
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(1)
        assertThat(viewModel.uiState.value.isLastPage).isFalse()
    }

    @Test
    fun `NextPage sets isLastPage when reaching last page`() {
        repeat(3) { viewModel.onEvent(OnboardingEvent.NextPage) }
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(3)
        assertThat(viewModel.uiState.value.isLastPage).isTrue()
    }

    @Test
    fun `NextPage does not exceed totalPages`() {
        repeat(10) { viewModel.onEvent(OnboardingEvent.NextPage) }
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(3)
    }

    @Test
    fun `PreviousPage decrements currentPage`() {
        viewModel.onEvent(OnboardingEvent.NextPage)
        viewModel.onEvent(OnboardingEvent.NextPage)
        viewModel.onEvent(OnboardingEvent.PreviousPage)
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(1)
    }

    @Test
    fun `PreviousPage does not go below zero`() {
        viewModel.onEvent(OnboardingEvent.PreviousPage)
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(0)
    }

    @Test
    fun `GoToPage sets correct page`() {
        viewModel.onEvent(OnboardingEvent.GoToPage(2))
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(2)
    }

    @Test
    fun `GoToPage coerces to valid range`() {
        viewModel.onEvent(OnboardingEvent.GoToPage(10))
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(3)

        viewModel.onEvent(OnboardingEvent.GoToPage(-5))
        assertThat(viewModel.uiState.value.currentPage).isEqualTo(0)
    }

    @Test
    fun `CompleteOnboarding saves preference and emits effect`() =
        runTest(testDispatcher) {
            coEvery { preferencesRepository.setOnboardingCompleted(true) } returns Unit

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.CompleteOnboarding)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(OnboardingEffect.NavigateToHome)
            }

            coVerify { preferencesRepository.setOnboardingCompleted(true) }
        }

    @Test
    fun `SkipOnboarding saves preference and emits effect`() =
        runTest(testDispatcher) {
            coEvery { preferencesRepository.setOnboardingCompleted(true) } returns Unit

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.SkipOnboarding)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(OnboardingEffect.NavigateToHome)
            }

            coVerify { preferencesRepository.setOnboardingCompleted(true) }
        }

    @Test
    fun `RequestDefaultLauncher emits effect`() =
        runTest(testDispatcher) {
            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.RequestDefaultLauncher)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(OnboardingEffect.RequestDefaultLauncher)
            }
        }
}
