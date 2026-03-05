package com.longboilauncher.app.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.longboilauncher.app.core.designsystem.theme.LongboiLauncherTheme
import com.longboilauncher.app.core.designsystem.theme.LongboiSpacing

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestDefaultLauncher: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToHome -> onComplete()
                is OnboardingEffect.RequestDefaultLauncher -> onRequestDefaultLauncher()
            }
        }
    }

    OnboardingContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

import com.longboilauncher.app.core.designsystem.components.GlassCard
import com.longboilauncher.app.core.designsystem.components.ThemeBackground
import com.longboilauncher.app.core.model.ThemeType

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    val pagerState =
        rememberPagerState(
            initialPage = state.currentPage,
            pageCount = { state.totalPages },
        )

    LaunchedEffect(state.currentPage) {
        pagerState.animateScrollToPage(state.currentPage)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            onEvent(OnboardingEvent.GoToPage(pagerState.currentPage))
        }
    }

    ThemeBackground(themeType = ThemeType.GLASSMORPHISM) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(LongboiSpacing.XL),
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { onEvent(OnboardingEvent.SkipOnboarding) }) {
                    Text(
                        stringResource(R.string.onboarding_skip),
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) { page ->
                OnboardingPage(page = OnboardingPages.entries[page])
            }

            // Page indicators
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = LongboiSpacing.XL),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(state.totalPages) { index ->
                    PageIndicator(
                        isSelected = index == state.currentPage,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.currentPage > 0) {
                    TextButton(onClick = { onEvent(OnboardingEvent.PreviousPage) }) {
                        Text(
                            stringResource(R.string.onboarding_back),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                GlassCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    cornerRadius = 32f,
                    backgroundAlpha = 0.4f
                ) {
                    Button(
                        onClick = {
                            if (state.isLastPage) {
                                onEvent(OnboardingEvent.CompleteOnboarding)
                            } else {
                                onEvent(OnboardingEvent.NextPage)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (state.isLastPage) {
                                stringResource(R.string.onboarding_get_started)
                            } else {
                                stringResource(R.string.onboarding_next)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(LongboiSpacing.M))
                        Icon(
                            imageVector =
                                if (state.isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    page: OnboardingPages,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(LongboiSpacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlassCard(
            modifier = Modifier.size(160.dp),
            cornerRadius = 40f,
            backgroundAlpha = 0.15f
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(LongboiSpacing.XXXL))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Light, // Premium thin typography
            textAlign = TextAlign.Center,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(LongboiSpacing.L))

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = LongboiSpacing.XL)
        )
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(width = if (isSelected) 24.dp else 8.dp, height = 8.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Color.White
                    } else {
                        Color.White.copy(alpha = 0.3f)
                    },
                ),
    )
}

enum class OnboardingPages(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
) {
    WELCOME(
        titleRes = R.string.onboarding_welcome_title,
        descriptionRes = R.string.onboarding_welcome_description,
        icon = Icons.Default.Home,
    ),
    FAVORITES(
        titleRes = R.string.onboarding_favorites_title,
        descriptionRes = R.string.onboarding_favorites_description,
        icon = Icons.Default.Search,
    ),
    NOTIFICATIONS(
        titleRes = R.string.onboarding_notifications_title,
        descriptionRes = R.string.onboarding_notifications_description,
        icon = Icons.Default.Notifications,
    ),
    CUSTOMIZE(
        titleRes = R.string.onboarding_customize_title,
        descriptionRes = R.string.onboarding_customize_description,
        icon = Icons.Default.Tune,
    ),
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    LongboiLauncherTheme {
        OnboardingContent(
            state = OnboardingState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingLastPagePreview() {
    LongboiLauncherTheme {
        OnboardingContent(
            state = OnboardingState(currentPage = 3, isLastPage = true),
            onEvent = {},
        )
    }
}
