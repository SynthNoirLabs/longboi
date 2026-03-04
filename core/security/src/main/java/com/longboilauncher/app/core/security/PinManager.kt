package com.longboilauncher.app.core.security

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager
    @Inject
    constructor() {
        private val _isShowingPinEntry = MutableStateFlow(false)
        val isShowingPinEntry: StateFlow<Boolean> = _isShowingPinEntry.asStateFlow()

        private var pinResult: CompletableDeferred<String?>? = null

        suspend fun awaitPinEntry(): String? {
            pinResult = CompletableDeferred()
            _isShowingPinEntry.value = true
            return try {
                pinResult?.await()
            } finally {
                _isShowingPinEntry.value = false
                pinResult = null
            }
        }

        fun onPinEntered(pin: String) {
            pinResult?.complete(pin)
        }

        fun onCancelled() {
            pinResult?.complete(null)
        }
    }
