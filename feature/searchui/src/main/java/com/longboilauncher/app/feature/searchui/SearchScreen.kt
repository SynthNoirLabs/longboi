package com.longboilauncher.app.feature.searchui

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.longboilauncher.app.core.designsystem.components.AppListItem
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding(),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(SearchEvent.UpdateSearchQuery(it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Search apps...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(SearchEvent.UpdateSearchQuery("")) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Search,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            if (uiState.searchResults.isNotEmpty()) {
                                when (val first = uiState.searchResults.first()) {
                                    is SearchResult.AppResult -> onAppSelected(first.app)
                                    is SearchResult.ShortcutResult ->
                                        onEvent(
                                            SearchEvent.LaunchShortcut(first.app, first.shortcutId),
                                        )
                                    is SearchResult.CalculatorResult -> { /* No action on search */ }
                                    is SearchResult.SettingsShortcutResult ->
                                        onEvent(
                                            SearchEvent.OpenSettings(first.destination),
                                        )
                                }
                            }
                        },
                    ),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            if (uiState.searchQuery.isNotEmpty()) {
                if (uiState.searchResults.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No results",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(
                            items = uiState.searchResults,
                            key = { result ->
                                when (result) {
                                    is SearchResult.AppResult ->
                                        "app_${result.app.packageName}_${result.app.userIdentifier}"
                                    is SearchResult.ShortcutResult ->
                                        "shortcut_${result.app.packageName}_${result.shortcutId}"
                                    is SearchResult.CalculatorResult ->
                                        "calc_${result.expression}"
                                    is SearchResult.SettingsShortcutResult ->
                                        "settings_${result.destination}"
                                }
                            },
                        ) { result ->
                            when (result) {
                                is SearchResult.AppResult -> {
                                    AppListItem(
                                        app = result.app,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    keyboardController?.hide()
                                                    onAppSelected(result.app)
                                                },
                                    )
                                }
                                is SearchResult.ShortcutResult -> {
                                    // TODO: Implement shortcut UI
                                }
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
            } else {
                // Show hint when empty
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Type to search apps, settings, or calculate",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
    androidx.compose.foundation.layout.Row(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = expression,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "= $resultValue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
    androidx.compose.foundation.layout.Row(
        modifier =
            modifier
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
