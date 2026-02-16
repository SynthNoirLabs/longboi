package com.longboilauncher.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.longboilauncher.app.core.icons.AppEntryFetcher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LauncherApplication :
    Application(),
    ImageLoaderFactory {
    @Inject
    lateinit var appEntryFetcherFactory: AppEntryFetcher.Factory

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .components {
                add(appEntryFetcherFactory)
            }
            .crossfade(true)
            .build()
}
