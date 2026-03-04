package com.longboilauncher.app.core.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AppLockManagerTest {
    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var appLockManager: AppLockManager

    @Before
    fun setup() {
        dataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = { tmpFolder.newFile("test_app_lock.preferences_pb") },
            )
        biometricPromptManager = mockk(relaxed = true)
        appLockManager = AppLockManager(biometricPromptManager, dataStore)
    }

    @Test
    fun `initial state has no locked apps`() =
        runTest(testDispatcher) {
            appLockManager.lockedApps.test {
                assertThat(awaitItem()).isEmpty()
            }
        }

    @Test
    fun `lockApp adds app to locked set`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"

            appLockManager.lockApp(packageName)

            appLockManager.lockedApps.test {
                assertThat(awaitItem()).contains(packageName)
            }
        }

    @Test
    fun `unlockApp removes app from locked set`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"
            appLockManager.lockApp(packageName)

            appLockManager.unlockApp(packageName)

            appLockManager.lockedApps.test {
                assertThat(awaitItem()).doesNotContain(packageName)
            }
        }

    @Test
    fun `isAppLocked returns true for locked app`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"
            appLockManager.lockApp(packageName)

            val isLocked = appLockManager.isAppLocked(packageName)

            assertThat(isLocked).isTrue()
        }

    @Test
    fun `isAppLocked returns false for unlocked app`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"

            val isLocked = appLockManager.isAppLocked(packageName)

            assertThat(isLocked).isFalse()
        }

    @Test
    fun `authenticateForApp returns true for unlocked app`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"

            val result = appLockManager.authenticateForApp(packageName)

            assertThat(result).isTrue()
        }

    @Test
    fun `authenticateForApp shows prompt for locked app`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"
            appLockManager.lockApp(packageName)
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns true

            val result = appLockManager.authenticateForApp(packageName)

            assertThat(result).isTrue()
        }

    @Test
    fun `authenticateForApp temporarily unlocks app on success`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"
            appLockManager.lockApp(packageName)
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns true

            appLockManager.authenticateForApp(packageName)

            // App should be temporarily unlocked
            val isLocked = appLockManager.isAppLocked(packageName)
            assertThat(isLocked).isFalse()
        }

    @Test
    fun `relockAllApps re-locks temporarily unlocked apps`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"
            appLockManager.lockApp(packageName)
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns true
            appLockManager.authenticateForApp(packageName)

            appLockManager.relockAllApps()

            val isLocked = appLockManager.isAppLocked(packageName)
            assertThat(isLocked).isTrue()
        }

    @Test
    fun `hasLockedApps returns true when apps are locked`() =
        runTest(testDispatcher) {
            appLockManager.lockApp("com.example.app")

            val hasLocked = appLockManager.hasLockedApps()

            assertThat(hasLocked).isTrue()
        }

    @Test
    fun `hasLockedApps returns false when no apps are locked`() =
        runTest(testDispatcher) {
            val hasLocked = appLockManager.hasLockedApps()

            assertThat(hasLocked).isFalse()
        }

    @Test
    fun `lockApp removes from recently unlocked cache`() =
        runTest(testDispatcher) {
            val packageName = "com.example.app"
            appLockManager.lockApp(packageName)
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns true
            appLockManager.authenticateForApp(packageName)

            // Re-lock the app
            appLockManager.lockApp(packageName)

            // Should be locked again
            val isLocked = appLockManager.isAppLocked(packageName)
            assertThat(isLocked).isTrue()
        }

    @Test
    fun `multiple apps can be locked independently`() =
        runTest(testDispatcher) {
            val app1 = "com.example.app1"
            val app2 = "com.example.app2"

            appLockManager.lockApp(app1)
            appLockManager.lockApp(app2)

            appLockManager.lockedApps.test {
                val locked = awaitItem()
                assertThat(locked).contains(app1)
                assertThat(locked).contains(app2)
            }
        }
}
