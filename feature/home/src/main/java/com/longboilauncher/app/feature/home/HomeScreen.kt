package com.longboilauncher.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.ActionsSheet
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.components.CompactCurvedAlphabetScrubber
import com.longboilauncher.app.core.designsystem.components.FavoriteAppItem
import com.longboilauncher.app.core.designsystem.components.FloatingLetterIndicator
import com.longboilauncher.app.core.designsystem.components.GlanceHeader
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.settings.HapticFeedbackManager
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
    // Intentionally never reset to null — holds the last letter so the AnimatedVisibility
    // exit animation still has content to render while fading/scaling out.
    var lastScrubbingLetter by remember { mutableStateOf<String?>(null) }

    // Build the unified flat list: Favorites → section-header anchors → Apps
    val flatList =
        remember(uiState.favorites, uiState.appSections) {
            buildList {
                if (uiState.favorites.isNotEmpty()) {
                    uiState.favorites.forEach { add(HomeScreenItem.Favorite(it)) }
                    add(HomeScreenItem.Divider(LongboiSpacing.XXXL))
                } else {
                    add(HomeScreenItem.EmptyFavorites)
                    add(HomeScreenItem.Divider(LongboiSpacing.XXL))
                }
                uiState.appSections.forEach { (letter, apps) ->
                    if (apps.isNotEmpty()) {
                        add(HomeScreenItem.Header(letter))
                        apps.forEach { add(HomeScreenItem.App(it, letter)) }
                    }
                }
            }
        }

    val isAllAppsVisible =
        remember(listState.firstVisibleItemIndex) {
            val firstItem = flatList.getOrNull(listState.firstVisibleItemIndex)
            firstItem !is HomeScreenItem.Favorite && firstItem !is HomeScreenItem.EmptyFavorites
        }

    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                var overscrollAccumulator = 0f

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y > 0 &&
                        listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0
                    ) {
                        overscrollAccumulator += available.y
                        if (overscrollAccumulator > 50f) {
                            onEvent(HomeEvent.ExpandNotifications)
                            overscrollAccumulator = 0f
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

    BackHandler(enabled = showActionsSheet != null || uiState.currentSurface != LauncherSurface.HOME) {
        if (showActionsSheet != null) {
            showActionsSheet = null
        } else {
            coroutineScope.launch { listState.animateScrollToItem(0) }
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
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection),
                    ) {
                        item {
                            // Top inset + clock/date header
                            Spacer(modifier = Modifier.height(LongboiSpacing.XXXL + LongboiSpacing.XXXL))
                            GlanceHeader(
                                data = uiState.glanceData,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(LongboiSpacing.XXXL))
                        }

                        items(
                            items = flatList,
                            key = { item ->
                                when (item) {
                                    is HomeScreenItem.Favorite ->
                                        "fav_${item.entry.appEntry.packageName}_${item.entry.appEntry.userIdentifier}"
                                    is HomeScreenItem.EmptyFavorites -> "empty_favorites"
                                    is HomeScreenItem.App ->
                                        "app_${item.app.packageName}_${item.app.userIdentifier}"
                                    is HomeScreenItem.Header -> "header_${item.letter}"
                                    is HomeScreenItem.Divider -> "divider_${item.height.value}"
                                }
                            },
                        ) { item ->
                            val isVisible =
                                !isScrubbing ||
                                    when (item) {
                                        is HomeScreenItem.App -> item.sectionLetter == currentLetter
                                        is HomeScreenItem.Header -> item.letter == currentLetter
                                        else -> false
                                    }

                            if (isVisible) {
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
                                        // Invisible spacer to pad out the "Favorites" section when empty
                                        // so that scrolling down requires a small swipe to reveal "All Apps"
                                        Spacer(modifier = Modifier.height(120.dp))
                                    }

                                    is HomeScreenItem.App -> {
                                        AppListItem(
                                            app = item.app,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .combinedClickable(
                                                        onClick = { onEvent(HomeEvent.LaunchApp(item.app)) },
                                                        onLongClick = { showActionsSheet = item.app },
                                                    ),
                                        )
                                    }

                                    // Headers are invisible anchors — the scrubber provides
                                    // all the letter navigation the user needs.
                                    is HomeScreenItem.Header -> {
                                        Spacer(modifier = Modifier.height(LongboiSpacing.XS))
                                    }

                                    is HomeScreenItem.Divider -> {
                                        Spacer(modifier = Modifier.height(item.height))
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(LongboiSpacing.XXL))
                            androidx.compose.foundation.layout.Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onEvent(HomeEvent.NavigateTo(LauncherSurface.SETTINGS)) }
                                        .padding(vertical = LongboiSpacing.L, horizontal = LongboiSpacing.L),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Launcher Settings",
                                    tint = Color.White,
                                    modifier = Modifier.padding(end = LongboiSpacing.M),
                                )
                                Text(
                                    text = "Launcher Settings",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            shadow =
                                                androidx.compose.ui.graphics.Shadow(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    offset =
                                                        androidx.compose.ui.geometry
                                                            .Offset(0f, 2f),
                                                    blurRadius = 4f,
                                                ),
                                        ),
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                )
                            }
                            Spacer(modifier = Modifier.height(LongboiSpacing.XXXL + LongboiSpacing.XXXL))
                        }
                    }
                }

                // Curved alphabet scrubber — the Niagara signature interaction
                CompactCurvedAlphabetScrubber(
                    letters = uiState.sectionIndices.keys.toList(),
                    currentLetter = currentLetter,
                    onHapticTick = { hapticFeedbackManager.tick(it) },
                    onScrubStateChanged = { active, letter ->
                        isScrubbing = active
                        // Only overwrite with a real letter; never clear to null so the
                        // AnimatedVisibility exit animation renders the last seen letter.
                        if (letter != null) lastScrubbingLetter = letter
                    },
                    onLetterSelected = { letter ->
                        currentLetter = letter
                        val indexInAllApps = uiState.sectionIndices[letter] ?: return@CompactCurvedAlphabetScrubber
                        val itemsBeforeApps = flatList.indexOfFirst { it is HomeScreenItem.Header }
                        if (itemsBeforeApps != -1) {
                            coroutineScope.launch {
                                if (isScrubbing) {
                                    listState.scrollToItem(indexInAllApps + itemsBeforeApps + 1)
                                } else {
                                    listState.animateScrollToItem(indexInAllApps + itemsBeforeApps + 1)
                                }
                            }
                        }
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
                // Visibility is gated purely on `isScrubbing`; content reads
                // `lastScrubbingLetter` which is never cleared to null, so the
                // exit animation (fadeOut + scaleOut) always has a letter to render.
                AnimatedVisibility(
                    visible = isScrubbing,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .padding(end = LongboiSpacing.XXL + LongboiSpacing.XXL),
                    enter = fadeIn() + scaleIn(initialScale = 0.7f),
                    exit = fadeOut() + scaleOut(targetScale = 0.7f),
                ) {
                    lastScrubbingLetter?.let { letter ->
                        FloatingLetterIndicator(
                            letter = letter,
                            modifier = Modifier.testTag("floating_letter_indicator"),
                        )
                    }
                }

                // Long-press context sheet
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
    data class Favorite(
        val entry: FavoriteEntry,
    ) : HomeScreenItem()

    object EmptyFavorites : HomeScreenItem()

    data class App(
        val app: AppEntry,
        val sectionLetter: String,
    ) : HomeScreenItem()

    /** Invisible anchor so the scrubber can scroll to the right list position. */
    data class Header(
        val letter: String,
    ) : HomeScreenItem()

    data class Divider(
        val height: Dp,
    ) : HomeScreenItem()
}
