package com.longboilauncher.core.datastore.testing

import androidx.datastore.core.DataStore
import com.longboilauncher.core.datastore_proto.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake DataStore for testing UserSettings.
 * Provides in-memory storage without file I/O.
 */
class FakeUserSettingsDataStore : DataStore<UserSettings> {
    private val _data = MutableStateFlow(UserSettings.getDefaultInstance())

    override val data: Flow<UserSettings> = _data

    override suspend fun updateData(transform: suspend (t: UserSettings) -> UserSettings): UserSettings {
        val newValue = transform(_data.value)
        _data.value = newValue
        return newValue
    }
}
