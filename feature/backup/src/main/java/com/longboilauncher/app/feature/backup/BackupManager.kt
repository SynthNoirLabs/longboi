package com.longboilauncher.feature.backup

import android.content.Context
import android.net.Uri
import com.longboilauncher.app.core.settings.PreferencesRepository
import com.longboilauncher.core.datastore_proto.PrivateSpaceAuthMethod
import com.longboilauncher.core.datastore_proto.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LauncherBackup(
    val version: Int = 1,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val settingsData: String, // Base64 encoded protobuf
)

@Singleton
class BackupManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val preferencesRepository: PreferencesRepository,
    ) {
        private val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

        /**
         * Create a backup of all launcher settings.
         * Returns the backup as a JSON string.
         */
        suspend fun createBackup(): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    val settingsBytes = preferencesRepository.getSerializedSettings()
                    val sanitized = sanitizeSettingsForBackup(settingsBytes)
                    val settingsBase64 =
                        android.util.Base64.encodeToString(
                            sanitized,
                            android.util.Base64.NO_WRAP,
                        )

                    val backup =
                        LauncherBackup(
                            version = BACKUP_VERSION,
                            settingsData = settingsBase64,
                        )

                    Result.success(json.encodeToString(backup))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Export backup to a file URI.
         */
        suspend fun exportToUri(uri: Uri): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val backupJson = createBackup().getOrThrow()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(backupJson.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("Could not open output stream")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Restore settings from a backup JSON string.
         */
        suspend fun restoreBackup(backupJson: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    if (backupJson.length > MAX_BACKUP_JSON_CHARS) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Backup file is too large"),
                        )
                    }

                    val backup = json.decodeFromString<LauncherBackup>(backupJson)

                    if (backup.version > BACKUP_VERSION) {
                        return@withContext Result.failure(
                            IllegalArgumentException(
                                "Backup version ${backup.version} is newer than supported version $BACKUP_VERSION",
                            ),
                        )
                    }

                    if (backup.settingsData.isBlank()) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Backup settingsData is empty"),
                        )
                    }

                    val settingsBytes =
                        android.util.Base64.decode(
                            backup.settingsData,
                            android.util.Base64.DEFAULT,
                        )

                    if (settingsBytes.size > MAX_SETTINGS_BYTES) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Backup settings payload is too large"),
                        )
                    }

                    val sanitized = sanitizeSettingsForRestore(settingsBytes)

                    preferencesRepository.restoreSettings(sanitized)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Import and restore backup from a file URI.
         */
        suspend fun importFromUri(uri: Uri): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val backupJson =
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.bufferedReader().use { reader ->
                                val buffer = CharArray(8 * 1024)
                                val builder = StringBuilder()
                                while (true) {
                                    val read = reader.read(buffer)
                                    if (read < 0) break
                                    builder.append(buffer, 0, read)
                                    if (builder.length > MAX_BACKUP_JSON_CHARS) {
                                        throw IllegalArgumentException("Backup file is too large")
                                    }
                                }
                                builder.toString()
                            }
                        } ?: throw IllegalStateException("Could not open input stream")

                    restoreBackup(backupJson)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        private fun sanitizeSettingsForBackup(settingsBytes: ByteArray): ByteArray {
            val settings = UserSettings.parseFrom(settingsBytes)
            val sanitized = sanitizeUserSettings(settings)
            return sanitized.toByteArray()
        }

        private fun sanitizeSettingsForRestore(settingsBytes: ByteArray): ByteArray {
            val settings = UserSettings.parseFrom(settingsBytes)
            val sanitized = sanitizeUserSettings(settings)
            return sanitized.toByteArray()
        }

        private fun sanitizeUserSettings(settings: UserSettings): UserSettings =
            settings
                .toBuilder()
                .setPrivateSpaceUnlocked(false)
                .setPrivateSpaceAuthMethod(PrivateSpaceAuthMethod.NONE)
                .clearPrivateSpacePinHash()
                .build()

        companion object {
            const val BACKUP_VERSION = 2
            const val BACKUP_FILE_NAME = "longboi_backup.json"
            const val BACKUP_MIME_TYPE = "application/json"

            private const val MAX_BACKUP_JSON_CHARS: Int = 2_000_000
            private const val MAX_SETTINGS_BYTES: Int = 5_000_000
        }
    }
