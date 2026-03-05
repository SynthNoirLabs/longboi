package com.longboilauncher.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ThemeType

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    val isGlass = themeType == ThemeType.GLASSMORPHISM

    val playfulColors =
        listOf(
            Color(0xFFA5D6A7), // Green
            Color(0xFFFFAB91), // Orange
            Color(0xFF81D4FA), // Blue
            Color(0xFFF48FB1), // Pink
            Color(0xFFCE93D8), // Purple
        )
    val appColorIndex = Math.abs(favorite.appEntry.packageName.hashCode()) % playfulColors.size

    val containerColor =
        when (themeType) {
            ThemeType.GLASSMORPHISM -> Color.White.copy(alpha = 0.15f)
            ThemeType.VIBRANT_PLAYFUL -> playfulColors[appColorIndex]
            ThemeType.SOPHISTICATED_SLEEK -> Color(0xFF1A1A1A)
            ThemeType.MODERN_MINIMALIST -> Color.Transparent
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    val contentColor =
        when (themeType) {
            ThemeType.GLASSMORPHISM -> Color.White
            ThemeType.VIBRANT_PLAYFUL -> Color.White
            ThemeType.SOPHISTICATED_SLEEK -> Color(0xFFF2CC0D)
            else -> MaterialTheme.colorScheme.onSurface
        }
    val shape =
        when (themeType) {
            ThemeType.VIBRANT_PLAYFUL -> RoundedCornerShape(32.dp)
            ThemeType.MODERN_MINIMALIST -> RoundedCornerShape(0.dp)
            else -> RoundedCornerShape(16.dp)
        }
    val borderStroke =
        when (themeType) {
            ThemeType.GLASSMORPHISM -> Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), shape)
            ThemeType.SOPHISTICATED_SLEEK -> Modifier.border(0.5.dp, Color(0xFFF2CC0D).copy(alpha = 0.3f), shape)
            else -> Modifier
        }

    val itemModifier =
        Modifier
            .scale(scale)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 100) {
                            onSwipeRight()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    },
                )
            }.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )

    val itemContent =
        @Composable {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isGlass) 24.dp else 12.dp),
            ) {
                // App Icon - with glass backdrop for glass theme
                if (isGlass) {
                    GlassCard(
                        modifier = Modifier.size(64.dp),
                        cornerRadius = 24.dp,
                        backgroundAlpha = 0.1f,
                        blurRadius = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            AppIcon(
                                appEntry = favorite.appEntry,
                                size = 40.dp,
                            )
                        }
                    }
                } else {
                    AppIcon(
                        appEntry = favorite.appEntry,
                        size = 48.dp,
                    )
                }

                // App Label
                Text(
                    text = favorite.displayLabel,
                    style = if (isGlass) {
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.15f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    },
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                // Status Indicators
                if (favorite.isPlaying) {
                    PlayingIndicator()
                }

                if (showNotifications && favorite.hasNotifications) {
                    NotificationDot(count = favorite.notificationCount)
                }
            }
        }

    if (isGlass) {
        GlassCard(
            modifier = modifier.then(itemModifier),
            containerColor = Color.White.copy(alpha = 0.1f),
            borderColor = Color.White.copy(alpha = 0.2f),
            cornerRadius = 24.dp,
        ) {
            itemContent()
        }
    } else {
        Card(
            modifier = modifier.then(itemModifier).then(borderStroke),
            shape = shape,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp,
                ),
            onClick = onClick,
        ) {
            itemContent()
        }
    }
}

@Composable
private fun PlayingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "♪",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun NotificationDot(count: Int) {
    if (count > 0) {
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Spacer(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
        )
    }
}
