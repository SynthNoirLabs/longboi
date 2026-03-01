package com.longboilauncher.app.feature.home

import android.content.pm.ShortcutInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.AppEntry

data class PopupAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun PopupPanel(
    isVisible: Boolean,
    app: AppEntry,
    shortcuts: List<ShortcutInfo>,
    onDismiss: () -> Unit,
    onLaunchShortcut: (ShortcutInfo) -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isVisible) {
        Box(modifier = modifier.fillMaxSize()) {
            // Backdrop
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                        .clickable { onDismiss() },
            )

            // Panel
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(320.dp)
                            .align(Alignment.CenterEnd)
                            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(
                                    appEntry = app,
                                    modifier = Modifier.size(48.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Actions
                        Text(
                            text = stringResource(id = com.longboilauncher.core.designsystem.R.string.quick_actions),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickActionsGrid(
                            onAppInfo = onAppInfo,
                            onUninstall = onUninstall,
                            onHide = onHide,
                        )

                        if (shortcuts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(id = com.longboilauncher.core.designsystem.R.string.shortcuts),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ShortcutsList(
                                shortcuts = shortcuts,
                                onLaunchShortcut = onLaunchShortcut,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onHide: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionRow(
            icon = Icons.Default.Info,
            title = stringResource(id = com.longboilauncher.core.designsystem.R.string.app_info),
            onClick = onAppInfo,
        )
        ActionRow(
            icon = Icons.Default.Delete,
            title = stringResource(id = com.longboilauncher.core.designsystem.R.string.uninstall),
            onClick = onUninstall,
        )
        ActionRow(
            icon = Icons.Default.VisibilityOff,
            title = stringResource(id = com.longboilauncher.core.designsystem.R.string.hide_app),
            onClick = onHide,
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ShortcutsList(
    shortcuts: List<ShortcutInfo>,
    onLaunchShortcut: (ShortcutInfo) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(shortcuts, key = { it.id }) { shortcut ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLaunchShortcut(shortcut) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // TODO: Load shortcut icon
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            shortcut.shortLabel
                                ?.toString()
                                ?.firstOrNull()
                                ?.uppercaseChar()
                                ?.toString() ?: "?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = shortcut.shortLabel?.toString() ?: "Shortcut",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
