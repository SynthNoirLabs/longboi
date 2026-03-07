package com.longboilauncher.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.longboilauncher.app.core.datastore.di.DataStoreModule
import com.longboilauncher.app.core.datastore.serializer.UserSettingsSerializer
import com.longboilauncher.core.datastore_proto.UserSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.UUID
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class],
)
object TestDataStoreModule {
    @Provides
    @Singleton
    fun provideUserSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<UserSettings> =
        DataStoreFactory.create(
            serializer = UserSettingsSerializer,
            // Use a unique file for each test process/run to avoid "multiple DataStores active"
            produceFile = { context.dataStoreFile("test_user_settings_${UUID.randomUUID()}.pb") },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )

    @Provides
    @Singleton
    fun provideJson(): kotlinx.serialization.json.Json =
        kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
}
