package com.longboilauncher.app.core.common

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface NowProvider {
    fun now(): Instant
    fun clock(): Clock
}

@Singleton
class DefaultNowProvider @Inject constructor() : NowProvider {
    override fun now(): Instant = Instant.now()
    override fun clock(): Clock = Clock.systemDefaultZone()
}
