package com.longboilauncher.app.feature.allapps

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.settings.HapticFeedbackManager
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

    // Build flat list with headers
    val flatList =
        remember(uiState.appSections) {
            buildList {
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

    // Update current letter based on scroll position
    LaunchedEffect(listState.firstVisibleItemIndex) {
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
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // App List
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(end = 36.dp),
            ) {
                items(
                    items = flatList,
                    key = { item ->
                        when (item) {
                            is ListItem.Header -> "header_${item.letter}"
                            is ListItem.App -> "${item.app.packageName}_${item.app.userIdentifier}"
                        }
                    },
                    contentType = { item ->
                        when (item) {
                            is ListItem.Header -> "header"
                            is ListItem.App -> "app"
                        }
                    },
                ) { item ->
                    when (item) {
                        is ListItem.Header -> {
                            SectionHeader(
                                letter = item.letter,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        is ListItem.App -> {
                            AppListItem(
                                app = item.app,
                                onClick = { onAppSelected(item.app) },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            // Alphabet Scrubber
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
                    val index = uiState.sectionIndices[letter] ?: return@AlphabetScrubber
                    scrollJob?.cancel()
                    scrollJob =
                        coroutineScope.launch {
                            if (isScrubbing) {
                                listState.scrollToItem(index)
                            } else {
                                listState.animateScrollToItem(index)
                            }
                        }
                },
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(32.dp)
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
                                .padding(end = 64.dp)
                                .testTag("floating_letter_indicator"),
                    )
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
    modifier: Modifier = Modifier,
) {
    Text(
        text = letter,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun AlphabetScrubber(
    letters: List<String>,
    currentLetter: String,
    hapticFeedbackManager: HapticFeedbackManager,
    onScrubStateChanged: (active: Boolean, letter: String?) -> Unit,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current

    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                ).padding(horizontal = 4.dp, vertical = 8.dp)
                .pointerInput(letters) {
                    if (letters.isEmpty()) return@pointerInput

                    var lastIndex = -1

                    fun selectIndex(index: Int) {
                        if (index == lastIndex) return
                        lastIndex = index
                        val letter = letters[index]
                        onScrubStateChanged(true, letter)
                        hapticFeedbackManager.tick(view)
                        onLetterSelected(letter)
                    }

                    fun yToIndex(y: Float): Int =
                        scrubberIndexForY(
                            y = y,
                            height = size.height.toFloat(),
                            itemCount = letters.size,
                        )

                    detectDragGestures(
                        onDragStart = { offset ->
                            selectIndex(yToIndex(offset.y))
                        },
                        onDragCancel = {
                            onScrubStateChanged(false, null)
                        },
                        onDragEnd = {
                            onScrubStateChanged(false, null)
                        },
                        onDrag = { change, _ ->
                            selectIndex(yToIndex(change.position.y))
                        },
                    )
                },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val activeIndex = letters.indexOf(currentLetter).takeIf { it >= 0 } ?: 0

            letters.forEachIndexed { index, letter ->
                val distance = kotlin.math.abs(index - activeIndex)
                val waveStrength = (1f - (distance / 6f)).coerceIn(0f, 1f)
                val offsetX by animateDpAsState(
                    targetValue = (-10f * waveStrength).dp,
                    label = "scrubberWaveOffset",
                )

                Text(
                    text = letter,
                    fontSize = (10f + (2f * waveStrength)).sp,
                    fontWeight = if (letter == currentLetter) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (letter == currentLetter) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .offset(x = offsetX)
                            .clickable {
                                onScrubStateChanged(true, letter)
                                hapticFeedbackManager.tick(view)
                                onLetterSelected(letter)
                                onScrubStateChanged(false, null)
                            },
                )
            }
        }
    }
}

@Composable
private fun FloatingLetterIndicator(
    letter: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
