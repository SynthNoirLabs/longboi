package com.longboilauncher.app.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.settings.PreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test.preferences_pb") }
        )
        repository = PreferencesRepository(dataStore)
    }

    @Test
    fun `default theme is system`() = runTest {
        assertThat(repository.theme.first()).isEqualTo("system")
    }

    @Test
    fun `setTheme updates theme`() = runTest {
        repository.setTheme("dark")
        assertThat(repository.theme.first()).isEqualTo("dark")
    }

    @Test
    fun `haptics are enabled by default`() = runTest {
        assertThat(repository.hapticsEnabled.first()).isTrue()
    }

    @Test
    fun `setHapticsEnabled updates value`() = runTest {
        repository.setHapticsEnabled(false)
        assertThat(repository.hapticsEnabled.first()).isFalse()
    }

    @Test
    fun `default swipe up gesture is all_apps`() = runTest {
        assertThat(repository.gestureSwipeUp.first()).isEqualTo("all_apps")
    }

    @Test
    fun `reduce motion is disabled by default`() = runTest {
        assertThat(repository.reduceMotion.first()).isFalse()
    }

    @Test
    fun `setReduceMotion updates value`() = runTest {
        repository.setReduceMotion(true)
        assertThat(repository.reduceMotion.first()).isTrue()
    }
}
