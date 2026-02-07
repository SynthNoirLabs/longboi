package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ProfileType
import com.longboilauncher.core.designsystem.R

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

    Card(
        modifier =
            modifier
                .scale(scale)
                .alpha(alpha)
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // App Icon
            AppIcon(
                appEntry = app,
                size = 48.dp,
            )

            // App Label
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
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
                        containerColor = Color(0xFF4285F4),
                    ),
            ) {
                Text(
                    text = stringResource(R.string.profile_badge_work),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
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
                        containerColor = Color(0xFFEA4335),
                    ),
            ) {
                Text(
                    text = stringResource(R.string.profile_badge_private),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        ProfileType.PERSONAL -> {
            // No badge for personal profile
        }
    }
}

@Composable
private fun ArchivedIndicator() {
    Text(
        text = stringResource(R.string.app_archived),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}
