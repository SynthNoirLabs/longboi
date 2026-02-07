package com.longboilauncher.app.core.model

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class ShortcutUiModel(
    val id: String,
    val label: String,
    val iconUri: Uri?,
    val intent: Intent?,
)
