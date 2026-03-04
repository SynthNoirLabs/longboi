package com.longboilauncher.app.feature.widgetpicker

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.longboilauncher.app.core.widgets.WidgetInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPickerScreen(
    uiState: WidgetPickerState,
    filteredWidgets: Map<String, List<WidgetInfo>>,
    onEvent: (WidgetPickerEvent) -> Unit,
    onWidgetSelected: (WidgetInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.widget_picker_title)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.widget_picker_close),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                // Search bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(WidgetPickerEvent.UpdateSearchQuery(it)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.widget_picker_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                } else if (filteredWidgets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.widget_picker_no_widgets),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        filteredWidgets.forEach { (appLabel, widgets) ->
                            item(key = "header_$appLabel") {
                                AppHeader(
                                    appLabel = appLabel,
                                    widgetCount = widgets.size,
                                    isExpanded = appLabel in uiState.expandedApps,
                                    onClick = {
                                        onEvent(WidgetPickerEvent.ToggleAppExpanded(appLabel))
                                    },
                                )
                            }

                            if (appLabel in uiState.expandedApps) {
                                items(
                                    items = widgets,
                                    key = { "${it.provider.provider.flattenToString()}" },
                                ) { widget ->
                                    WidgetItem(
                                        widget = widget,
                                        onClick = { onWidgetSelected(widget) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    appLabel: String,
    widgetCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val widgetCountText =
        if (widgetCount == 1) {
            stringResource(R.string.widget_picker_widget_count_single, widgetCount)
        } else {
            stringResource(R.string.widget_picker_widget_count_plural, widgetCount)
        }
    val toggleContentDescription =
        if (isExpanded) {
            stringResource(R.string.widget_picker_collapse)
        } else {
            stringResource(R.string.widget_picker_expand)
        }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = appLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = widgetCountText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector =
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = toggleContentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WidgetItem(
    widget: WidgetInfo,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Widget preview or icon
            WidgetPreview(
                previewImage = widget.previewImage,
                icon = widget.icon,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = widget.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        stringResource(
                            R.string.widget_picker_widget_size,
                            widget.minWidth,
                            widget.minHeight,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WidgetPreview(
    previewImage: Drawable?,
    icon: Drawable?,
    modifier: Modifier = Modifier,
) {
    val drawable = previewImage ?: icon
    if (drawable != null) {
        val bitmap = remember(drawable) { drawable.toBitmap(128, 128) }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Widgets,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
private fun WidgetPickerLoadingPreview() {
    com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme {
        WidgetPickerScreen(
            uiState = WidgetPickerState(isLoading = true),
            filteredWidgets = emptyMap(),
            onEvent = {},
            onWidgetSelected = {},
            onDismiss = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
private fun WidgetPickerEmptyPreview() {
    com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme {
        WidgetPickerScreen(
            uiState = WidgetPickerState(isLoading = false),
            filteredWidgets = emptyMap(),
            onEvent = {},
            onWidgetSelected = {},
            onDismiss = {},
        )
    }
}
