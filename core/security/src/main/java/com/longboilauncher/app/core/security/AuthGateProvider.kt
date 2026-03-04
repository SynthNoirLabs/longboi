package com.longboilauncher.app.core.security

import com.longboilauncher.app.PrivateSpaceAuthMethod
import com.longboilauncher.app.core.settings.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Provider

class AuthGateProvider
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
        private val noneAuthGate: Provider<NoneAuthGate>,
        private val biometricAuthGate: Provider<BiometricAuthGate>,
        private val pinAuthGate: Provider<PinAuthGate>,
    ) {
        suspend fun getAuthGate(): AuthGate {
            val method = preferencesRepository.privateSpaceAuthMethod.first()
            return when (method) {
                PrivateSpaceAuthMethod.NONE -> noneAuthGate.get()
                PrivateSpaceAuthMethod.BIOMETRIC -> biometricAuthGate.get()
                PrivateSpaceAuthMethod.PIN -> pinAuthGate.get()
                else -> noneAuthGate.get()
            }
        }
    }
