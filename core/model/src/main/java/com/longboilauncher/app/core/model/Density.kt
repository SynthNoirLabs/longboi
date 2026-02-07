package com.longboilauncher.app.core.model

enum class Density(val value: String) {
    DEFAULT("default"),
    COMPACT("compact"),
    COMFORTABLE("comfortable"),
    ;

    companion object {
        fun fromValue(value: String): Density =
            entries.find { it.value == value } ?: DEFAULT
    }
}
