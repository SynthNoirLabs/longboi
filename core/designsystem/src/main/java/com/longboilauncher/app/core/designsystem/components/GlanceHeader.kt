package com.longboilauncher.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.LocalLongboiColors
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.model.GlanceHeaderData

@Composable
fun GlanceHeader(
    data: GlanceHeaderData,
    modifier: Modifier = Modifier,
) {
    val contentColor = LocalLongboiColors.current.onWallpaperContent
    val shadowColor = if (contentColor.luminance() > 0.5f) Color.Black else Color.White

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = LongboiSpacing.ScreenEdgePadding,
                    end = LongboiSpacing.ScreenEdgePadding,
                    top = LongboiSpacing.ClockTopMargin,
                    bottom = LongboiSpacing.L,
                ),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Massive Clock - The centerpiece
        Text(
            text = data.currentTime,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    shadow =
                        androidx.compose.ui.graphics.Shadow(
                            color = shadowColor.copy(alpha = 0.2f),
                            offset =
                                androidx.compose.ui.geometry
                                    .Offset(0f, 4f),
                            blurRadius = 8f,
                        ),
                ),
            color = contentColor,
        )

        // Date Line - Elegant and secondary
        Text(
            text = data.currentDate,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor.copy(alpha = 0.7f),
        )

        // Optional Context Modules (Event, Weather, etc.) - Compact and minimal
        if (data.nextEvent != null || data.weather != null || data.nextAlarm != null) {
            Spacer(modifier = Modifier.height(12.dp))

            data.nextEvent?.let { event ->
                Text(
                    text = "${event.title} • ${event.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.6f),
                )
            }

            data.weather?.let { weather ->
                Text(
                    text = "${weather.temperature} • ${weather.condition}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.6f),
                )
            }
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
