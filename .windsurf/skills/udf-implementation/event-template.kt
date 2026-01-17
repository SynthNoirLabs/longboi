package com.longboilauncher.app.feature.FEATURE_NAME

sealed interface FeatureNameEvent {
    data object Load : FeatureNameEvent
    data object Refresh : FeatureNameEvent
    // Add feature-specific events here
}

sealed interface FeatureNameEffect {
    data class ShowSnackbar(val message: String) : FeatureNameEffect
    data object NavigateBack : FeatureNameEffect
    // Add one-time effects here
}
