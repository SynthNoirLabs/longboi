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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.longboilauncher.feature.settingsui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
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
            // Appearance Section
            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                SettingsItem(
                    icon = Icons.Default.ColorLens,
                    title = stringResource(R.string.settings_theme),
                    subtitle = uiState.theme.replaceFirstChar { it.uppercase() },
                    onClick = { /* TODO: Show theme picker */ },
                )
                SettingsItem(
                    icon = Icons.Default.GridView,
                    title = stringResource(R.string.settings_density),
                    subtitle = uiState.density.replaceFirstChar { it.uppercase() },
                    onClick = { /* TODO: Show density picker */ },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Home Section
            SettingsSection(title = stringResource(R.string.settings_section_home)) {
                SettingsItem(
                    icon = Icons.Default.Home,
                    title = stringResource(R.string.settings_favorites),
                    subtitle = stringResource(R.string.settings_favorites_subtitle),
                    onClick = { /* TODO: Show favorites editor */ },
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_notification_dots),
                    subtitle = stringResource(R.string.settings_notification_dots_subtitle),
                    checked = uiState.showNotifications,
                    onCheckedChange = { onEvent(SettingsEvent.SetShowNotifications(it)) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Behavior Section
            SettingsSection(title = stringResource(R.string.settings_section_behavior)) {
                SettingsItem(
                    icon = Icons.Default.TouchApp,
                    title = stringResource(R.string.settings_gestures),
                    subtitle = stringResource(R.string.settings_gestures_subtitle),
                    onClick = { /* TODO: Show gesture settings */ },
                )
                SettingsSwitchItem(
                    icon = Icons.Default.TouchApp,
                    title = stringResource(R.string.settings_haptic_feedback),
                    subtitle = stringResource(R.string.settings_haptic_feedback_subtitle),
                    checked = uiState.hapticsEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.SetHapticsEnabled(it)) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Apps Section
            SettingsSection(title = stringResource(R.string.settings_section_apps)) {
                SettingsItem(
                    icon = Icons.Default.VisibilityOff,
                    title = stringResource(R.string.settings_hidden_apps),
                    subtitle = stringResource(R.string.settings_hidden_apps_subtitle),
                    onClick = { /* TODO: Show hidden apps manager */ },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Backup Section
            SettingsSection(title = stringResource(R.string.settings_section_backup)) {
                SettingsItem(
                    icon = Icons.Default.SettingsBackupRestore,
                    title = stringResource(R.string.settings_backup_restore),
                    subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                    onClick = { /* TODO: Show backup options */ },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // About Section
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = stringResource(R.string.settings_version_number),
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
