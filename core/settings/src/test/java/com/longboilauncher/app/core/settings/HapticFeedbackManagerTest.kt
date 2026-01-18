package com.longboilauncher.app.core.settings

import android.os.Build
import android.view.View
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
class HapticFeedbackManagerTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var hapticFeedbackManager: HapticFeedbackManager
    private lateinit var mockView: View

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockView = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        testScope = CoroutineScope(SupervisorJob() + testDispatcher)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `tick_triggersHaptic_whenEnabled_andReduceMotionOff`() =
        runTest {
            every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
            every { preferencesRepository.reduceMotion } returns MutableStateFlow(false)
            hapticFeedbackManager = HapticFeedbackManager(preferencesRepository, testScope)
            advanceUntilIdle()

            hapticFeedbackManager.tick(mockView)

            verify { mockView.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP) }
        }

    @Test
    fun `tick_noHaptic_whenDisabled`() =
        runTest {
            every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(false)
            every { preferencesRepository.reduceMotion } returns MutableStateFlow(false)
            hapticFeedbackManager = HapticFeedbackManager(preferencesRepository, testScope)
            advanceUntilIdle()

            hapticFeedbackManager.tick(mockView)

            verify(exactly = 0) { mockView.performHapticFeedback(any()) }
        }

    @Test
    fun `tick_noHaptic_whenReduceMotionOn`() =
        runTest {
            every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
            every { preferencesRepository.reduceMotion } returns MutableStateFlow(true)
            hapticFeedbackManager = HapticFeedbackManager(preferencesRepository, testScope)
            advanceUntilIdle()

            hapticFeedbackManager.tick(mockView)

            verify(exactly = 0) { mockView.performHapticFeedback(any()) }
        }

    @Test
    fun `confirm_triggersHaptic_whenEnabled_andReduceMotionOff`() =
        runTest {
            every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
            every { preferencesRepository.reduceMotion } returns MutableStateFlow(false)
            hapticFeedbackManager = HapticFeedbackManager(preferencesRepository, testScope)
            advanceUntilIdle()

            hapticFeedbackManager.confirm(mockView)

            verify { mockView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY) }
        }

    @Test
    fun `heavy_triggersHaptic_whenEnabled_andReduceMotionOff`() =
        runTest {
            every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true)
            every { preferencesRepository.reduceMotion } returns MutableStateFlow(false)
            hapticFeedbackManager = HapticFeedbackManager(preferencesRepository, testScope)
            advanceUntilIdle()

            hapticFeedbackManager.heavy(mockView)

            verify { mockView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS) }
        }
}
