package com.longboilauncher.app.core.model

import androidx.compose.runtime.Stable

@Stable
data class GlanceHeaderData(
    val currentTime: String,
    val currentDate: String,
    val nextEvent: CalendarEvent? = null,
    val weather: WeatherInfo? = null,
    val nextAlarm: AlarmInfo? = null,
    val nowPlaying: MediaInfo? = null,
)

@Stable
data class CalendarEvent(
    val title: String,
    val time: String,
    val color: Int,
)

@Stable
data class WeatherInfo(
    val temperature: String,
    val condition: String,
    val icon: String? = null,
)

@Stable
data class AlarmInfo(
    val time: String,
    val label: String? = null,
)

@Stable
data class MediaInfo(
    val appName: String,
    val title: String,
    val artist: String? = null,
    val albumArt: String? = null,
)
