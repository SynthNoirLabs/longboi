package com.longboilauncher.app.feature.searchui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.longboilauncher.app.core.designsystem.components.AppListItem
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import com.longboilauncher.app.core.model.AppEntry

@Composable
fun SearchScreen(
    uiState: SearchState,
    onEvent: (SearchEvent) -> Unit,
    onAppSelected: (AppEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Immersive Overlay with deep blur/dim
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.85f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding(),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Minimalist Command Input
            TextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(SearchEvent.UpdateSearchQuery(it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Search apps, settings...",
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
                        IconButton(onClick = { onEvent(SearchEvent.UpdateSearchQuery("")) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.6f),
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        if (uiState.searchResults.isNotEmpty()) {
                            when (val first = uiState.searchResults.first()) {
                                is SearchResult.AppResult -> onAppSelected(first.app)
                                is SearchResult.ShortcutResult -> onEvent(SearchEvent.LaunchShortcut(first.app, first.shortcutId))
                                is SearchResult.CalculatorResult -> { /* No action */ }
                                is SearchResult.SettingsShortcutResult -> onEvent(SearchEvent.OpenSettings(first.destination))
                            }
                        }
                    }
                ),
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

            // Dynamic Results
            if (uiState.searchQuery.isNotEmpty()) {
                val groupedResults = remember(uiState.searchResults) {
                    uiState.searchResults.groupBy {
                        when(it) {
                            is SearchResult.AppResult -> "APPS"
                            is SearchResult.ShortcutResult -> "SHORTCUTS"
                            is SearchResult.CalculatorResult -> "CALCULATOR"
                            is SearchResult.SettingsShortcutResult -> "SETTINGS"
                        }
                    }
                }

                if (uiState.searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found", color = Color.White.copy(alpha = 0.4f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        groupedResults.forEach { (category, results) ->
                            item {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
                                )
                            }
                            items(results) { result ->
                                when (result) {
                                    is SearchResult.AppResult -> {
                                        AppListItem(
                                            app = result.app,
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                keyboardController?.hide()
                                                onAppSelected(result.app)
                                            },
                                        )
                                    }
                                    is SearchResult.ShortcutResult -> { /* TODO */ }
                                    is SearchResult.CalculatorResult -> {
                                        CalculatorResultItem(
                                            expression = result.expression,
                                            resultValue = result.result,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    is SearchResult.SettingsShortcutResult -> {
                                        SettingsShortcutItem(
                                            title = result.title,
                                            onClick = {
                                                keyboardController?.hide()
                                                onEvent(SearchEvent.OpenSettings(result.destination))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorResultItem(
    expression: String,
    resultValue: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = expression,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
            Text(
                text = "= $resultValue",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsShortcutItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
    }
}
