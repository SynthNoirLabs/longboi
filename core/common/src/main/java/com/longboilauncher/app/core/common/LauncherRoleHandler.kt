package com.longboilauncher.app.core.common

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LauncherRoleHandler(
    roleManager: LauncherRoleManager
) {
    val shouldRequestRole by roleManager.shouldRequestRole.collectAsStateWithLifecycle()

    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        roleManager.checkDefaultLauncher()
    }

    LaunchedEffect(shouldRequestRole) {
        if (shouldRequestRole) {
            val intent = roleManager.requestDefaultLauncher()
            if (intent != null) {
                roleRequestLauncher.launch(intent)
            }
            roleManager.dismissRoleRequest()
        }
    }
}
