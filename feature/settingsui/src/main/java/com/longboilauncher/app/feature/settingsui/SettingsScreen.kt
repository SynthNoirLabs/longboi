package com.longboilauncher.app.feature.settingsui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.GlassCard
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.model.ThemeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ThemeBackground(themeType = LocalThemeType.current) {
        val isGlass = LocalThemeType.current == ThemeType.GLASSMORPHISM

        Scaffold(
            containerColor = Color.Transparent, // Immersive background
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isGlass) Color.White else Color.Unspecified,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isGlass) Color.White else Color.Unspecified,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = if (isGlass) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        titleContentColor = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                    ),
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
            ) {
                // Section: Appearance
                SettingsSection(title = "APPEARANCE", isGlass = isGlass) {
                    SettingsItem(
                        icon = Icons.Default.ColorLens,
                        title = "Theme",
                        subtitle = uiState.theme.name.lowercase().replace("_", " ").capitalize(),
                        isGlass = isGlass,
                        onClick = {
                            onEvent(SettingsEvent.SetTheme(
                                ThemeType.entries[(uiState.theme.ordinal + 1) % ThemeType.entries.size]
                            ))
                        },
                    )
                    SettingsItem(
                        icon = Icons.Default.GridView,
                        title = "Density",
                        subtitle = uiState.density.capitalize(),
                        isGlass = isGlass,
                        onClick = { },
                    )
                }

                // Section: Home
                SettingsSection(title = "HOME", isGlass = isGlass) {
                    SettingsItem(
                        icon = Icons.Default.Home,
                        title = "Favorites",
                        subtitle = "Manage top apps",
                        isGlass = isGlass,
                        onClick = { },
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Notification dots",
                        subtitle = "Show indicators",
                        checked = uiState.showNotifications,
                        isGlass = isGlass,
                        onCheckedChange = { onEvent(SettingsEvent.SetShowNotifications(it)) },
                    )
                }

                // Section: Interaction
                SettingsSection(title = "INTERACTION", isGlass = isGlass) {
                    SettingsItem(
                        icon = Icons.Default.TouchApp,
                        title = "Gestures",
                        subtitle = "Swipe actions",
                        isGlass = isGlass,
                        onClick = { },
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.TouchApp,
                        title = "Haptic feedback",
                        subtitle = "Subtle vibrations",
                        checked = uiState.hapticsEnabled,
                        isGlass = isGlass,
                        onCheckedChange = { onEvent(SettingsEvent.SetHapticsEnabled(it)) },
                    )
                }

                // Section: System
                SettingsSection(title = "SYSTEM", isGlass = isGlass) {
                    SettingsItem(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hidden apps",
                        subtitle = "Manage visibility",
                        isGlass = isGlass,
                        onClick = { },
                    )
                    SettingsItem(
                        icon = Icons.Default.SettingsBackupRestore,
                        title = "Backup & Restore",
                        subtitle = "Export layout",
                        isGlass = isGlass,
                        onClick = { },
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "Longboi v1.0",
                        isGlass = isGlass,
                        onClick = { },
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    isGlass: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isGlass) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
        )
        if (isGlass) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundAlpha = 0.2f,
                cornerRadius = 28.dp,
            ) {
                Column {
                    content()
                }
            }
        } else {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGlass: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isGlass) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGlass) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isGlass: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isGlass) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGlass) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
            )
        )
    }
}

private fun String.capitalize(): String = this.replaceFirstChar { it.uppercase() }
