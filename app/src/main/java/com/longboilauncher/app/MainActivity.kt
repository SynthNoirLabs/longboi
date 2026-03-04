package com.longboilauncher.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.metrics.performance.JankStats
import com.longboilauncher.app.core.common.LauncherRoleHandler
import com.longboilauncher.app.core.common.LauncherRoleHelper
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.settings.PreferencesRepository
import com.longboilauncher.app.feature.allapps.AllAppsScreen
import com.longboilauncher.app.feature.allapps.AllAppsViewModel
import com.longboilauncher.app.feature.home.HomeEvent
import com.longboilauncher.app.feature.home.HomeScreen
import com.longboilauncher.app.feature.home.HomeViewModel
import com.longboilauncher.app.feature.home.LauncherSurface
import com.longboilauncher.app.feature.searchui.SearchScreen
import com.longboilauncher.app.feature.searchui.SearchViewModel
import com.longboilauncher.app.feature.settingsui.SettingsScreen
import com.longboilauncher.app.feature.settingsui.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @javax.inject.Inject
    lateinit var hapticFeedbackManager: HapticFeedbackManager

    @javax.inject.Inject
    lateinit var preferencesRepository: PreferencesRepository

    @javax.inject.Inject
    lateinit var roleHelper: LauncherRoleHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val jankStats =
                remember {
                    JankStats.createAndTrack(window) { frameData ->
                        if (frameData.isJank) {
                            Log.v("JankStats", "Jank detected: ${frameData.frameDurationUiNanos}ns")
                        }
                    }
                }

            DisposableEffect(Unit) {
                jankStats.isTrackingEnabled = true
                onDispose {
                    jankStats.isTrackingEnabled = false
                }
            }

            LaunchedEffect(hapticFeedbackManager) {
                combine(
                    preferencesRepository.hapticsEnabled,
                    preferencesRepository.reduceMotion,
                ) { hapticsEnabled, reduceMotion ->
                    hapticsEnabled && !reduceMotion
                }.onEach { hapticFeedbackManager.isEnabled = it }
                    .launchIn(this)
            }

            val homeViewModel: HomeViewModel = hiltViewModel()
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            LongboiLauncherTheme(themeType = uiState.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    LauncherApp(
                        hapticFeedbackManager = hapticFeedbackManager,
                        homeViewModel = homeViewModel,
                        roleHelper = roleHelper,
                    )
                }
            }
        }
    }
}

@Composable
fun LauncherApp(
    hapticFeedbackManager: HapticFeedbackManager,
    homeViewModel: HomeViewModel,
    roleHelper: LauncherRoleHelper,
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Handle launcher role
    LauncherRoleHandler(roleHelper)

    // Handle back button
    BackHandler(enabled = uiState.currentSurface != LauncherSurface.HOME) {
        homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
    }

    // Home screen is always rendered as the base
    HomeScreen(
        uiState = uiState,
        onEvent = homeViewModel::onEvent,
    )

    // All Apps overlay
    AnimatedVisibility(
        visible = uiState.currentSurface == LauncherSurface.ALL_APPS,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        val allAppsViewModel: AllAppsViewModel = hiltViewModel()
        val allAppsState by allAppsViewModel.uiState.collectAsStateWithLifecycle()

        AllAppsScreen(
            uiState = allAppsState,
            onEvent = allAppsViewModel::onEvent,
            onAppSelected = { appEntry ->
                homeViewModel.onEvent(HomeEvent.LaunchApp(appEntry))
                homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
            },
            onDismiss = { homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME)) },
            hapticFeedbackManager = hapticFeedbackManager,
        )
    }

    // Search overlay
    AnimatedVisibility(
        visible = uiState.currentSurface == LauncherSurface.SEARCH,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val searchViewModel: SearchViewModel = hiltViewModel()
        val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()

        SearchScreen(
            uiState = searchState,
            onEvent = searchViewModel::onEvent,
            onAppSelected = { appEntry ->
                homeViewModel.onEvent(HomeEvent.LaunchApp(appEntry))
                homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
            },
            onDismiss = { homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME)) },
        )
    }

    // Settings overlay
    AnimatedVisibility(
        visible = uiState.currentSurface == LauncherSurface.SETTINGS,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val settingsViewModel: SettingsViewModel = hiltViewModel()
        val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

        SettingsScreen(
            uiState = settingsState,
            onEvent = settingsViewModel::onEvent,
            onNavigateBack = { homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME)) },
        )
    }
}
