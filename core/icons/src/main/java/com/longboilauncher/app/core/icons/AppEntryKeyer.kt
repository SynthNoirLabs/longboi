package com.longboilauncher.app.core.icons

import coil.key.Keyer
import coil.request.Options
import com.longboilauncher.app.core.model.AppEntry

class AppEntryKeyer : Keyer<AppEntry> {
    override fun key(
        data: AppEntry,
        options: Options,
    ): String = "app:${data.packageName}/${data.className}:${data.userSerialNumber}:${data.lastUpdateTime}"
}
