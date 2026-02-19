package com.longboilauncher.app.feature.settingsui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
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
        @Composable
        private fun ThemeSwitcher(
            currentTheme: ThemeType,
            onThemeSelected: (ThemeType) -> Unit,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ThemeType.entries.forEach { theme ->
                    val isSelected = theme == currentTheme
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(80.dp)
                                .background(
                                    color =
                                        when (theme) {
                                            ThemeType.GLASSMORPHISM -> Color.Cyan.copy(alpha = 0.3f)
                                            ThemeType.VIBRANT_PLAYFUL -> Color.Magenta.copy(alpha = 0.3f)
                                            ThemeType.SOPHISTICATED_SLEEK -> Color.Black
                                            ThemeType.MODERN_MINIMALIST -> Color.White
                                            else -> Color.Gray.copy(alpha = 0.3f)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                ).border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Gray.copy(alpha = 0.5f)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                ).clickable { onThemeSelected(theme) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                when (theme) {
                                    ThemeType.GLASSMORPHISM -> "✨"
                                    ThemeType.VIBRANT_PLAYFUL -> "🎨"
                                    ThemeType.SOPHISTICATED_SLEEK -> "💎"
                                    ThemeType.MODERN_MINIMALIST -> "⚡"
                                    else -> "📱"
                                },
                            style = MaterialTheme.typography.headlineSmall,
                            color =
                                if (theme == ThemeType.SOPHISTICATED_SLEEK) {
                                    Color.Yellow
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                }
            }
        }

        Scaffold(
            containerColor =
                if (LocalThemeType.current ==
                    ThemeType.MATERIAL_YOU
                ) {
                    MaterialTheme.colorScheme.background
                } else {
                    Color.Transparent
                },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
            ) {
                ThemeSwitcher(uiState.theme) { onEvent(SettingsEvent.SetTheme(it)) }
                // Appearance Section
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        icon = Icons.Default.ColorLens,
                        title = "Theme",
                        subtitle =
                            uiState.theme.name
                                .lowercase()
                                .replace("_", " ")
                                .replaceFirstChar { it.uppercase() },
                        onClick = {
                            onEvent(
                                SettingsEvent.SetTheme(
                                    ThemeType.entries[(uiState.theme.ordinal + 1) % ThemeType.entries.size],
                                ),
                            )
                        },
                    )
                    SettingsItem(
                        icon = Icons.Default.GridView,
                        title = "Density",
                        subtitle = uiState.density.replaceFirstChar { it.uppercase() },
                        onClick = { /* TODO: Show density picker */ },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Home Section
                SettingsSection(title = "Home") {
                    SettingsItem(
                        icon = Icons.Default.Home,
                        title = "Favorites",
                        subtitle = "Manage your favorite apps",
                        onClick = { /* TODO: Show favorites editor */ },
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Notification dots",
                        subtitle = "Show notification indicators",
                        checked = uiState.showNotifications,
                        onCheckedChange = { onEvent(SettingsEvent.SetShowNotifications(it)) },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Behavior Section
                SettingsSection(title = "Behavior") {
                    SettingsItem(
                        icon = Icons.Default.TouchApp,
                        title = "Gestures",
                        subtitle = "Customize swipe actions",
                        onClick = { /* TODO: Show gesture settings */ },
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.TouchApp,
                        title = "Haptic feedback",
                        subtitle = "Vibration on interactions",
                        checked = uiState.hapticsEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.SetHapticsEnabled(it)) },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Apps Section
                SettingsSection(title = "Apps") {
                    SettingsItem(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hidden apps",
                        subtitle = "Manage hidden applications",
                        onClick = { /* TODO: Show hidden apps manager */ },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Backup Section
                SettingsSection(title = "Backup") {
                    SettingsItem(
                        icon = Icons.Default.SettingsBackupRestore,
                        title = "Backup & Restore",
                        subtitle = "Export or import settings",
                        onClick = { /* TODO: Show backup options */ },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // About Section
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = { },
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    @Composable
    private fun SettingsSection(
        title: String,
        content: @Composable () -> Unit,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            content()
        }
    }

    @Composable
    private fun SettingsItem(
        icon: ImageVector,
        title: String,
        subtitle: String,
        onClick: () -> Unit,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onCheckedChange(!checked) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
