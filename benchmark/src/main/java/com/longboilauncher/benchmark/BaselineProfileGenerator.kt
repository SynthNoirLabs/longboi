package com.longboilauncher.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.longboilauncher.app",
        includeInStartupProfile = true
    ) {
        // Core journey for baseline profile
        pressHome()
        startActivityAndWait()

        // TODO: Add more interactions like swiping up to all apps
        // and opening search to optimize those paths too.
    }
}
