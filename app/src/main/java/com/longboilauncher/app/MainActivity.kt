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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.metrics.performance.JankStats
import com.longboilauncher.app.core.common.LauncherRoleHandler
import com.longboilauncher.app.core.common.LauncherRoleManager
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.settings.HapticFeedbackManager
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @javax.inject.Inject
    lateinit var hapticFeedbackManager: HapticFeedbackManager

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

            val homeViewModel: HomeViewModel = hiltViewModel()
            val theme by homeViewModel.uiState.collectAsStateWithLifecycle()
            LongboiLauncherTheme(themeType = theme.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    LauncherApp(
                        hapticFeedbackManager = hapticFeedbackManager,
                        homeViewModel = homeViewModel,
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
    roleManager: LauncherRoleManager = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Handle launcher role
    LauncherRoleHandler(roleManager)

    // Handle back button
    BackHandler(enabled = uiState.currentSurface != LauncherSurface.HOME) {
        homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
    }

    // Unified Home + All Apps screen
    HomeScreen(
        uiState = uiState,
        onEvent = homeViewModel::onEvent,
        hapticFeedbackManager = hapticFeedbackManager,
    )

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
