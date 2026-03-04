package com.longboilauncher.app.core.security

import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.settings.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinAuthGateTest {
    private val pinManager = mockk<PinManager>()
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val pinHasher = PinHasher()

    @Test
    fun `requestUnlock returns true for valid hashed pin`() =
        runTest {
            val stored = pinHasher.hash("1234")
            every { preferencesRepository.privateSpacePinHash } returns MutableStateFlow(stored)
            coEvery { pinManager.awaitPinEntry() } returns "1234"

            val gate = PinAuthGate(pinManager, preferencesRepository, pinHasher)

            assertThat(gate.requestUnlock()).isTrue()
        }

    @Test
    fun `requestUnlock returns false for invalid hashed pin`() =
        runTest {
            val stored = pinHasher.hash("1234")
            every { preferencesRepository.privateSpacePinHash } returns MutableStateFlow(stored)
            coEvery { pinManager.awaitPinEntry() } returns "0000"

            val gate = PinAuthGate(pinManager, preferencesRepository, pinHasher)

            assertThat(gate.requestUnlock()).isFalse()
        }

    @Test
    fun `requestUnlock migrates legacy plaintext pin on success`() =
        runTest {
            every { preferencesRepository.privateSpacePinHash } returns MutableStateFlow("1234")
            coEvery { pinManager.awaitPinEntry() } returns "1234"
            coEvery { preferencesRepository.setPrivateSpacePinHash(any()) } returns Unit

            val gate = PinAuthGate(pinManager, preferencesRepository, pinHasher)

            assertThat(gate.requestUnlock()).isTrue()

            coVerify { preferencesRepository.setPrivateSpacePinHash(match { it.startsWith("pbkdf2$") }) }
        }

    @Test
    fun `requestUnlock does not migrate legacy plaintext pin on failure`() =
        runTest {
            every { preferencesRepository.privateSpacePinHash } returns MutableStateFlow("1234")
            coEvery { pinManager.awaitPinEntry() } returns "0000"

            val gate = PinAuthGate(pinManager, preferencesRepository, pinHasher)

            assertThat(gate.requestUnlock()).isFalse()

            coVerify(exactly = 0) { preferencesRepository.setPrivateSpacePinHash(any()) }
        }
}
