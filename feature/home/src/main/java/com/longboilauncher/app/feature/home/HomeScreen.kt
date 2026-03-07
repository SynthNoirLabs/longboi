package com.longboilauncher.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.designsystem.components.ActionsSheet
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.components.CompactCurvedAlphabetScrubber
import com.longboilauncher.app.core.designsystem.components.FavoriteAppItem
import com.longboilauncher.app.core.designsystem.components.FloatingLetterIndicator
import com.longboilauncher.app.core.designsystem.components.GlanceHeader
import com.longboilauncher.app.core.designsystem.components.GlassCard
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.designsystem.theme.LocalLongboiColors
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ThemeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
    hapticFeedbackManager: HapticFeedbackManager,
) {
    val favoritesListState = rememberLazyListState()
    val scrubbedListState = rememberLazyListState()
    val customColors = LocalLongboiColors.current
    val contentColor = customColors.onWallpaperContent

    var showActionsSheet by remember { mutableStateOf<AppEntry?>(null) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubbingLetter by remember { mutableStateOf<String?>(null) }

    // Build the favorites list
    val favoritesFlatList =
        remember(uiState.favorites) {
            buildList {
                if (uiState.favorites.isNotEmpty()) {
                    uiState.favorites.forEach { add(HomeScreenItem.Favorite(it)) }
                } else {
                    add(HomeScreenItem.EmptyFavorites)
                }
            }
        }

    // Build the scrubbed list
    val scrubbedFlatList =
        remember(uiState.appSections, scrubbingLetter) {
            buildList {
                if (scrubbingLetter != null) {
                    val apps = uiState.appSections[scrubbingLetter] ?: emptyList()
                    apps.forEach { add(HomeScreenItem.App(it)) }
                }
            }
        }

    val nestedScrollConnection =
        remember {
            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                var overscrollAccumulator = 0f

                override fun onPostScroll(
                    consumed: androidx.compose.ui.geometry.Offset,
                    available: androidx.compose.ui.geometry.Offset,
                    source: NestedScrollSource,
                ): androidx.compose.ui.geometry.Offset {
                    if (available.y > 0 &&
                        favoritesListState.firstVisibleItemIndex == 0 &&
                        favoritesListState.firstVisibleItemScrollOffset == 0
                    ) {
                        overscrollAccumulator += available.y
                        if (overscrollAccumulator > 50f) {
                            onEvent(HomeEvent.ExpandNotifications)
                            overscrollAccumulator = 0f
                            return androidx.compose.ui.geometry
                                .Offset(0f, available.y)
                        }
                    } else if (available.y < 0) {
                        overscrollAccumulator = 0f
                    }
                    return androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }

    BackHandler(enabled = showActionsSheet != null || uiState.currentSurface != LauncherSurface.HOME) {
        if (showActionsSheet != null) {
            showActionsSheet = null
        } else {
            onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
        }
    }

    ThemeBackground(themeType = uiState.theme) {
        val isGlass = uiState.theme == ThemeType.GLASSMORPHISM

        Surface(
            modifier = Modifier.fillMaxSize(),
            color =
                if (uiState.theme == ThemeType.MATERIAL_YOU) {
                    MaterialTheme.colorScheme.background
                } else {
                    Color.Transparent
                },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -20f && uiState.currentSurface == LauncherSurface.HOME) {
                                    onEvent(HomeEvent.UpdateScrubberLetter(null))
                                    onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH))
                                }
                            }
                        },
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    // Only show Home content if surface is HOME
                    if (uiState.currentSurface == LauncherSurface.HOME) {
                        // Two separate LazyColumns to preserve scroll position of favorites
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Favorites List
                            if (!isScrubbing) {
                                LazyColumn(
                                    state = favoritesListState,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .nestedScroll(nestedScrollConnection),
                                ) {
                                    item {
                                        Spacer(modifier = Modifier.height(LongboiSpacing.XXXL + LongboiSpacing.XXXL))
                                        AnimatedVisibility(
                                            visible = !isScrubbing,
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                        ) {
                                            GlanceHeader(
                                                data = uiState.glanceData,
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(LongboiSpacing.XXXL))
                                    }

                                    items(
                                        items = favoritesFlatList,
                                        key = { item ->
                                            when (item) {
                                                is HomeScreenItem.Favorite ->
                                                    "fav_${item.entry.appEntry.packageName}_${item.entry.appEntry.userSerialNumber}"
                                                is HomeScreenItem.EmptyFavorites -> "empty_favorites"
                                                is HomeScreenItem.App -> "app_${item.entry.packageName}"
                                            }
                                        },
                                    ) { item ->
                                        when (item) {
                                            is HomeScreenItem.Favorite -> {
                                                FavoriteAppItem(
                                                    favorite = item.entry,
                                                    onClick = { onEvent(HomeEvent.LaunchApp(item.entry.appEntry)) },
                                                    onLongClick = { showActionsSheet = item.entry.appEntry },
                                                    modifier = Modifier.padding(vertical = LongboiSpacing.XS),
                                                )
                                            }
                                            is HomeScreenItem.EmptyFavorites -> {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(200.dp)
                                                            .clickable {
                                                                onEvent(HomeEvent.UpdateScrubberLetter(null))
                                                                onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH))
                                                            },
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = "Tap or swipe up to search",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = contentColor.copy(alpha = 0.5f),
                                                    )
                                                }
                                            }
                                            else -> {}
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(LongboiSpacing.XXL))
                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onEvent(HomeEvent.NavigateTo(LauncherSurface.SETTINGS))
                                                    }.padding(
                                                        vertical = LongboiSpacing.L,
                                                        horizontal = LongboiSpacing.L,
                                                    ),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Launcher Settings",
                                                tint = contentColor,
                                                modifier = Modifier.padding(end = LongboiSpacing.M),
                                            )
                                            Text(
                                                text = "Launcher Settings",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = contentColor,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(LongboiSpacing.XXXL + LongboiSpacing.XXXL))
                                    }
                                }
                            } else {
                                // Scrubbed List - No Clock Header here
                                LazyColumn(
                                    state = scrubbedListState,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    item {
                                        // Keep top spacer but remove GlanceHeader
                                        Spacer(
                                            modifier =
                                                Modifier.height(
                                                    LongboiSpacing.XXXL + LongboiSpacing.XXXL + 100.dp,
                                                ),
                                        )
                                    }

                                    items(
                                        items = scrubbedFlatList,
                                        key = { item ->
                                            when (item) {
                                                is HomeScreenItem.App -> {
                                                    val pkg = item.entry.packageName
                                                    val serial = item.entry.userSerialNumber
                                                    "scrub_app_${pkg}_$serial"
                                                }
                                                else -> item.hashCode()
                                            }
                                        },
                                    ) { item ->
                                        if (item is HomeScreenItem.App) {
                                            AppListItem(
                                                app = item.entry,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clickable { onEvent(HomeEvent.LaunchApp(item.entry)) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Premium Bottom Search Bar
                        if (!isScrubbing) {
                            GlassCard(
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 32.dp, start = 48.dp, end = 48.dp)
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clickable {
                                            onEvent(HomeEvent.NavigateTo(LauncherSurface.SEARCH))
                                        },
                                cornerRadius = 28.dp,
                                backgroundAlpha = 0.15f,
                                blurRadius = 16.dp,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = contentColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Search apps...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = contentColor.copy(alpha = 0.4f),
                                    )
                                }
                            }
                        }

                        // Curved alphabet scrubber
                        CompactCurvedAlphabetScrubber(
                            letters = uiState.sectionIndices.keys.toList(),
                            currentLetter = scrubbingLetter ?: "A",
                            onHapticTick = { hapticFeedbackManager.tick(it) },
                            onScrubStateChanged = { active, letter ->
                                isScrubbing = active
                                if (letter != null) {
                                    scrubbingLetter = letter
                                    onEvent(HomeEvent.UpdateScrubberLetter(letter))
                                }
                            },
                            onLetterSelected = { letter ->
                                scrubbingLetter = letter
                                onEvent(HomeEvent.UpdateScrubberLetter(letter))
                            },
                            onLetterConfirmed = { letter ->
                                onEvent(HomeEvent.UpdateScrubberLetter(letter))
                                onEvent(HomeEvent.NavigateTo(LauncherSurface.ALL_APPS))
                            },
                            modifier =
                                Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                                    .padding(
                                        top = LongboiSpacing.XXXL + LongboiSpacing.XXXL,
                                        bottom = LongboiSpacing.XXL,
                                        end = LongboiSpacing.S,
                                    ).width(LongboiSpacing.XXL + LongboiSpacing.XXXL)
                                    .testTag("alphabet_scrubber"),
                        )

                        // Accent letter bubble — shown only while actively scrubbing.
                        AnimatedVisibility(
                            visible = isScrubbing,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(end = LongboiSpacing.XXL + LongboiSpacing.XXL),
                            enter = fadeIn() + scaleIn(initialScale = 0.7f),
                            exit = fadeOut() + scaleOut(targetScale = 0.7f),
                        ) {
                            scrubbingLetter?.let { letter ->
                                FloatingLetterIndicator(
                                    letter = letter,
                                    modifier = Modifier.testTag("floating_letter_indicator"),
                                )
                            }
                        }
                    }
                }

                // Long-press context sheet
                showActionsSheet?.let { app ->
                    val isFav =
                        uiState.favorites.any {
                            it.appEntry.packageName == app.packageName &&
                                it.appEntry.userSerialNumber == app.userSerialNumber
                        }
                    ActionsSheet(
                        app = app,
                        isFavorite = isFav,
                        onDismiss = { showActionsSheet = null },
                        onAddToFavorites = {
                            onEvent(HomeEvent.AddToFavorites(app))
                            showActionsSheet = null
                        },
                        onRemoveFromFavorites = {
                            uiState.favorites
                                .find {
                                    it.appEntry.packageName == app.packageName &&
                                        it.appEntry.userSerialNumber == app.userSerialNumber
                                }?.let {
                                    onEvent(HomeEvent.RemoveFromFavorites(it.id))
                                }
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
    data class Favorite(
        val entry: FavoriteEntry,
    ) : HomeScreenItem()

    object EmptyFavorites : HomeScreenItem()

    data class App(
        val entry: AppEntry,
    ) : HomeScreenItem()
}
