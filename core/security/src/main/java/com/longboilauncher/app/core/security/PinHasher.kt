package com.longboilauncher.app.core.security

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinHasher
    @Inject
    constructor() {
        private val secureRandom = SecureRandom()

        fun hash(pin: String): String {
            val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
            val iterations = ITERATIONS
            val (algorithmId, hash) = pbkdf2(pin, salt, iterations)
            return buildString {
                append(FORMAT_PREFIX)
                append('$')
                append(algorithmId)
                append('$')
                append(iterations)
                append('$')
                append(Base64.getEncoder().encodeToString(salt))
                append('$')
                append(Base64.getEncoder().encodeToString(hash))
            }
        }

        fun isHashed(stored: String): Boolean = stored.startsWith("$FORMAT_PREFIX$")

        fun verify(
            pin: String,
            stored: String,
        ): Boolean {
            if (!isHashed(stored)) return false

            val parts = stored.split('$')
            if (parts.size != 5) return false
            if (parts[0] != FORMAT_PREFIX) return false

            val algorithmId = parts[1]
            val iterations = parts[2].toIntOrNull() ?: return false
            val salt = runCatching { Base64.getDecoder().decode(parts[3]) }.getOrNull() ?: return false
            val expected = runCatching { Base64.getDecoder().decode(parts[4]) }.getOrNull() ?: return false

            val actual = pbkdf2(pin, salt, iterations, algorithmId) ?: return false
            return MessageDigest.isEqual(actual, expected)
        }

        private fun pbkdf2(
            pin: String,
            salt: ByteArray,
            iterations: Int,
        ): Pair<String, ByteArray> {
            val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
            return try {
                ALGORITHM_ID_SHA256 to SecretKeyFactory.getInstance(ALGORITHM_SHA256).generateSecret(spec).encoded
            } catch (_: NoSuchAlgorithmException) {
                ALGORITHM_ID_SHA1 to SecretKeyFactory.getInstance(ALGORITHM_SHA1).generateSecret(spec).encoded
            }
        }

        private fun pbkdf2(
            pin: String,
            salt: ByteArray,
            iterations: Int,
            algorithmId: String,
        ): ByteArray? {
            val algorithm =
                when (algorithmId) {
                    ALGORITHM_ID_SHA256 -> ALGORITHM_SHA256
                    ALGORITHM_ID_SHA1 -> ALGORITHM_SHA1
                    else -> return null
                }
            val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
        }

        private companion object {
            const val ALGORITHM_SHA256 = "PBKDF2WithHmacSHA256"
            const val ALGORITHM_SHA1 = "PBKDF2WithHmacSHA1"
            const val ALGORITHM_ID_SHA256 = "sha256"
            const val ALGORITHM_ID_SHA1 = "sha1"
            const val ITERATIONS = 120_000
            const val KEY_LENGTH_BITS = 256
            const val SALT_LENGTH_BYTES = 16
            const val FORMAT_PREFIX = "pbkdf2"
        }
    }
