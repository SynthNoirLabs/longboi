package com.longboilauncher.app

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.metrics.performance.JankStats
import com.longboilauncher.app.core.common.HapticFeedbackManager
import com.longboilauncher.app.core.common.LauncherRoleHandler
import com.longboilauncher.app.core.common.LauncherRoleHelper
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.model.ThemeType
import com.longboilauncher.app.core.settings.PreferencesRepository
import com.longboilauncher.app.feature.allapps.AllAppsEvent
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
            val context = LocalContext.current
            val wallpaperManager = remember { WallpaperManager.getInstance(context) }
            val systemDarkTheme = isSystemInDarkTheme()
            
            // Smart wallpaper luminance detection
            var isWallpaperDark by remember { mutableStateOf(systemDarkTheme) }

            DisposableEffect(wallpaperManager) {
                val handler = Handler(Looper.getMainLooper())
                val listener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    WallpaperManager.OnColorsChangedListener { colors, _ ->
                        if (colors != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                isWallpaperDark = (colors.colorHints and WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0
                            } else {
                                val primary = colors.primaryColor.toArgb()
                                val luminance = 0.299 * android.graphics.Color.red(primary) + 
                                               0.587 * android.graphics.Color.green(primary) + 
                                               0.114 * android.graphics.Color.blue(primary)
                                isWallpaperDark = luminance < 128
                            }
                        }
                    }
                } else null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && listener != null) {
                    val currentColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                    if (currentColors != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            isWallpaperDark = (currentColors.colorHints and WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0
                        } else {
                            val primary = currentColors.primaryColor.toArgb()
                            val luminance = 0.299 * android.graphics.Color.red(primary) + 
                                           0.587 * android.graphics.Color.green(primary) + 
                                           0.114 * android.graphics.Color.blue(primary)
                            isWallpaperDark = luminance < 128
                        }
                    }
                    wallpaperManager.addOnColorsChangedListener(listener, handler)
                }

                onDispose {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && listener != null) {
                        wallpaperManager.removeOnColorsChangedListener(listener)
                    }
                }
            }

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
            
            val finalDarkTheme = when (uiState.theme) {
                ThemeType.MATERIAL_YOU -> isWallpaperDark
                ThemeType.SOPHISTICATED_SLEEK -> true
                ThemeType.MODERN_MINIMALIST -> false
                else -> isWallpaperDark
            }

            LongboiLauncherTheme(
                themeType = uiState.theme,
                darkTheme = finalDarkTheme
            ) {
                ThemeBackground(themeType = uiState.theme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
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
}

import com.longboilauncher.app.feature.onboarding.OnboardingScreen

@Composable
fun LauncherApp(
    hapticFeedbackManager: HapticFeedbackManager,
    homeViewModel: HomeViewModel,
    roleHelper: LauncherRoleHelper,
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    LauncherRoleHandler(roleHelper)

    BackHandler(enabled = uiState.currentSurface != LauncherSurface.HOME && uiState.currentSurface != LauncherSurface.ONBOARDING) {
        homeViewModel.onEvent(HomeEvent.NavigateTo(LauncherSurface.HOME))
    }

    if (uiState.currentSurface == LauncherSurface.ONBOARDING) {
        OnboardingScreen(
            onComplete = { homeViewModel.onEvent(HomeEvent.CompleteOnboarding) },
            onRequestDefaultLauncher = { roleHelper.requestRole() }
        )
    } else {
        HomeScreen(
            uiState = uiState,
            onEvent = homeViewModel::onEvent,
            hapticFeedbackManager = hapticFeedbackManager,
        )
    }

    AnimatedVisibility(
        visible = uiState.currentSurface == LauncherSurface.ALL_APPS,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val allAppsViewModel: AllAppsViewModel = hiltViewModel()
        val allAppsState by allAppsViewModel.uiState.collectAsStateWithLifecycle()

        // Apply target letter from Home scrubber if available
        LaunchedEffect(uiState.currentSurface, uiState.scrubberLetter) {
            if (uiState.currentSurface == LauncherSurface.ALL_APPS) {
                allAppsViewModel.onEvent(AllAppsEvent.SetTargetLetter(uiState.scrubberLetter))
            }
        }

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
