package com.longboilauncher.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ThemeType
import com.longboilauncher.core.designsystem.R

@Composable
fun GlanceHeader(
    data: GlanceHeaderData,
    modifier: Modifier = Modifier,
) {
    val themeType = LocalThemeType.current

    // Resolve colours once so child composables stay clean
    val primaryContentColor =
        when (themeType) {
            ThemeType.GLASSMORPHISM -> Color.White
            ThemeType.MODERN_MINIMALIST -> Color.Black
            else -> Color.White // Default to white for dark wallpapers
        }
    val secondaryContentColor = primaryContentColor.copy(alpha = 0.6f)
    val accentColor =
        when (themeType) {
            ThemeType.SOPHISTICATED_SLEEK -> MaterialTheme.colorScheme.primary
            else -> primaryContentColor
        }

    val textShadow =
        Shadow(
            color = Color.Black.copy(alpha = 0.5f),
            offset = Offset(0f, 2f),
            blurRadius = 4f,
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = LongboiSpacing.L, vertical = LongboiSpacing.XL),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(LongboiSpacing.S),
    ) {
        // Clock — Thin weight, full display size
        Text(
            text = data.currentTime,
            style =
                when (themeType) {
                    ThemeType.SOPHISTICATED_SLEEK ->
                        MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif, shadow = textShadow)
                    else -> MaterialTheme.typography.displayLarge.copy(shadow = textShadow)
                },
            fontWeight =
                if (themeType == ThemeType.MODERN_MINIMALIST) FontWeight.Light else FontWeight.Thin,
            color = accentColor,
            textAlign = TextAlign.Start,
        )

        // Date
        Text(
            text = data.currentDate,
            style = MaterialTheme.typography.titleMedium.copy(shadow = textShadow),
            color = secondaryContentColor,
            textAlign = TextAlign.Start,
        )

        Spacer(modifier = Modifier.height(LongboiSpacing.L))

        // Next calendar event
        data.nextEvent?.let { event ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.glance_next_event_label),
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                    color = secondaryContentColor,
                )
                Text(
                    text = " ${event.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                    fontWeight = FontWeight.Medium,
                    color = primaryContentColor,
                )
                Text(
                    text = stringResource(R.string.glance_separator) + event.time,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                    color = secondaryContentColor,
                )
            }
        }

        // Weather
        data.weather?.let { weather ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = weather.temperature,
                    style = MaterialTheme.typography.titleLarge.copy(shadow = textShadow),
                    color = primaryContentColor,
                )
                Text(
                    text = stringResource(R.string.glance_separator) + weather.condition,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                    color = secondaryContentColor,
                )
            }
        }

        // Next alarm
        data.nextAlarm?.let { alarm ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.glance_alarm_label),
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                    color = secondaryContentColor,
                )
                Text(
                    text = " ${alarm.time}",
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                    fontWeight = FontWeight.Medium,
                    color = primaryContentColor,
                )
                alarm.label?.let { label ->
                    Text(
                        text = stringResource(R.string.glance_separator) + label,
                        style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                        color = secondaryContentColor,
                    )
                }
            }
        }
    }
}
