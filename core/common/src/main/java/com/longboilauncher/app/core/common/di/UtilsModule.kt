package com.longboilauncher.app.core.common.di

import com.longboilauncher.app.core.common.ClockTicker
import com.longboilauncher.app.core.common.DefaultClockTicker
import com.longboilauncher.app.core.common.DefaultNowProvider
import com.longboilauncher.app.core.common.NowProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilsModule {
    @Binds
    @Singleton
    abstract fun bindNowProvider(impl: DefaultNowProvider): NowProvider

    @Binds
    @Singleton
    abstract fun bindClockTicker(impl: DefaultClockTicker): ClockTicker
}
