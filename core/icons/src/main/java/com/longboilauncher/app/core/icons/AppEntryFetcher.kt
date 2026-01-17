package com.longboilauncher.app.core.icons

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.settings.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppEntryFetcher(
    private val appEntry: AppEntry,
    private val context: Context,
    private val options: Options,
    private val preferencesRepository: PreferencesRepository
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        // 1. Per-app override (Check repository)
        val overrides = preferencesRepository.perAppIconOverrides.first()
        val overrideKey = "${appEntry.packageName}_${appEntry.userIdentifier}"
        val overridePath = overrides[overrideKey]
        if (overridePath != null) {
            // Logic to load from path (e.g. URI or file)
            // For now, continue to system icons if override logic not fully implemented
        }

        // 2. Icon pack override
        val iconPack = preferencesRepository.iconPackPackageName.first()
        if (iconPack.isNotBlank()) {
            // Logic to load from icon pack
        }

        // 3. Themed/Monochrome support (Android 13+)
        var drawable: Drawable? = try {
            val activities = launcherApps.getActivityList(appEntry.packageName, appEntry.user)
            val activity = activities.find { it.name == appEntry.className }
            activity?.getBadgedIcon(0)
        } catch (e: Exception) {
            null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && drawable is AdaptiveIconDrawable) {
            val monochrome = drawable.monochrome
            if (monochrome != null) {
                val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorPrimary))
                val primaryColor = typedArray.getColor(0, 0)
                typedArray.recycle()

                if (primaryColor != 0) {
                    monochrome.setTint(primaryColor)
                }
            }
        }

        // 4. Adaptive icon normal (already handled by getBadgedIcon usually)

        // 5. Fallback: mask + background unify (if still null or generic)
        if (drawable == null) {
            drawable = try {
                context.packageManager.getApplicationIcon(appEntry.packageName)
            } catch (e: Exception) {
                null
            }
        }

        return drawable?.let {
            DrawableResult(
                drawable = it,
                isSampled = false,
                dataSource = DataSource.DISK
            )
        }
    }

    class Factory @Inject constructor(
        @ApplicationContext private val context: Context,
        private val preferencesRepository: PreferencesRepository
    ) : Fetcher.Factory<AppEntry> {
        override fun create(data: AppEntry, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppEntryFetcher(data, context, options, preferencesRepository)
        }
    }
}
