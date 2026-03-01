package com.longboilauncher.app.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.AppEntry

data class AppAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionsSheet(
    app: AppEntry,
    isFavorite: Boolean,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onRemoveFromFavorites: () -> Unit,
    onHideApp: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onRename: () -> Unit = {},
) {
    val actions =
        buildList {
            if (isFavorite) {
                add(
                    AppAction(
                        icon = Icons.Default.StarBorder,
                        label = stringResource(id = com.longboilauncher.core.designsystem.R.string.remove_from_favorites),
                        onClick = onRemoveFromFavorites,
                    ),
                )
            } else {
                add(
                    AppAction(
                        icon = Icons.Default.Star,
                        label = stringResource(id = com.longboilauncher.core.designsystem.R.string.add_to_favorites),
                        onClick = onAddToFavorites,
                    ),
                )
            }

            add(
                AppAction(
                    icon = Icons.Default.Info,
                    label = stringResource(id = com.longboilauncher.core.designsystem.R.string.app_info),
                    onClick = onAppInfo,
                ),
            )

            add(
                AppAction(
                    icon = Icons.Default.VisibilityOff,
                    label = stringResource(id = com.longboilauncher.core.designsystem.R.string.hide_app),
                    onClick = onHideApp,
                ),
            )

            if (!app.isSystemApp) {
                add(
                    AppAction(
                        icon = Icons.Default.Delete,
                        label = stringResource(id = com.longboilauncher.core.designsystem.R.string.uninstall),
                        onClick = onUninstall,
                    ),
                )
            }
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
        ) {
            // App header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    appEntry = app,
                    size = 48.dp,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            actions.forEach { action ->
                ActionItem(
                    icon = action.icon,
                    label = action.label,
                    onClick = {
                        action.onClick()
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
