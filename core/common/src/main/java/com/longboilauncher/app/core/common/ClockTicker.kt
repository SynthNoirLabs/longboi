package com.longboilauncher.app.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ClockTicker {
    fun tick(period: Duration = 1.seconds): Flow<Unit>
}

@Singleton
class DefaultClockTicker
    @Inject
    constructor() : ClockTicker {
        override fun tick(period: Duration): Flow<Unit> =
            flow {
                while (true) {
                    emit(Unit)
                    delay(period)
                }
            }
    }
