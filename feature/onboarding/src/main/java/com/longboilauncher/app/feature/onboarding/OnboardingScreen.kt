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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(LongboiSpacing.XL),
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { onEvent(OnboardingEvent.SkipOnboarding) }) {
                Text(stringResource(R.string.onboarding_skip))
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
                    .padding(vertical = LongboiSpacing.L),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(state.totalPages) { index ->
                PageIndicator(
                    isSelected = index == state.currentPage,
                    modifier = Modifier.padding(horizontal = 4.dp),
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
                    Text(stringResource(R.string.onboarding_back))
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (state.isLastPage) {
                        onEvent(OnboardingEvent.CompleteOnboarding)
                    } else {
                        onEvent(OnboardingEvent.NextPage)
                    }
                },
            ) {
                Text(
                    if (state.isLastPage) {
                        stringResource(R.string.onboarding_get_started)
                    } else {
                        stringResource(R.string.onboarding_next)
                    },
                )
                Spacer(modifier = Modifier.width(LongboiSpacing.S))
                Icon(
                    imageVector =
                        if (state.isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
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
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(LongboiSpacing.XXL))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(LongboiSpacing.M))

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .size(if (isSelected) 10.dp else 8.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
