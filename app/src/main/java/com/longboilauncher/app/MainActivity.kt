package com.longboilauncher.app

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
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

    /**
     * AppWidgetHost manages the lifecycle of hosted widgets on the launcher surface.
     * HOST_ID is an arbitrary but stable identifier unique to this host within the app.
     */
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize widget hosting infrastructure
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)

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

            LongboiLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    LauncherApp(hapticFeedbackManager = hapticFeedbackManager)
                }
            }
        }
    }

    /**
     * Start listening for widget updates. Without this, AppWidgetHostView instances
     * will render empty/frozen content because the host never receives RemoteViews updates
     * from the AppWidgetProvider broadcast cycle.
     */
    override fun onStart() {
        super.onStart()
        try {
            appWidgetHost.startListening()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start widget host listening", e)
        }
    }

    /**
     * Stop listening when the launcher is no longer visible to conserve resources
     * and avoid holding stale RemoteViews references.
     */
    override fun onStop() {
        super.onStop()
        try {
            appWidgetHost.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop widget host listening", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /** Stable host ID for the AppWidgetHost. Must remain constant across process restarts. */
        private const val APPWIDGET_HOST_ID = 1024
    }
}

@Composable
fun LauncherApp(
    hapticFeedbackManager: HapticFeedbackManager,
    homeViewModel: HomeViewModel = hiltViewModel(),
    roleManager: LauncherRoleManager = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Handle launcher role
    LauncherRoleHandler(roleManager)

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
