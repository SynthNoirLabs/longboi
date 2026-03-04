package com.longboilauncher.app.feature.settingsui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.appcatalog.AppCatalogRepository
import com.longboilauncher.app.core.datastore.FavoritesRepository
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.icons.AppIcon
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.feature.settingsui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HiddenAppsState(
    val allApps: List<AppEntry> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
) {
    val hiddenApps: List<AppEntry>
        get() = allApps.filter { it.packageName in hiddenPackages }

    val visibleApps: List<AppEntry>
        get() = allApps.filter { it.packageName !in hiddenPackages }
}

@HiltViewModel
class HiddenAppsViewModel
    @Inject
    constructor(
        private val appCatalogRepository: AppCatalogRepository,
        private val favoritesRepository: FavoritesRepository,
    ) : ViewModel() {
        val uiState: StateFlow<HiddenAppsState> =
            combine(
                appCatalogRepository.apps,
                favoritesRepository.hiddenApps,
            ) { apps, hidden ->
                HiddenAppsState(
                    allApps = apps.sortedBy { it.label.lowercase() },
                    hiddenPackages = hidden,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HiddenAppsState(),
            )

        fun hideApp(packageName: String) {
            viewModelScope.launch {
                favoritesRepository.hideApp(packageName)
            }
        }

        fun unhideApp(packageName: String) {
            viewModelScope.launch {
                favoritesRepository.unhideApp(packageName)
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenAppsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HiddenAppsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                    title = { Text(stringResource(R.string.hidden_apps_title)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.settings_back),
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
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                if (state.hiddenApps.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title =
                                stringResource(
                                    R.string.hidden_apps_hidden_section,
                                    state.hiddenApps.size,
                                ),
                            subtitle = stringResource(R.string.hidden_apps_hidden_subtitle),
                        )
                    }

                    items(
                        items = state.hiddenApps,
                        key = { "${it.packageName}_${it.userIdentifier}_hidden" },
                    ) { app ->
                        HiddenAppItem(
                            app = app,
                            isHidden = true,
                            onToggle = { viewModel.unhideApp(app.packageName) },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(LongboiSpacing.L))
                    }
                }

                item {
                    SectionHeader(
                        title =
                            stringResource(
                                R.string.hidden_apps_visible_section,
                                state.visibleApps.size,
                            ),
                        subtitle = stringResource(R.string.hidden_apps_visible_subtitle),
                    )
                }

                items(
                    items = state.visibleApps,
                    key = { "${it.packageName}_${it.userIdentifier}_visible" },
                ) { app ->
                    HiddenAppItem(
                        app = app,
                        isHidden = false,
                        onToggle = { viewModel.hideApp(app.packageName) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = LongboiSpacing.L,
                    vertical = LongboiSpacing.M,
                ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HiddenAppItem(
    app: AppEntry,
    isHidden: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(
                    horizontal = LongboiSpacing.L,
                    vertical = LongboiSpacing.M,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            appEntry = app,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.width(LongboiSpacing.M))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription =
                stringResource(
                    if (isHidden) {
                        R.string.hidden_apps_status_hidden
                    } else {
                        R.string.hidden_apps_status_visible
                    },
                ),
            tint =
                if (isHidden) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
