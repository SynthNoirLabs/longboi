package com.longboilauncher.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import coil.ImageLoader
import coil.ImageLoaderFactory
import com.longboilauncher.app.core.designsystem.components.AppEntryFetcher
import javax.inject.Inject

@HiltAndroidApp
class LauncherApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var appEntryFetcherFactory: AppEntryFetcher.Factory

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(appEntryFetcherFactory)
            }
            .crossfade(true)
            .build()
    }
}
