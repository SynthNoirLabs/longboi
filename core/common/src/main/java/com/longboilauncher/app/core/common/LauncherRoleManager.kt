package com.longboilauncher.app.core.common

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.settings.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LauncherRoleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

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
                viewModelScope.launch {
                    // Only prompt if we haven't checked recently or if user dismissed
                    // This is a simplified check
                    _shouldRequestRole.value = true
                }
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
