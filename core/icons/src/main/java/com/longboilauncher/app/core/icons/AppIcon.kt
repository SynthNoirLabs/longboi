package com.longboilauncher.app.core.icons

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.longboilauncher.app.core.model.AppEntry
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.BitmapPainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build

@Composable
fun AppIcon(
    appEntry: AppEntry,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconThemingEnabled: Boolean = true // This should come from settings
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(appEntry) // We need a custom fetcher for AppEntry
            .crossfade(true)
            .build(),
        contentDescription = appEntry.label,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentScale = ContentScale.Fit,
        // Fallback/Loading states handled via ImageRequest or custom implementation
    )
}

private fun getAppIcon(context: android.content.Context, appEntry: AppEntry): Drawable? {
    return try {
        val launcherApps = context.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE)
            as android.content.pm.LauncherApps
        val activities = launcherApps.getActivityList(appEntry.packageName, appEntry.user)
        val activity = activities.find { it.name == appEntry.className }
        activity?.getBadgedIcon(0)
    } catch (e: Exception) {
        try {
            context.packageManager.getApplicationIcon(appEntry.packageName)
        } catch (e2: Exception) {
            null
        }
    }
}
