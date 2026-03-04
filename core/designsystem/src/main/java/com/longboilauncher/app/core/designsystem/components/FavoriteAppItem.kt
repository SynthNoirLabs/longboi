package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ThemeType

private val PlayfulPalette =
    listOf(
        Color(0xFFA5D6A7),
        Color(0xFFFFAB91),
        Color(0xFF81D4FA),
        Color(0xFFF48FB1),
        Color(0xFFCE93D8),
    )

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteAppItem(
    favorite: FavoriteEntry,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    showNotifications: Boolean = true,
    onClick: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale",
    )
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val themeType = LocalThemeType.current
    val appColorIndex = Math.abs(favorite.appEntry.packageName.hashCode()) % PlayfulPalette.size

    val containerColor =
        when (themeType) {
            ThemeType.GLASSMORPHISM -> Color.White.copy(alpha = 0.15f)
            ThemeType.VIBRANT_PLAYFUL -> PlayfulPalette[appColorIndex]
            ThemeType.SOPHISTICATED_SLEEK -> Color(0xFF1A1A1A)
            else -> Color.Transparent
        }
    val contentColor =
        when (themeType) {
            ThemeType.VIBRANT_PLAYFUL -> Color.White
            ThemeType.SOPHISTICATED_SLEEK -> MaterialTheme.colorScheme.primary
            else -> Color.White
        }
    val shape =
        when (themeType) {
            ThemeType.VIBRANT_PLAYFUL -> RoundedCornerShape(32.dp)
            ThemeType.MODERN_MINIMALIST -> RoundedCornerShape(0.dp)
            else -> RoundedCornerShape(16.dp)
        }
    val borderMod =
        when (themeType) {
            ThemeType.GLASSMORPHISM ->
                Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), shape)
            ThemeType.SOPHISTICATED_SLEEK ->
                Modifier.border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), shape)
            else -> Modifier
        }

    val useCard = themeType != ThemeType.MATERIAL_YOU && themeType != ThemeType.MODERN_MINIMALIST

    val interactionModifier =
        Modifier
            .scale(scale)
            .fillMaxWidth()
            .then(borderMod)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 100) onSwipeRight()
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount },
                )
            }.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )

    if (useCard) {
        Card(
            modifier = modifier.then(interactionModifier),
            shape = shape,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
        ) {
            FavoriteRow(favorite = favorite, contentColor = contentColor, showNotifications = showNotifications)
        }
    } else {
        // Wallpaper-native: transparent, direct on wallpaper
        Row(
            modifier =
                modifier
                    .then(interactionModifier)
                    .padding(horizontal = LongboiSpacing.L, vertical = LongboiSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LongboiSpacing.M),
        ) {
            AppIcon(appEntry = favorite.appEntry, size = 48.dp)
            Text(
                text = favorite.displayLabel,
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
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (favorite.isPlaying) PlayingIndicator()
            if (showNotifications && favorite.hasNotifications) {
                NotificationDot(count = favorite.notificationCount)
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteEntry,
    contentColor: Color,
    showNotifications: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = LongboiSpacing.L, vertical = LongboiSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LongboiSpacing.M),
    ) {
        AppIcon(appEntry = favorite.appEntry, size = 48.dp)
        Text(
            text = favorite.displayLabel,
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
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (favorite.isPlaying) PlayingIndicator()
        if (showNotifications && favorite.hasNotifications) {
            NotificationDot(count = favorite.notificationCount)
        }
    }
}

@Composable
private fun PlayingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LongboiSpacing.XS),
    ) {
        Spacer(modifier = Modifier.width(LongboiSpacing.XS))
        Text(
            text = "♪",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun NotificationDot(count: Int) {
    if (count > 0) {
        Card(
            modifier = Modifier.size(20.dp),
            shape = CircleShape,
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    } else {
        Spacer(
            modifier =
                Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
        )
    }
}
