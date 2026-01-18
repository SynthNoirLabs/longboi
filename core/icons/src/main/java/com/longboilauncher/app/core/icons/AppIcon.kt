package com.longboilauncher.app.core.icons

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.longboilauncher.app.core.model.AppEntry

@Composable
fun AppIcon(
    appEntry: AppEntry,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    // This should come from settings
    iconThemingEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    AsyncImage(
        model =
            ImageRequest
                .Builder(context)
                .data(appEntry) // We need a custom fetcher for AppEntry
                .crossfade(true)
                .build(),
        contentDescription = appEntry.label,
        modifier =
            modifier
                .size(size)
                .clip(CircleShape),
        contentScale = ContentScale.Fit,
        // Fallback/Loading states handled via ImageRequest or custom implementation
    )
}

private fun getAppIcon(
    context: android.content.Context,
    appEntry: AppEntry,
): Drawable? =
    try {
        val launcherApps =
            context.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE)
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
