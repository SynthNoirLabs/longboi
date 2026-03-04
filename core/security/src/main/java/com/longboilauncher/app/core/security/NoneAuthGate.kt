package com.longboilauncher.app.core.security

import javax.inject.Inject

class NoneAuthGate
    @Inject
    constructor() : AuthGate {
        override suspend fun requestUnlock(): Boolean = true
    }
