package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.LocalLongboiColors
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
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
    val customColors = LocalLongboiColors.current
    val isGlass = customColors.useBlur

    val containerColor =
        when (themeType) {
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

    val shape =
        when (themeType) {
            ThemeType.VIBRANT_PLAYFUL -> RoundedCornerShape(24.dp)
            ThemeType.MODERN_MINIMALIST -> RoundedCornerShape(0.dp)
            else -> RoundedCornerShape(12.dp)
        }

    val itemContent =
        @Composable {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isGlass) 20.dp else 12.dp),
            ) {
                // App Icon
                AppIcon(
                    appEntry = app,
                    size = if (isGlass) 52.dp else 48.dp,
                )

                // App Label
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isGlass) FontWeight.Light else FontWeight.Medium,
                    color = customColors.onWallpaperContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                // Profile Badge
                ProfileBadge(profile = app.profile)

                // Status Indicators
                if (app.isArchived) {
                    ArchivedIndicator()
                }
            }
        }

    if (isGlass) {
        GlassCard(
            modifier =
                modifier
                    .scale(scale)
                    .alpha(alpha)
                    .fillMaxWidth(),
            containerColor = Color.White.copy(alpha = customColors.cardAlpha),
            borderColor = Color.White.copy(alpha = customColors.borderAlpha),
            cornerRadius = 12.dp,
        ) {
            itemContent()
        }
    } else {
        Card(
            modifier =
                modifier
                    .scale(scale)
                    .alpha(alpha)
                    .fillMaxWidth(),
            shape = shape,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor.copy(alpha = customColors.cardAlpha),
                    contentColor = customColors.onWallpaperContent,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 1.dp,
                ),
        ) {
            itemContent()
        }
    }
}

@Composable
private fun ProfileBadge(
    profile: ProfileType,
    modifier: Modifier = Modifier,
) {
    val badgeColor =
        when (profile) {
            ProfileType.WORK -> Color(0xFF4285F4)
            ProfileType.PRIVATE -> Color(0xFFEA4335)
            ProfileType.PERSONAL -> return
        }

    androidx.compose.foundation.Canvas(
        modifier = modifier.size(10.dp),
        onDraw = {
            drawCircle(color = badgeColor)
        },
    )
}

@Composable
private fun ArchivedIndicator() {
    Text(
        text = "Archived",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}
