package com.longboilauncher.feature.backup

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.settings.PreferencesRepository
import com.longboilauncher.core.datastore_proto.PrivateSpaceAuthMethod
import com.longboilauncher.core.datastore_proto.UserSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class BackupManagerTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `createBackup sanitizes private space fields`() =
        runTest {
            val context = mockk<android.content.Context>(relaxed = true)
            val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)

            val originalSettings =
                UserSettings
                    .newBuilder()
                    .setTheme("dark")
                    .setPrivateSpaceUnlocked(true)
                    .setPrivateSpaceAuthMethod(PrivateSpaceAuthMethod.PIN)
                    .setPrivateSpacePinHash("pbkdf2\$sha256\$120000\$AA==\$BB==")
                    .build()

            coEvery { preferencesRepository.getSerializedSettings() } returns originalSettings.toByteArray()

            val manager = BackupManager(context, preferencesRepository)
            val backupJson = manager.createBackup().getOrThrow()

            val backup = json.decodeFromString<LauncherBackup>(backupJson)
            val settingsBytes = android.util.Base64.decode(backup.settingsData, android.util.Base64.DEFAULT)
            val restored = UserSettings.parseFrom(settingsBytes)

            assertThat(restored.theme).isEqualTo("dark")
            assertThat(restored.privateSpaceUnlocked).isFalse()
            assertThat(restored.privateSpaceAuthMethod).isEqualTo(PrivateSpaceAuthMethod.NONE)
            assertThat(restored.privateSpacePinHash).isEmpty()
        }

    @Test
    fun `restoreBackup sanitizes private space fields before writing`() =
        runTest {
            val context = mockk<android.content.Context>(relaxed = true)
            val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)

            val incomingSettings =
                UserSettings
                    .newBuilder()
                    .setTheme("system")
                    .setPrivateSpaceUnlocked(true)
                    .setPrivateSpaceAuthMethod(PrivateSpaceAuthMethod.PIN)
                    .setPrivateSpacePinHash("1234")
                    .build()

            val payload =
                android.util.Base64.encodeToString(
                    incomingSettings.toByteArray(),
                    android.util.Base64.NO_WRAP,
                )
            val backupJson = json.encodeToString(LauncherBackup(version = 1, settingsData = payload))

            val manager = BackupManager(context, preferencesRepository)
            val result = manager.restoreBackup(backupJson)

            assertThat(result.isSuccess).isTrue()
            coVerify {
                preferencesRepository.restoreSettings(
                    match {
                        val restored = UserSettings.parseFrom(it)
                        !restored.privateSpaceUnlocked &&
                            restored.privateSpaceAuthMethod == PrivateSpaceAuthMethod.NONE &&
                            restored.privateSpacePinHash.isEmpty()
                    },
                )
            }
        }

    @Test
    fun `restoreBackup rejects empty settingsData`() =
        runTest {
            val context = mockk<android.content.Context>(relaxed = true)
            val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)

            val backupJson = json.encodeToString(LauncherBackup(version = 1, settingsData = ""))

            val manager = BackupManager(context, preferencesRepository)
            val result = manager.restoreBackup(backupJson)

            assertThat(result.isFailure).isTrue()
        }

    @Test
    fun `restoreBackup rejects newer version`() =
        runTest {
            val context = mockk<android.content.Context>(relaxed = true)
            val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)

            val incomingSettings = UserSettings.getDefaultInstance().toByteArray()
            val payload = android.util.Base64.encodeToString(incomingSettings, android.util.Base64.NO_WRAP)
            val backupJson = json.encodeToString(LauncherBackup(version = 999, settingsData = payload))

            val manager = BackupManager(context, preferencesRepository)
            val result = manager.restoreBackup(backupJson)

            assertThat(result.isFailure).isTrue()
        }
}
