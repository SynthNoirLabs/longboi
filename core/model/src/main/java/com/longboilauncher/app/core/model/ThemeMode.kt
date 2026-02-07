package com.longboilauncher.app.core.model

enum class ThemeMode(val value: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light"),
    ;

    companion object {
        fun fromValue(value: String): ThemeMode =
            entries.find { it.value == value } ?: SYSTEM
    }
}
