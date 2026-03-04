package com.longboilauncher.app.core.model

/**
 * Defines when the private space should automatically re-lock itself.
 * Stored as a string key in DataStore (see [key]).
 */
enum class PrivateSpaceAutoLockPolicy(
    val key: String,
) {
    /** Lock immediately when the user leaves the private space. */
    IMMEDIATELY("immediately"),

    /** Lock when the device screen turns off. */
    SCREEN_OFF("screen_off"),

    /** Lock after a configurable idle timeout. */
    AFTER_TIMEOUT("after_timeout"),
    ;

    companion object {
        fun fromKey(key: String): PrivateSpaceAutoLockPolicy = entries.firstOrNull { it.key == key } ?: SCREEN_OFF
    }
}
