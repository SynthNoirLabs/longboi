package com.longboilauncher.app.core.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PinHasherTest {
    private val pinHasher = PinHasher()

    @Test
    fun `hash produces pbkdf2 format`() {
        val hashed = pinHasher.hash("1234")

        assertThat(hashed).startsWith("pbkdf2$")
        val parts = hashed.split('$')
        assertThat(parts).hasSize(5)
        assertThat(parts[0]).isEqualTo("pbkdf2")
        assertThat(parts[1]).isIn(listOf("sha256", "sha1"))
        assertThat(parts[2].toIntOrNull()).isNotNull()
        assertThat(parts[3]).isNotEmpty() // salt
        assertThat(parts[4]).isNotEmpty() // hash
    }

    @Test
    fun `hash produces unique hashes for same pin`() {
        val hash1 = pinHasher.hash("1234")
        val hash2 = pinHasher.hash("1234")

        // Different salts should produce different hashes
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `verify returns true for correct pin`() {
        val pin = "1234"
        val hashed = pinHasher.hash(pin)

        assertThat(pinHasher.verify(pin, hashed)).isTrue()
    }

    @Test
    fun `verify returns false for wrong pin`() {
        val hashed = pinHasher.hash("1234")

        assertThat(pinHasher.verify("0000", hashed)).isFalse()
        assertThat(pinHasher.verify("1235", hashed)).isFalse()
        assertThat(pinHasher.verify("", hashed)).isFalse()
    }

    @Test
    fun `verify returns false for unhashed input`() {
        assertThat(pinHasher.verify("1234", "1234")).isFalse()
        assertThat(pinHasher.verify("1234", "plaintext")).isFalse()
    }

    @Test
    fun `verify handles malformed hash gracefully`() {
        assertThat(pinHasher.verify("1234", "")).isFalse()
        assertThat(pinHasher.verify("1234", "pbkdf2")).isFalse()
        assertThat(pinHasher.verify("1234", "pbkdf2" + "$" + "sha256")).isFalse()
        assertThat(pinHasher.verify("1234", "pbkdf2" + "$" + "sha256" + "$" + "invalid")).isFalse()
        assertThat(
            pinHasher.verify("1234", "pbkdf2" + "$" + "sha256" + "$" + "100" + "$" + "invalid" + "$" + "base64"),
        ).isFalse()
    }

    @Test
    fun `verify handles corrupted base64 salt gracefully`() {
        val corruptedHash = "pbkdf2" + "$" + "sha256" + "$" + "120000" + "$" + "!!!invalid!!!" + "$" + "AAAA"
        assertThat(pinHasher.verify("1234", corruptedHash)).isFalse()
    }

    @Test
    fun `verify handles corrupted base64 hash gracefully`() {
        val corruptedHash = "pbkdf2" + "$" + "sha256" + "$" + "120000" + "$" + "AAAA" + "$" + "!!!invalid!!!"
        assertThat(pinHasher.verify("1234", corruptedHash)).isFalse()
    }

    @Test
    fun `verify handles unknown algorithm gracefully`() {
        val unknownAlgo = "pbkdf2" + "$" + "sha512" + "$" + "120000" + "$" + "AAAA" + "$" + "BBBB"
        assertThat(pinHasher.verify("1234", unknownAlgo)).isFalse()
    }

    @Test
    fun `isHashed correctly detects hashed values`() {
        val hashed = pinHasher.hash("1234")
        assertThat(pinHasher.isHashed(hashed)).isTrue()
    }

    @Test
    fun `isHashed returns false for plaintext`() {
        assertThat(pinHasher.isHashed("1234")).isFalse()
        assertThat(pinHasher.isHashed("plaintext")).isFalse()
        assertThat(pinHasher.isHashed("")).isFalse()
    }

    @Test
    fun `hash works with empty pin`() {
        val hashed = pinHasher.hash("")
        assertThat(hashed).startsWith("pbkdf2$")
        assertThat(pinHasher.verify("", hashed)).isTrue()
    }

    @Test
    fun `hash works with long pin`() {
        val longPin = "1234567890123456789012345678901234567890"
        val hashed = pinHasher.hash(longPin)
        assertThat(pinHasher.verify(longPin, hashed)).isTrue()
    }

    @Test
    fun `hash works with special characters`() {
        val specialPin = "!@#$%^&*()"
        val hashed = pinHasher.hash(specialPin)
        assertThat(pinHasher.verify(specialPin, hashed)).isTrue()
    }
}
