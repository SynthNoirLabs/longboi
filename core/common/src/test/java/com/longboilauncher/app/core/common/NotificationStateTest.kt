package com.longboilauncher.app.core.common

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationStateTest {
    @After
    fun tearDown() {
        // Reset to empty so tests are isolated
        NotificationState.updateCounts(emptyMap())
    }

    @Test
    fun `initial counts are empty`() =
        runTest {
            val counts = NotificationState.counts.first()
            assertThat(counts).isEmpty()
        }

    @Test
    fun `updateCounts publishes new map`() =
        runTest {
            val expected = mapOf("com.example.app" to 3, "com.other.app" to 1)
            NotificationState.updateCounts(expected)

            val counts = NotificationState.counts.first()
            assertThat(counts).isEqualTo(expected)
        }

    @Test
    fun `updateCounts replaces previous map`() =
        runTest {
            NotificationState.updateCounts(mapOf("com.a" to 5))
            NotificationState.updateCounts(mapOf("com.b" to 2))

            val counts = NotificationState.counts.first()
            assertThat(counts).doesNotContainKey("com.a")
            assertThat(counts).containsEntry("com.b", 2)
        }

    @Test
    fun `updateCounts with empty clears all counts`() =
        runTest {
            NotificationState.updateCounts(mapOf("com.a" to 1))
            NotificationState.updateCounts(emptyMap())

            val counts = NotificationState.counts.first()
            assertThat(counts).isEmpty()
        }
}
