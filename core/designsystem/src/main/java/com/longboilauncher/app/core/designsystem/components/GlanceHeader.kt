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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.model.GlanceHeaderData
import com.longboilauncher.app.core.model.ThemeType

@Composable
fun GlanceHeader(
    data: GlanceHeaderData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Time
        val themeType = LocalThemeType.current
        Text(
            text = data.currentTime,
            style =
                when (themeType) {
                    ThemeType.SOPHISTICATED_SLEEK ->
                        MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif)
                    ThemeType.MODERN_MINIMALIST ->
                        MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold)
                    else -> MaterialTheme.typography.displayLarge
                },
            fontWeight = if (themeType == ThemeType.MODERN_MINIMALIST) FontWeight.Bold else FontWeight.Light,
            color =
                when (themeType) {
                    ThemeType.SOPHISTICATED_SLEEK -> Color(0xFFF2CC0D)
                    ThemeType.GLASSMORPHISM -> Color.White
                    ThemeType.MODERN_MINIMALIST -> Color.Black
                    else -> MaterialTheme.colorScheme.onBackground
                },
            textAlign = TextAlign.Center,
        )

        // Date
        Text(
            text = data.currentDate,
            style = MaterialTheme.typography.titleMedium,
            color =
                when (themeType) {
                    ThemeType.SOPHISTICATED_SLEEK ->
                        Color(0xFFF2CC0D).copy(alpha = 0.8f)
                    ThemeType.GLASSMORPHISM -> Color.White.copy(alpha = 0.8f)
                    ThemeType.MODERN_MINIMALIST -> Color.Black.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                },
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Next Event
        data.nextEvent?.let { event ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Next: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = " • ${event.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }

        // Weather
        data.weather?.let { weather ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = weather.temperature,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = " • ${weather.condition}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }

        // Next Alarm
        data.nextAlarm?.let { alarm ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Alarm: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Text(
                    text = alarm.time,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                alarm.label?.let { label ->
                    Text(
                        text = " • $label",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
