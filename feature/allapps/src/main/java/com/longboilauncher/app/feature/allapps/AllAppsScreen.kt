package com.longboilauncher.app.feature.allapps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.components.CompactCurvedAlphabetScrubber
import com.longboilauncher.app.core.designsystem.components.FloatingLetterIndicator
import com.longboilauncher.app.core.designsystem.components.GlassSurface
import com.longboilauncher.app.core.designsystem.components.AmbientLightBackground
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.designsystem.theme.LocalThemeType
import com.longboilauncher.app.core.designsystem.effects.StaggeredSlideIn
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.ThemeType
import com.longboilauncher.app.core.common.HapticFeedbackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun AllAppsScreen(
    uiState: AllAppsState,
    onEvent: (AllAppsEvent) -> Unit,
    onAppSelected: (AppEntry) -> Unit,
    onDismiss: () -> Unit,
    hapticFeedbackManager: HapticFeedbackManager,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentLetter by remember { mutableStateOf("A") }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubbingLetter by remember { mutableStateOf<String?>(null) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    val isGlass = LocalThemeType.current == ThemeType.GLASSMORPHISM

    // Build flat list with headers, filtering if scrubbing OR if a target letter is set
    val flatList =
        remember(uiState.appSections, isScrubbing, scrubbingLetter, uiState.targetLetter, uiState.searchQuery) {
            val filter = if (isScrubbing) scrubbingLetter else uiState.targetLetter
            buildList {
                if (uiState.searchQuery.isNotEmpty()) {
                    // Search mode: show all results (flat list handled by VM filtering already)
                    uiState.appSections.forEach { (letter, apps) ->
                        if (apps.isNotEmpty()) {
                            add(ListItem.Header(letter))
                            apps.forEach { add(ListItem.App(it)) }
                        }
                    }
                } else if (filter != null) {
                    // Filtered mode: show only apps for the selected letter
                    val apps = uiState.appSections[filter] ?: emptyList()
                    if (apps.isNotEmpty()) {
                        add(ListItem.Header(filter))
                        apps.forEach { add(ListItem.App(it)) }
                    }
                } else {
                    // Full list mode
                    uiState.appSections.forEach { (letter, apps) ->
                        if (apps.isNotEmpty()) {
                            add(ListItem.Header(letter))
                            apps.forEach { app ->
                                add(ListItem.App(app))
                            }
                        }
                    }
                }
            }
        }

    // Handle initial scroll to target letter (only when full list)
    LaunchedEffect(uiState.targetLetter) {
        if (uiState.targetLetter != null && uiState.searchQuery.isEmpty()) {
            // If we are filtering to a single letter, we always want to be at the top
            listState.scrollToItem(0)
        }
    }

    // Update current letter based on scroll position (only in full list mode)
    LaunchedEffect(listState.firstVisibleItemIndex, isScrubbing, uiState.targetLetter, uiState.searchQuery) {
        if (!isScrubbing && uiState.targetLetter == null && uiState.searchQuery.isEmpty()) {
            val index = listState.firstVisibleItemIndex
            val item = flatList.getOrNull(index)
            currentLetter =
                when (item) {
                    is ListItem.Header -> item.letter
                    is ListItem.App ->
                        item.app.label
                            .firstOrNull()
                            ?.uppercaseChar()
                            ?.toString() ?: "A"
                    null -> "A"
                }
        } else {
            val letter = scrubbingLetter ?: uiState.targetLetter
            if (letter != null) {
                currentLetter = letter
            }
        }
    }

    AmbientLightBackground(modifier = Modifier.fillMaxSize()) {
        GlassSurface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Minimalist, borderless Search Bar
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { 
                        onEvent(AllAppsEvent.UpdateSearchQuery(it))
                        if (it.isNotEmpty()) {
                            onEvent(AllAppsEvent.SetTargetLetter(null))
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = com.longboilauncher.core.designsystem.R.string.search_apps_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.6f),
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(AllAppsEvent.UpdateSearchQuery("")) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.6f),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                )
                
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                ) {
                    // App List
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(end = 48.dp), // More room for the premium scrubber
                    ) {
                        itemsIndexed(
                            items = flatList,
                            key = { _, item ->
                                when (item) {
                                    is ListItem.Header -> "header_${item.letter}"
                                    is ListItem.App -> "${item.app.packageName}_${item.app.userSerialNumber}"
                                }
                            },
                            contentType = { _, item ->
                                when (item) {
                                    is ListItem.Header -> "header"
                                    is ListItem.App -> "app"
                                }
                            },
                        ) { index, item ->
                            StaggeredSlideIn(
                                index = index,
                                totalItems = flatList.size,
                            ) {
                                when (item) {
                                    is ListItem.Header -> {
                                        SectionHeader(
                                            letter = item.letter,
                                            isGlass = isGlass,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = LongboiSpacing.ScreenEdgePadding, top = 16.dp, bottom = 8.dp),
                                        )
                                    }
                                    is ListItem.App -> {
                                        AppListItem(
                                            app = item.app,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onAppSelected(item.app) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Premium Alphabet Scrubber
                    CompactCurvedAlphabetScrubber(
                        letters = uiState.sectionIndices.keys.toList(),
                        currentLetter = scrubbingLetter ?: uiState.targetLetter ?: currentLetter,
                        onHapticTick = { hapticFeedbackManager.tick(it) },
                        onScrubStateChanged = { active, letter ->
                            isScrubbing = active
                            scrubbingLetter = letter
                            if (active) {
                                onEvent(AllAppsEvent.SetTargetLetter(null)) // Exit single-letter mode when scrubbing starts
                            }
                        },
                        onLetterSelected = { letter ->
                            scrubbingLetter = letter
                            val index = uiState.sectionIndices[letter] ?: return@CompactCurvedAlphabetScrubber
                            scrollJob?.cancel()
                            scrollJob =
                                coroutineScope.launch {
                                    listState.scrollToItem(index)
                                }
                        },
                        onLetterConfirmed = { letter ->
                            onEvent(AllAppsEvent.SetTargetLetter(letter))
                        },
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(56.dp) // Slightly wider for easier reach
                                .padding(vertical = 48.dp)
                                .testTag("alphabet_scrubber"),
                    )

                    if (isScrubbing) {
                        scrubbingLetter?.let { letter ->
                            FloatingLetterIndicator(
                                letter = letter,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .padding(end = 80.dp)
                                        .testTag("floating_letter_indicator"),
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed class ListItem {
    data class Header(
        val letter: String,
    ) : ListItem()

    data class App(
        val app: AppEntry,
    ) : ListItem()
}

@Composable
private fun SectionHeader(
    letter: String,
    isGlass: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        text = letter,
        style = if (isGlass) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleLarge,
        fontWeight = if (isGlass) FontWeight.Normal else FontWeight.Bold,
        color = if (isGlass) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
