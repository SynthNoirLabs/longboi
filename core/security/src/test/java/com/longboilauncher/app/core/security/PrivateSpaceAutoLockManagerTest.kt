package com.longboilauncher.app.core.security

import android.content.Context
import com.longboilauncher.app.PrivateSpaceAutoLockPolicy
import com.longboilauncher.app.core.settings.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrivateSpaceAutoLockManagerTest {
    private lateinit var context: Context
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var autoLockManager: PrivateSpaceAutoLockManager
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        testScope = TestScope(UnconfinedTestDispatcher())
        autoLockManager = PrivateSpaceAutoLockManager(context, preferencesRepository, testScope)
    }

    @Test
    fun `startMonitoring registers screen off receiver`() {
        autoLockManager.startMonitoring()

        verify { context.registerReceiver(any(), any()) }
    }

    @Test
    fun `stopMonitoring unregisters screen off receiver`() {
        autoLockManager.startMonitoring()

        autoLockManager.stopMonitoring()

        verify { context.unregisterReceiver(any()) }
    }

    @Test
    fun `stopMonitoring handles unregistered receiver gracefully`() {
        every { context.unregisterReceiver(any()) } throws IllegalArgumentException()

        // Should not throw
        autoLockManager.stopMonitoring()
    }

    @Test
    fun `onAppPaused locks when policy is IMMEDIATELY`() =
        runTest {
            coEvery { preferencesRepository.privateSpaceAutoLockPolicy } returns
                flowOf(PrivateSpaceAutoLockPolicy.IMMEDIATELY)
            coEvery { preferencesRepository.setPrivateSpaceUnlocked(any()) } returns Unit

            autoLockManager.onAppPaused()

            coVerify { preferencesRepository.setPrivateSpaceUnlocked(false) }
        }

    @Test
    fun `onAppPaused does not lock when policy is ON_SCREEN_OFF`() =
        runTest {
            coEvery { preferencesRepository.privateSpaceAutoLockPolicy } returns
                flowOf(PrivateSpaceAutoLockPolicy.ON_SCREEN_OFF)

            autoLockManager.onAppPaused()

            coVerify(exactly = 0) { preferencesRepository.setPrivateSpaceUnlocked(false) }
        }

    @Test
    fun `onAppPaused does not lock when policy is AFTER_TIMEOUT`() =
        runTest {
            coEvery { preferencesRepository.privateSpaceAutoLockPolicy } returns
                flowOf(PrivateSpaceAutoLockPolicy.AFTER_TIMEOUT)

            autoLockManager.onAppPaused()

            coVerify(exactly = 0) { preferencesRepository.setPrivateSpaceUnlocked(false) }
        }
}
