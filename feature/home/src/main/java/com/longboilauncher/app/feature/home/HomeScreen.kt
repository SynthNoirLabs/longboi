package com.longboilauncher.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.ActionsSheet
import com.longboilauncher.app.core.designsystem.components.FavoriteAppItem
import com.longboilauncher.app.core.designsystem.components.GlanceHeader
import com.longboilauncher.app.core.model.AppEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToAllApps: () -> Unit = { onEvent(HomeEvent.NavigateTo(LauncherSurface.ALL_APPS)) },
    onNavigateToSearch: () -> Unit = { onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH)) },
) {
    var showActionsSheet by remember { mutableStateOf<AppEntry?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Handle back button for local overlays
    BackHandler(enabled = showActionsSheet != null) {
        showActionsSheet = null
    }

    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffset < -100) {
                                onNavigateToAllApps()
                            } else if (dragOffset > 100) {
                                onNavigateToSearch()
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        },
                    )
                },
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    GlanceHeader(
                        data = uiState.glanceData,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Favorites List
                    if (uiState.favorites.isEmpty()) {
                        EmptyFavoritesHint(
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.favorites.forEach { favorite ->
                                FavoriteAppItem(
                                    favorite = favorite,
                                    onClick = { onEvent(HomeEvent.LaunchFavorite(favorite)) },
                                    onSwipeRight = { onEvent(HomeEvent.ShowPopup(favorite.appEntry)) },
                                    onLongClick = { showActionsSheet = favorite.appEntry },
                                )
                            }
                        }
                    }

                    // Bottom hint
                    Text(
                        text = "Swipe up for all apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 32.dp),
                    )
                }
            }

            // Actions Sheet
            showActionsSheet?.let { app ->
                ActionsSheet(
                    app = app,
                    isFavorite = uiState.favorites.any { it.appEntry.packageName == app.packageName },
                    onDismiss = { showActionsSheet = null },
                    onAddToFavorites = {
                        onEvent(HomeEvent.AddToFavorites(app))
                        showActionsSheet = null
                    },
                    onRemoveFromFavorites = {
                        val fav = uiState.favorites.find { it.appEntry.packageName == app.packageName }
                        fav?.let { onEvent(HomeEvent.RemoveFromFavorites(it.id)) }
                        showActionsSheet = null
                    },
                    onHideApp = {
                        showActionsSheet = null
                    },
                )
            }

            // Popup Panel
            uiState.popupApp?.let { app ->
                PopupPanel(
                    isVisible = true,
                    app = app,
                    shortcuts = uiState.popupShortcuts,
                    onDismiss = { onEvent(HomeEvent.HidePopup) },
                    onLaunchShortcut = { shortcut ->
                        onEvent(HomeEvent.LaunchShortcut(app, shortcut.id))
                    },
                    onAppInfo = { onEvent(HomeEvent.ShowAppInfo) },
                    onUninstall = { onEvent(HomeEvent.UninstallApp) },
                    onHide = { onEvent(HomeEvent.HideApp) },
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Swipe up to add favorite apps",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}
