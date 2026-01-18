package com.longboilauncher.app.core.datastore

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.UserSettings
import com.longboilauncher.app.core.datastore.serializer.UserSettingsSerializer
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
class FavoritesRepositoryTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStore: DataStore<UserSettings>
    private lateinit var repository: FavoritesRepository

    private val testApp =
        AppEntry(
            packageName = "com.test.app",
            className = "MainActivity",
            label = "Test App",
            userIdentifier = 0,
            profile = ProfileType.PERSONAL,
        )

    @Before
    fun setup() {
        dataStore =
            DataStoreFactory.create(
                serializer = UserSettingsSerializer,
                produceFile = { tmpFolder.newFile("user_settings.pb") },
                scope = testScope,
            )
        repository = FavoritesRepository(dataStore)
    }

    @Test
    fun `favorites flow is initially empty`() =
        runTest {
            repository.favorites.test {
                assertThat(awaitItem()).isEmpty()
            }
        }

    @Test
    fun `addFavorite adds app to list`() =
        runTest {
            repository.addFavorite(testApp)

            val favorites = repository.favorites.first()
            assertThat(favorites).hasSize(1)
            assertThat(favorites[0].appEntry.packageName).isEqualTo(testApp.packageName)
        }

    @Test
    fun `removeFavorite removes app by id`() =
        runTest {
            repository.addFavorite(testApp)
            val id = "${testApp.packageName}_0"

            repository.removeFavorite(id)

            val favorites = repository.favorites.first()
            assertThat(favorites).isEmpty()
        }

    @Test
    fun `reorderFavorites updates positions`() =
        runTest {
            val app2 = testApp.copy(packageName = "com.test.app2")
            repository.addFavorite(testApp) // id: com.test.app_0
            repository.addFavorite(app2) // id: com.test.app2_0

            val id1 = "${testApp.packageName}_0"
            val id2 = "${app2.packageName}_0"

            repository.reorderFavorites(listOf(id2, id1))

            val favorites = repository.favorites.first()
            assertThat(favorites).hasSize(2)
            assertThat(favorites.find { it.id == id2 }?.position).isEqualTo(0)
            assertThat(favorites.find { it.id == id1 }?.position).isEqualTo(1)
        }

    @Test
    fun `hideApp adds package to hidden list`() =
        runTest {
            repository.hideApp("com.hidden.app")

            val hidden = repository.hiddenApps.first()
            assertThat(hidden).contains("com.hidden.app")
        }

    @Test
    fun `unhideApp removes package from hidden list`() =
        runTest {
            repository.hideApp("com.hidden.app")
            repository.unhideApp("com.hidden.app")

            val hidden = repository.hiddenApps.first()
            assertThat(hidden).isEmpty()
        }
}
