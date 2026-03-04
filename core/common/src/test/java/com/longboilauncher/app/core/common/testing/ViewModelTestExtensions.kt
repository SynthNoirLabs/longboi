package com.longboilauncher.app.core.common.testing

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

// Test utilities for ViewModel testing following UDF pattern.

/**
 * Collects state emissions and runs assertions.
 * Uses Turbine for Flow testing.
 */
suspend fun <T> StateFlow<T>.testState(assertions: suspend (T) -> Unit) {
    test {
        assertions(awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}

/**
 * Collects multiple state emissions and runs assertions on each.
 */
suspend fun <T> StateFlow<T>.testStates(
    count: Int,
    assertions: suspend (List<T>) -> Unit,
) {
    test {
        val items = mutableListOf<T>()
        repeat(count) {
            items.add(awaitItem())
        }
        assertions(items)
        cancelAndIgnoreRemainingEvents()
    }
}

/**
 * Assert that a state property matches expected value.
 */
inline fun <T, R> assertStateProperty(
    state: T,
    property: (T) -> R,
    expected: R,
) {
    assertThat(property(state)).isEqualTo(expected)
}

/**
 * Assert that a state property satisfies a condition.
 */
inline fun <T, R> assertStatePropertySatisfies(
    state: T,
    property: (T) -> R,
    condition: (R) -> Boolean,
    message: String = "Property condition not satisfied",
) {
    assertThat(condition(property(state))).isTrue()
}

/**
 * Wrapper for running ViewModel event tests.
 * Ensures proper setup/teardown of test dispatcher.
 */
fun viewModelTest(testBody: suspend TestScope.() -> Unit) =
    runTest {
        testBody()
    }
