package com.longboilauncher.app.core.security

import com.longboilauncher.app.core.settings.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PinAuthGate
    @Inject
    constructor(
        private val pinManager: PinManager,
        private val preferencesRepository: PreferencesRepository,
        private val pinHasher: PinHasher,
    ) : AuthGate {
        override suspend fun requestUnlock(): Boolean {
            val enteredPin = pinManager.awaitPinEntry() ?: return false
            val saved = preferencesRepository.privateSpacePinHash.first()
            if (saved.isBlank()) return false

            if (pinHasher.isHashed(saved)) {
                return pinHasher.verify(enteredPin, saved)
            }

            val legacyMatches = enteredPin == saved
            if (legacyMatches) {
                preferencesRepository.setPrivateSpacePinHash(pinHasher.hash(enteredPin))
            }
            return legacyMatches
        }
    }
