package com.longboilauncher.app.core.security

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BiometricAuthGateTest {
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var biometricAuthGate: BiometricAuthGate

    @Before
    fun setup() {
        biometricPromptManager = mockk()
        biometricAuthGate = BiometricAuthGate(biometricPromptManager)
    }

    @Test
    fun `requestUnlock shows biometric prompt with correct parameters`() =
        runTest {
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns true

            biometricAuthGate.requestUnlock()

            coVerify {
                biometricPromptManager.showPrompt(
                    title = "Unlock Private Space",
                    subtitle = null,
                    authenticators = any(),
                )
            }
        }

    @Test
    fun `requestUnlock returns true when authentication succeeds`() =
        runTest {
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns true

            val result = biometricAuthGate.requestUnlock()

            assertThat(result).isTrue()
        }

    @Test
    fun `requestUnlock returns false when authentication fails`() =
        runTest {
            coEvery { biometricPromptManager.showPrompt(any(), any(), any(), any()) } returns false

            val result = biometricAuthGate.requestUnlock()

            assertThat(result).isFalse()
        }
}
