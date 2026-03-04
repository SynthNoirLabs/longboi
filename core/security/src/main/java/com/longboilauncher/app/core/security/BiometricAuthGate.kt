package com.longboilauncher.app.core.security

import androidx.biometric.BiometricManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthGate
    @Inject
    constructor(
        private val biometricPromptManager: BiometricPromptManager,
    ) : AuthGate {
        override suspend fun requestUnlock(): Boolean {
            val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

            return biometricPromptManager.showPrompt(
                title = "Unlock Private Space",
                authenticators = authenticators,
            )
        }
    }
