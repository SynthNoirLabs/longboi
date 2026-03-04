package com.longboilauncher.feature.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
)

sealed class BackupEvent {
    data class ExportBackup(
        val uri: Uri,
    ) : BackupEvent()

    data class ImportBackup(
        val uri: Uri,
    ) : BackupEvent()
}

sealed class BackupEffect {
    data class ShowMessage(
        @StringRes val messageRes: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : BackupEffect()

    data object ExportSuccess : BackupEffect()

    data object ImportSuccess : BackupEffect()
}

@HiltViewModel
class BackupViewModel
    @Inject
    constructor(
        private val backupManager: BackupManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BackupState())
        val uiState: StateFlow<BackupState> = _uiState.asStateFlow()

        private val _effects = MutableSharedFlow<BackupEffect>()
        val effects: SharedFlow<BackupEffect> = _effects.asSharedFlow()

        fun onEvent(event: BackupEvent) {
            when (event) {
                is BackupEvent.ExportBackup -> exportBackup(event.uri)
                is BackupEvent.ImportBackup -> importBackup(event.uri)
            }
        }

        private fun exportBackup(uri: Uri) {
            viewModelScope.launch {
                _uiState.update { it.copy(isExporting = true) }
                backupManager
                    .exportToUri(uri)
                    .onSuccess {
                        _effects.emit(BackupEffect.ShowMessage(R.string.backup_export_success))
                        _effects.emit(BackupEffect.ExportSuccess)
                    }.onFailure { e ->
                        _effects.emit(
                            BackupEffect.ShowMessage(
                                R.string.backup_export_failed,
                                listOf(e.message.orEmpty()),
                            ),
                        )
                    }
                _uiState.update { it.copy(isExporting = false) }
            }
        }

        private fun importBackup(uri: Uri) {
            viewModelScope.launch {
                _uiState.update { it.copy(isImporting = true) }
                backupManager
                    .importFromUri(uri)
                    .onSuccess {
                        _effects.emit(BackupEffect.ShowMessage(R.string.backup_import_success))
                        _effects.emit(BackupEffect.ImportSuccess)
                    }.onFailure { e ->
                        _effects.emit(
                            BackupEffect.ShowMessage(
                                R.string.backup_import_failed,
                                listOf(e.message.orEmpty()),
                            ),
                        )
                    }
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

/**
 * Stateful route that owns the ViewModel, SAF launchers, and Snackbar host.
 *
 * String resources are resolved in the composable scope (via [stringResource]) by storing the
 * incoming [BackupEffect.ShowMessage] as state and deriving the resolved string from it — the
 * Compose-idiomatic way to use string resources inside a [LaunchedEffect] without reaching for
 * [LocalContext].
 */
@Composable
fun BackupRoute(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Pending message stored as state so stringResource() can resolve it composably
    var pendingMessage by remember { mutableStateOf<BackupEffect.ShowMessage?>(null) }
    val resolvedMessage =
        pendingMessage?.let { effect ->
            if (effect.formatArgs.isEmpty()) {
                stringResource(effect.messageRes)
            } else {
                stringResource(effect.messageRes, *effect.formatArgs.toTypedArray())
            }
        }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(BackupManager.BACKUP_MIME_TYPE),
        ) { uri ->
            uri?.let { viewModel.onEvent(BackupEvent.ExportBackup(it)) }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let { viewModel.onEvent(BackupEvent.ImportBackup(it)) }
        }

    // Collect effects — only set state, never resolve strings here
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BackupEffect.ShowMessage -> pendingMessage = effect
                BackupEffect.ExportSuccess -> {}
                BackupEffect.ImportSuccess -> {}
            }
        }
    }

    // Show snackbar once the composable has resolved the string
    LaunchedEffect(resolvedMessage) {
        resolvedMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            pendingMessage = null
        }
    }

    BackupScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onExportClick = { exportLauncher.launch(BackupManager.BACKUP_FILE_NAME) },
        onImportClick = { importLauncher.launch(arrayOf(BackupManager.BACKUP_MIME_TYPE)) },
        onNavigateBack = onNavigateBack,
    )
}

@Preview(showBackground = true)
@Composable
private fun BackupScreenPreview() {
    com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme {
        BackupScreen(
            state = BackupState(),
            snackbarHostState = SnackbarHostState(),
            onExportClick = {},
            onImportClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupScreenExportingPreview() {
    com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme {
        BackupScreen(
            state = BackupState(isExporting = true),
            snackbarHostState = SnackbarHostState(),
            onExportClick = {},
            onImportClick = {},
            onNavigateBack = {},
        )
    }
}

/**
 * Stateless screen composable. Does not reference the ViewModel — all data and actions
 * are passed as parameters, making it straightforward to preview and test in isolation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    state: BackupState,
    snackbarHostState: SnackbarHostState,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
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
                    title = { Text(stringResource(R.string.backup_title)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.backup_back),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(LongboiSpacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.backup_heading),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(LongboiSpacing.S))

                Text(
                    text = stringResource(R.string.backup_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(LongboiSpacing.XXL))

                Button(
                    onClick = onExportClick,
                    enabled = !state.isExporting && !state.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(LongboiSpacing.S))
                    Text(stringResource(R.string.backup_export_button))
                }

                Spacer(modifier = Modifier.height(LongboiSpacing.M))

                OutlinedButton(
                    onClick = onImportClick,
                    enabled = !state.isExporting && !state.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(LongboiSpacing.S))
                    Text(stringResource(R.string.backup_restore_button))
                }

                Spacer(modifier = Modifier.height(LongboiSpacing.XXL))

                Text(
                    text = stringResource(R.string.backup_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
