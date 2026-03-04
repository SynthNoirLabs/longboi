package com.longboilauncher.app.core.common.gestures

/**
 * Available gesture types that can be configured.
 */
enum class GestureType {
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    DOUBLE_TAP,
    LONG_PRESS,
    PINCH_IN,
    PINCH_OUT,
}

/**
 * Actions that can be triggered by gestures.
 */
enum class GestureAction {
    NONE,
    OPEN_APP_DRAWER,
    OPEN_SEARCH,
    OPEN_NOTIFICATIONS,
    OPEN_QUICK_SETTINGS,
    OPEN_SETTINGS,
    TOGGLE_FLASHLIGHT,
    LOCK_SCREEN,
    TAKE_SCREENSHOT,
    LAUNCH_APP,
    GO_HOME,
    SHOW_RECENTS,
    EXPAND_WIDGETS,
}

/**
 * Configuration for a gesture-action mapping.
 */
data class GestureConfig(
    val gesture: GestureType,
    val action: GestureAction,
    val appPackageName: String? = null, // For LAUNCH_APP action
)

/**
 * Default gesture mappings for Longboi Launcher.
 */
object DefaultGestures {
    val defaults =
        mapOf(
            GestureType.SWIPE_UP to GestureAction.OPEN_APP_DRAWER,
            GestureType.SWIPE_DOWN to GestureAction.OPEN_NOTIFICATIONS,
            GestureType.DOUBLE_TAP to GestureAction.LOCK_SCREEN,
            GestureType.LONG_PRESS to GestureAction.OPEN_SEARCH,
            GestureType.PINCH_IN to GestureAction.GO_HOME,
            GestureType.PINCH_OUT to GestureAction.EXPAND_WIDGETS,
        )

    fun getDefaultAction(gesture: GestureType): GestureAction = defaults[gesture] ?: GestureAction.NONE
}
