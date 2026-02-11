package com.longboilauncher.app.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeType(
    val key: String,
) {
    MATERIAL_YOU("material_you"),
    GLASSMORPHISM("glassmorphism"),
    MODERN_MINIMALIST("modern_minimalist"),
    VIBRANT_PLAYFUL("vibrant_playful"),
    SOPHISTICATED_SLEEK("sophisticated_sleek"),
    ;

    companion object {
        fun fromKey(key: String): ThemeType = entries.find { it.key == key } ?: MATERIAL_YOU
    }
}
