package com.longboilauncher.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import coil.ImageLoader
import coil.ImageLoaderFactory

@HiltAndroidApp
class LauncherApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }
}
