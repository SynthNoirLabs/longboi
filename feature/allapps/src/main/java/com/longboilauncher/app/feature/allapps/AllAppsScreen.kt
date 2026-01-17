package com.longboilauncher.app.feature.allapps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.feature.home.AllAppsViewModel
import kotlinx.coroutines.launch

import com.longboilauncher.app.feature.home.AllAppsState
import com.longboilauncher.app.feature.home.AllAppsEvent

@Composable
fun AllAppsScreen(
    uiState: AllAppsState,
    onEvent: (AllAppsEvent) -> Unit,
    onAppSelected: (AppEntry) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentLetter by remember { mutableStateOf("A") }

    // Build flat list with headers
    val flatList = remember(uiState.appSections) {
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
        currentLetter = when (item) {
            is ListItem.Header -> item.letter
            is ListItem.App -> item.app.label.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
            null -> "A"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // App List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 36.dp)
            ) {
                items(
                    items = flatList,
                    key = { item ->
                        when (item) {
                            is ListItem.Header -> "header_${item.letter}"
                            is ListItem.App -> "${item.app.packageName}_${item.app.userIdentifier}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is ListItem.Header -> {
                            SectionHeader(
                                letter = item.letter,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        is ListItem.App -> {
                            AppListItem(
                                app = item.app,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAppSelected(item.app) }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Alphabet Scrubber
            AlphabetScrubber(
                letters = uiState.appSections.keys.toList(),
                currentLetter = currentLetter,
                onLetterSelected = { letter ->
                    val index = uiState.sectionIndices[letter] ?: return@AlphabetScrubber
                    coroutineScope.launch {
                        listState.animateScrollToItem(index)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(32.dp)
                    .padding(vertical = 48.dp)
            )
        }
    }
}

private sealed class ListItem {
    data class Header(val letter: String) : ListItem()
    data class App(val app: AppEntry) : ListItem()
}

@Composable
private fun SectionHeader(
    letter: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = letter,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun AlphabetScrubber(
    letters: List<String>,
    currentLetter: String,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .pointerInput(letters) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (letters.isEmpty()) return@detectVerticalDragGestures
                    val itemHeight = size.height.toFloat() / letters.size
                    val currentIndex = letters.indexOf(currentLetter).coerceAtLeast(0)
                    val newIndex = (currentIndex + (dragAmount / itemHeight).toInt())
                        .coerceIn(0, letters.size - 1)
                    if (newIndex != currentIndex) {
                        onLetterSelected(letters[newIndex])
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                Text(
                    text = letter,
                    fontSize = 10.sp,
                    fontWeight = if (letter == currentLetter) FontWeight.Bold else FontWeight.Normal,
                    color = if (letter == currentLetter) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onLetterSelected(letter) }
                )
            }
        }
    }
}
