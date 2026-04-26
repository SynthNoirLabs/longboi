package com.longboilauncher.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longboilauncher.app.core.settings.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val isLastPage: Boolean = false,
)

sealed class OnboardingEvent {
    data object NextPage : OnboardingEvent()

    data object PreviousPage : OnboardingEvent()

    data class GoToPage(
        val page: Int,
    ) : OnboardingEvent()

    data object CompleteOnboarding : OnboardingEvent()

    data object SkipOnboarding : OnboardingEvent()

    data object RequestDefaultLauncher : OnboardingEvent()
}

sealed class OnboardingEffect {
    data object NavigateToHome : OnboardingEffect()

    data object RequestDefaultLauncher : OnboardingEffect()
}

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingState())
        val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

        private val _effects = MutableSharedFlow<OnboardingEffect>()
        val effects: SharedFlow<OnboardingEffect> = _effects.asSharedFlow()

        fun onEvent(event: OnboardingEvent) {
            when (event) {
                is OnboardingEvent.NextPage -> {
                    _uiState.update { state ->
                        val nextPage = (state.currentPage + 1).coerceAtMost(state.totalPages - 1)
                        state.copy(
                            currentPage = nextPage,
                            isLastPage = nextPage == state.totalPages - 1,
                        )
                    }
                }

                is OnboardingEvent.PreviousPage -> {
                    _uiState.update { state ->
                        state.copy(
                            currentPage = (state.currentPage - 1).coerceAtLeast(0),
                            isLastPage = false,
                        )
                    }
                }

                is OnboardingEvent.GoToPage -> {
                    _uiState.update { state ->
                        state.copy(
                            currentPage = event.page.coerceIn(0, state.totalPages - 1),
                            isLastPage = event.page == state.totalPages - 1,
                        )
                    }
                }

                is OnboardingEvent.CompleteOnboarding -> {
                    viewModelScope.launch {
                        preferencesRepository.setOnboardingCompleted(true)
                        _effects.emit(OnboardingEffect.NavigateToHome)
                    }
                }

                is OnboardingEvent.SkipOnboarding -> {
                    viewModelScope.launch {
                        preferencesRepository.setOnboardingCompleted(true)
                        _effects.emit(OnboardingEffect.NavigateToHome)
                    }
                }

                is OnboardingEvent.RequestDefaultLauncher -> {
                    viewModelScope.launch {
                        _effects.emit(OnboardingEffect.RequestDefaultLauncher)
                    }
                }
            }
        }
    }
