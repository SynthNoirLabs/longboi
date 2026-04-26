package com.longboilauncher.app.core.common

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class LauncherRoleHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val externalScope: CoroutineScope,
    ) {
        private val _isDefaultLauncher = MutableStateFlow(false)
        val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

        private val _shouldRequestRole = MutableStateFlow(false)
        val shouldRequestRole: StateFlow<Boolean> = _shouldRequestRole.asStateFlow()

        init {
            checkDefaultLauncher()
        }

        fun checkDefaultLauncher() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                _isDefaultLauncher.value = isHeld

                if (!isHeld) {
                    externalScope.launch {
                        _shouldRequestRole.value = true
                    }
                } else {
                    _shouldRequestRole.value = false
                }
            }
        }

        fun requestDefaultLauncher(): Intent? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
            return null
        }

        fun dismissRoleRequest() {
            _shouldRequestRole.value = false
        }
    }
