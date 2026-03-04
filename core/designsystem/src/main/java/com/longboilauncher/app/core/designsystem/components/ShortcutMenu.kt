package com.longboilauncher.app.core.designsystem.components

import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.longboilauncher.app.core.designsystem.theme.LongboiCorners
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing

/**
 * A popup menu displaying app shortcuts.
 * Shows on long-press of an app icon.
 */
@Composable
fun ShortcutMenu(
    shortcuts: List<ShortcutInfo>,
    visible: Boolean,
    onShortcutClick: (ShortcutInfo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    getShortcutIcon: (ShortcutInfo) -> Drawable? = { null },
) {
    AnimatedVisibility(
        visible = visible && shortcuts.isNotEmpty(),
        enter =
            scaleIn(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                transformOrigin = TransformOrigin(0.5f, 0f),
            ) + fadeIn(),
        exit =
            scaleOut(
                transformOrigin = TransformOrigin(0.5f, 0f),
            ) + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(LongboiCorners.L),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.width(220.dp),
        ) {
            Column(
                modifier = Modifier.padding(vertical = LongboiSpacing.S),
            ) {
                shortcuts.take(MAX_SHORTCUTS).forEach { shortcut ->
                    ShortcutMenuItem(
                        shortcut = shortcut,
                        icon = getShortcutIcon(shortcut),
                        onClick = {
                            onShortcutClick(shortcut)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutMenuItem(
    shortcut: ShortcutInfo,
    icon: Drawable?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = LongboiSpacing.M,
                    vertical = LongboiSpacing.S,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Shortcut icon
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(LongboiCorners.S))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                val bitmap = icon.toBitmap(48, 48).asImageBitmap()
                Icon(
                    painter = BitmapPainter(bitmap),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(LongboiSpacing.M))

        // Shortcut label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortcut.shortLabel?.toString() ?: shortcut.id,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            shortcut.longLabel?.let { longLabel ->
                if (longLabel.toString() != shortcut.shortLabel?.toString()) {
                    Text(
                        text = longLabel.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Compact shortcut chip for inline display.
 */
@Composable
fun ShortcutChip(
    shortcut: ShortcutInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(LongboiCorners.M),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = shortcut.shortLabel?.toString() ?: shortcut.id,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    horizontal = LongboiSpacing.M,
                    vertical = LongboiSpacing.S,
                ),
        )
    }
}

/**
 * Horizontal row of shortcut chips.
 */
@Composable
fun ShortcutChipRow(
    shortcuts: List<ShortcutInfo>,
    onShortcutClick: (ShortcutInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (shortcuts.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LongboiSpacing.S),
    ) {
        shortcuts.take(MAX_SHORTCUTS).forEach { shortcut ->
            ShortcutChip(
                shortcut = shortcut,
                onClick = { onShortcutClick(shortcut) },
            )
        }
    }
}

private const val MAX_SHORTCUTS = 5
