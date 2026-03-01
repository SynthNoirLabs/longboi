package com.longboilauncher.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.common.handleGestures
import com.longboilauncher.app.core.designsystem.components.ActionsSheet
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.components.FavoriteAppItem
import com.longboilauncher.app.core.designsystem.components.GlanceHeader
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.settings.HapticFeedbackManager
import com.longboilauncher.feature.home.R
import com.longboilauncher.app.feature.allapps.AlphabetScrubber
import com.longboilauncher.app.feature.allapps.FloatingLetterIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
    hapticFeedbackManager: HapticFeedbackManager,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showActionsSheet by remember { mutableStateOf<AppEntry?>(null) }
    var currentLetter by remember { mutableStateOf("A") }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubbingLetter by remember { mutableStateOf<String?>(null) }

    // Build the unified list: Favorites + All Apps Sections
    val flatList = remember(uiState.favorites, uiState.appSections) {
        buildList {
            // Favorites section
            if (uiState.favorites.isNotEmpty()) {
                uiState.favorites.forEach { favorite ->
                    add(HomeScreenItem.Favorite(favorite.appEntry))
                }
                add(HomeScreenItem.Spacer(48.dp))
            }

            // All Apps sections
            uiState.appSections.forEach { (letter, apps) ->
                if (apps.isNotEmpty()) {
                    add(HomeScreenItem.Header(letter))
                    apps.forEach { app ->
                        add(HomeScreenItem.App(app))
                    }
                }
            }
        }
    }

    // Determine when we are in "All Apps" mode based on scroll
    val isAllAppsVisible = remember(listState.firstVisibleItemIndex) {
        val firstVisible = flatList.getOrNull(listState.firstVisibleItemIndex)
        firstVisible !is HomeScreenItem.Favorite
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            var overscrollAccumulator = 0f
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If we're dragging downwards and the list is already at the top
                if (available.y > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    overscrollAccumulator += available.y
                    if (overscrollAccumulator > 50f) {
                        onEvent(HomeEvent.ExpandNotifications)
                        overscrollAccumulator = 0f
                        // Consume the rest of the gesture to prevent multiple triggers
                        return Offset(0f, available.y)
                    }
                } else if (available.y < 0) {
                    overscrollAccumulator = 0f
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(isAllAppsVisible) {
        if (isAllAppsVisible && uiState.currentSurface != LauncherSurface.ALL_APPS) {
            onEvent(HomeEvent.NavigateTo(LauncherSurface.ALL_APPS))
        } else if (!isAllAppsVisible && uiState.currentSurface != LauncherSurface.HOME) {
            onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
        }
    }

    // Handle back button
    BackHandler(enabled = showActionsSheet != null || uiState.currentSurface != LauncherSurface.HOME) {
        if (showActionsSheet != null) {
            showActionsSheet = null
        } else {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    ThemeBackground(themeType = uiState.theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                            .padding(horizontal = 16.dp),
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                            GlanceHeader(
                                data = uiState.glanceData,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(48.dp))
                        }

                        items(
                            items = flatList,
                            key = { item ->
                                when (item) {
                                    is HomeScreenItem.Favorite -> "fav_${item.app.packageName}_${item.app.userIdentifier}"
                                    is HomeScreenItem.App -> "app_${item.app.packageName}_${item.app.userIdentifier}"
                                    is HomeScreenItem.Header -> "header_${item.letter}"
                                    is HomeScreenItem.Spacer -> "spacer_${item.height.value}"
                                }
                            }
                        ) { item ->
                            when (item) {
                                is HomeScreenItem.Favorite -> {
                                    FavoriteAppItem(
                                        appEntry = item.app,
                                        onClick = { onEvent(HomeEvent.LaunchApp(item.app)) },
                                        onLongClick = { showActionsSheet = item.app },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                is HomeScreenItem.App -> {
                                    AppListItem(
                                        app = item.app,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onEvent(HomeEvent.LaunchApp(item.app)) }
                                            .padding(vertical = 4.dp),
                                    )
                                }
                                is HomeScreenItem.Header -> {
                                    Text(
                                        text = item.letter,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                                is HomeScreenItem.Spacer -> {
                                    Spacer(modifier = Modifier.height(item.height))
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }

                // Alphabet Scrubber on the right
                AlphabetScrubber(
                    letters = uiState.sectionIndices.keys.toList(),
                    currentLetter = currentLetter,
                    hapticFeedbackManager = hapticFeedbackManager,
                    onScrubStateChanged = { active, letter ->
                        isScrubbing = active
                        scrubbingLetter = letter
                    },
                    onLetterSelected = { letter ->
                        currentLetter = letter
                        val indexInAllApps = uiState.sectionIndices[letter] ?: return@AlphabetScrubber
                        // Offset by items before the first header (Glance, Favorites, Spacers)
                        val itemsBeforeApps = flatList.indexOfFirst { it is HomeScreenItem.Header }
                        if (itemsBeforeApps != -1) {
                            coroutineScope.launch {
                                if (isScrubbing) {
                                    listState.scrollToItem(indexInAllApps + itemsBeforeApps + 1) // +1 for Glance item
                                } else {
                                    listState.animateScrollToItem(indexInAllApps + itemsBeforeApps + 1)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(0.8f)
                        .width(32.dp)
                        .padding(end = 8.dp)
                        .testTag("alphabet_scrubber"),
                )

                if (isScrubbing) {
                    scrubbingLetter?.let { letter ->
                        FloatingLetterIndicator(
                            letter = letter,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(end = 64.dp)
                                .testTag("floating_letter_indicator"),
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
                            onEvent(HomeEvent.HideApp(app))
                            showActionsSheet = null
                        },
                        onAppInfo = {
                            onEvent(HomeEvent.ShowAppInfo(app))
                            showActionsSheet = null
                        },
                        onUninstall = {
                            onEvent(HomeEvent.UninstallApp(app))
                            showActionsSheet = null
                        },
                    )
                }
            }
        }
    }
}

private sealed class HomeScreenItem {
    data class Favorite(val app: AppEntry) : HomeScreenItem()
    data class App(val app: AppEntry) : HomeScreenItem()
    data class Header(val letter: String) : HomeScreenItem()
    data class Spacer(val height: androidx.compose.ui.unit.Dp) : HomeScreenItem()
}

@Composable
private fun FavoriteAppItem(
    appEntry: AppEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.longboilauncher.app.core.icons.AppIcon(
            appEntry = appEntry,
            size = 48.dp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appEntry.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
