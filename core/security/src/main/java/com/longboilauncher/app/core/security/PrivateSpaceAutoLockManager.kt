package com.longboilauncher.app.core.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.longboilauncher.app.PrivateSpaceAutoLockPolicy
import com.longboilauncher.app.core.settings.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class PrivateSpaceAutoLockManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val preferencesRepository: PreferencesRepository,
        private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    ) {
        private val screenOffReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                        handleScreenOff()
                    }
                }
            }

        fun startMonitoring() {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            context.registerReceiver(screenOffReceiver, filter)
        }

        fun stopMonitoring() {
            try {
                context.unregisterReceiver(screenOffReceiver)
            } catch (e: Exception) {
                // Already unregistered or not registered
            }
        }

        private fun handleScreenOff() {
            scope.launch {
                val policy = preferencesRepository.privateSpaceAutoLockPolicy.first()
                if (policy == PrivateSpaceAutoLockPolicy.ON_SCREEN_OFF ||
                    policy == PrivateSpaceAutoLockPolicy.IMMEDIATELY
                ) {
                    preferencesRepository.setPrivateSpaceUnlocked(false)
                }
            }
        }

        fun onAppPaused() {
            scope.launch {
                val policy = preferencesRepository.privateSpaceAutoLockPolicy.first()
                if (policy == PrivateSpaceAutoLockPolicy.IMMEDIATELY) {
                    preferencesRepository.setPrivateSpaceUnlocked(false)
                }
            }
        }
    }
