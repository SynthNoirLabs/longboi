package com.longboilauncher.app.core.settings

import android.view.View
import app.cash.turbine.test
import com.longboilauncher.app.UserSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HapticFeedbackManagerTest {

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var hapticFeedbackManager: HapticFeedbackManager
    private lateinit var mockView: View

    @Before
    fun setup() {
        mockView = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        // Default flows: haptics enabled, reduce motion off
        every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(true).asStateFlow()
        every { preferencesRepository.reduceMotion } returns MutableStateFlow(false).asStateFlow()
        hapticFeedbackManager = HapticFeedbackManager(preferencesRepository)
    }

    @Test
    fun tick_triggersHaptic_whenEnabled_andReduceMotionOff() = runTest {
        hapticFeedbackManager.tick(mockView)
        verify { mockView.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP) }
    }

    @Test
    fun tick_noHaptic_whenDisabled() = runTest {
        every { preferencesRepository.hapticsEnabled } returns MutableStateFlow(false).asStateFlow()
        hapticFeedbackManager = HapticFeedbackManager(preferencesRepository)
        hapticFeedbackManager.tick(mockView)
        verify(exactly = 0) { mockView.performHapticFeedback(any()) }
    }

    @Test
    fun tick_noHaptic_whenReduceMotionOn() = runTest {
        every { preferencesRepository.reduceMotion } returns MutableStateFlow(true).asStateFlow()
        hapticFeedbackManager = HapticFeedbackManager(preferencesRepository)
        hapticFeedbackManager.tick(mockView)
        verify(exactly = 0) { mockView.performHapticFeedback(any()) }
    }

    @Test
    fun confirm_triggersHaptic_whenEnabled_andReduceMotionOff() = runTest {
        hapticFeedbackManager.confirm(mockView)
        verify { mockView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY) }
    }

    @Test
    fun heavy_triggersHaptic_whenEnabled_andReduceMotionOff() = runTest {
        hapticFeedbackManager.heavy(mockView)
        verify { mockView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS) }
    }
}
