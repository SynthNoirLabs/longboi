package com.longboilauncher.app.core.security

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class BiometricPromptManager
    @Inject
    constructor() {
        private val _promptRequests = MutableSharedFlow<BiometricPromptRequest>(extraBufferCapacity = 1)
        val promptRequests = _promptRequests.asSharedFlow()

        suspend fun showPrompt(
            title: String,
            subtitle: String? = null,
            description: String? = null,
            authenticators: Int,
        ): Boolean {
            val deferred = CompletableDeferred<Boolean>()
            val request =
                BiometricPromptRequest(
                    title = title,
                    subtitle = subtitle,
                    description = description,
                    authenticators = authenticators,
                    result = deferred,
                )
            _promptRequests.emit(request)
            return deferred.await()
        }
    }

data class BiometricPromptRequest(
    val title: String,
    val subtitle: String?,
    val description: String?,
    val authenticators: Int,
    val result: CompletableDeferred<Boolean>,
)
