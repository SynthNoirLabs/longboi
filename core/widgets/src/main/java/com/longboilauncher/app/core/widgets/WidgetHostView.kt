package com.longboilauncher.app.core.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WidgetHostViewComposable(
    widgetHost: WidgetHost,
    boundWidget: BoundWidget,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val hostView =
        remember(boundWidget.appWidgetId) { widgetHost.createView(boundWidget.appWidgetId) }

    Card(
        modifier = modifier.size(width, height),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        if (hostView != null) {
            AndroidView(
                factory = { hostView },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    val widthPx = with(density) { width.roundToPx() }
                    val heightPx = with(density) { height.roundToPx() }
                    view.updateAppWidgetSize(
                        null,
                        widthPx,
                        heightPx,
                        widthPx,
                        heightPx,
                    )
                },
            )
        } else {
            WidgetUnavailablePlaceholder()
        }
    }
}

@Composable
private fun WidgetUnavailablePlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.BrokenImage,
            contentDescription = "Widget unavailable",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

@Composable
fun WidgetContainer(
    widgets: List<BoundWidget>,
    widgetHost: WidgetHost,
    modifier: Modifier = Modifier,
    onWidgetClick: (BoundWidget) -> Unit = {},
    onWidgetLongClick: (BoundWidget) -> Unit = {},
) {
    if (widgets.isEmpty()) return

    Box(modifier = modifier) {
        widgets.forEach { widget ->
            val info = widget.providerInfo
            val minWidth = (info.minWidth / LocalDensity.current.density).dp.coerceAtLeast(100.dp)
            val minHeight = (info.minHeight / LocalDensity.current.density).dp.coerceAtLeast(100.dp)

            WidgetHostViewComposable(
                widgetHost = widgetHost,
                boundWidget = widget,
                width = minWidth,
                height = minHeight,
            )
        }
    }
}
