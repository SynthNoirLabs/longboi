package com.longboilauncher.app.core.settings

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.UserSettings
import com.longboilauncher.app.core.datastore.serializer.UserSettingsSerializer
import com.longboilauncher.app.core.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class PreferencesRepositoryTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataStore: DataStore<UserSettings>
    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default theme is system`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            assertThat(repository.theme.first()).isEqualTo(ThemeMode.SYSTEM)
        }

    @Test
    fun `setTheme updates theme`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            repository.setTheme(ThemeMode.DARK)
            advanceUntilIdle()

            assertThat(repository.theme.first()).isEqualTo(ThemeMode.DARK)
        }

    @Test
    fun `haptics are disabled by default in proto3`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            // Note: proto3 defaults booleans to false
            assertThat(repository.hapticsEnabled.first()).isFalse()
        }

    @Test
    fun `setHapticsEnabled updates value`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            repository.setHapticsEnabled(true)
            advanceUntilIdle()

            assertThat(repository.hapticsEnabled.first()).isTrue()
        }

    @Test
    fun `default swipe up gesture is all_apps`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            assertThat(repository.gestureSwipeUp.first()).isEqualTo("all_apps")
        }

    @Test
    fun `reduce motion is disabled by default`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            assertThat(repository.reduceMotion.first()).isFalse()
        }

    @Test
    fun `setReduceMotion updates value`() =
        runTest(testDispatcher) {
            dataStore =
                DataStoreFactory.create(
                    serializer = UserSettingsSerializer,
                    scope = backgroundScope,
                    produceFile = { tmpFolder.newFile("test_${System.nanoTime()}.preferences_pb") },
                )
            repository = PreferencesRepository(dataStore)
            advanceUntilIdle()

            repository.setReduceMotion(true)
            advanceUntilIdle()

            assertThat(repository.reduceMotion.first()).isTrue()
        }
}
