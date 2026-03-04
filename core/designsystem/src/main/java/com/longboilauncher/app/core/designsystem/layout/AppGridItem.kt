package com.longboilauncher.app.core.designsystem.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Grid item component for displaying an app in grid layout.
 *
 * @param label App label
 * @param onClick Click handler
 * @param modifier Modifier for the item
 * @param iconContent Composable for the app icon
 */
@Composable
fun AppGridItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Icon container
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) { iconContent() }

        // App label
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
