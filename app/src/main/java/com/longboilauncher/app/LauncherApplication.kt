package com.longboilauncher.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.longboilauncher.app.core.icons.AppEntryFetcher
import com.longboilauncher.app.core.model.AppEntry
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class LauncherApplication :
    Application(),
    ImageLoaderFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageLoaderEntryPoint {
        fun appEntryFetcherFactory(): AppEntryFetcher.Factory
    }

    override fun newImageLoader(): ImageLoader {
        val entryPoint = EntryPoints.get(this, ImageLoaderEntryPoint::class.java)
        return ImageLoader.Builder(this)
            .components {
                add(entryPoint.appEntryFetcherFactory())
            }
            .crossfade(true)
            .build()
    }
}
