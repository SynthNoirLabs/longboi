package com.longboilauncher.app.core.designsystem.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens for Longboi Launcher.
 * Redesigned for maximum "breathing room" and one-handed usability.
 */
@Stable
object LongboiSpacing {
    val XXS: Dp = 2.dp
    val XS: Dp = 4.dp
    val S: Dp = 8.dp
    val M: Dp = 12.dp
    val L: Dp = 16.dp
    val XL: Dp = 24.dp
    val XXL: Dp = 32.dp
    val XXXL: Dp = 48.dp
    val XXXXL: Dp = 64.dp
    val JUMBO: Dp = 80.dp

    // Opinionated layout tokens
    val ListVerticalPadding: Dp = 20.dp // Generous space between app items
    val ScreenEdgePadding: Dp = 24.dp
    val ClockTopMargin: Dp = 80.dp
}

/** Corner radius tokens. */
@Stable
object LongboiCorners {
    val None: Dp = 0.dp
    val XS: Dp = 4.dp
    val S: Dp = 8.dp
    val M: Dp = 12.dp
    val L: Dp = 16.dp
    val XL: Dp = 24.dp
    val XXL: Dp = 32.dp
    val Full: Dp = 1000.dp // For circles
}
