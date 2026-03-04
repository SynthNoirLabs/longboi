package com.longboilauncher.app.core.common.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsManager(debugAnalyticsManager: DebugAnalyticsManager): AnalyticsManager
}
