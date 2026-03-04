package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.app.core.model.ThemeType

@Composable
fun AppListItem(
    app: AppEntry,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale",
    )
    val alpha = if (app.isArchived) 0.5f else 1f
    val themeType = LocalThemeType.current

    when (themeType) {
        ThemeType.GLASSMORPHISM, ThemeType.VIBRANT_PLAYFUL -> {
            // Themed card treatment — glass blur / playful colour chip
            val containerColor =
                if (themeType == ThemeType.GLASSMORPHISM) {
                    Color.White.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                }
            val shape =
                if (themeType == ThemeType.VIBRANT_PLAYFUL) {
                    RoundedCornerShape(24.dp)
                } else {
                    RoundedCornerShape(12.dp)
                }
            val borderMod =
                if (themeType == ThemeType.GLASSMORPHISM) {
                    Modifier.border(0.5.dp, Color.White.copy(alpha = 0.2f), shape)
                } else {
                    Modifier
                }

            Card(
                modifier =
                    modifier
                        .scale(scale)
                        .alpha(alpha)
                        .fillMaxWidth()
                        .then(borderMod),
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
            ) {
                AppListItemRow(app = app)
            }
        }

        else -> {
            // Wallpaper-native: transparent, no card — wallpaper IS the canvas
            Row(
                modifier =
                    modifier
                        .scale(scale)
                        .alpha(alpha)
                        .fillMaxWidth()
                        .padding(horizontal = LongboiSpacing.L, vertical = LongboiSpacing.S),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LongboiSpacing.M),
            ) {
                AppIcon(appEntry = app, size = 48.dp)
                Text(
                    text = app.label,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            shadow =
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 4f,
                                ),
                        ),
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                ProfileBadge(profile = app.profile)
                if (app.isArchived) ArchivedIndicator()
            }
        }
    }
}

@Composable
private fun AppListItemRow(app: AppEntry) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = LongboiSpacing.L, vertical = LongboiSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LongboiSpacing.M),
    ) {
        AppIcon(appEntry = app, size = 48.dp)
        Text(
            text = app.label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f,
                        ),
                ),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ProfileBadge(profile = app.profile)
        if (app.isArchived) ArchivedIndicator()
    }
}

@Composable
private fun ProfileBadge(
    profile: ProfileType,
    modifier: Modifier = Modifier,
) {
    when (profile) {
        ProfileType.WORK -> {
            Card(
                modifier = modifier,
                shape = CircleShape,
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
            ) {
                Text(
                    text = "W",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        ProfileType.PRIVATE -> {
            Card(
                modifier = modifier,
                shape = CircleShape,
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text(
                    text = "P",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        ProfileType.PERSONAL -> Unit
    }
}

@Composable
private fun ArchivedIndicator() {
    Text(
        text = "Archived",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = LongboiSpacing.S),
    )
}
