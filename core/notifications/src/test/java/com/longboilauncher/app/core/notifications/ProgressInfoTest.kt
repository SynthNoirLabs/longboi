package com.longboilauncher.app.core.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProgressInfoTest {
    @Test
    fun `fraction returns ratio of current to max`() {
        assertThat(ProgressInfo(current = 25, max = 100).fraction).isEqualTo(0.25f)
        assertThat(ProgressInfo(current = 1, max = 4).fraction).isEqualTo(0.25f)
    }

    @Test
    fun `fraction is zero when max is zero`() {
        assertThat(ProgressInfo(current = 5, max = 0).fraction).isEqualTo(0f)
    }

    @Test
    fun `fraction is zero for indeterminate progress`() {
        val progress = ProgressInfo(current = 50, max = 100, isIndeterminate = true)

        assertThat(progress.fraction).isEqualTo(0f)
    }

    @Test
    fun `percentage truncates to int`() {
        assertThat(ProgressInfo(current = 33, max = 100).percentage).isEqualTo(33)
        assertThat(ProgressInfo(current = 1, max = 3).percentage).isEqualTo(33)
    }

    @Test
    fun `isComplete is true when current reaches max`() {
        assertThat(ProgressInfo(current = 100, max = 100).isComplete).isTrue()
        assertThat(ProgressInfo(current = 150, max = 100).isComplete).isTrue()
    }

    @Test
    fun `isComplete is false when current is below max`() {
        assertThat(ProgressInfo(current = 50, max = 100).isComplete).isFalse()
    }

    @Test
    fun `isComplete is false for indeterminate progress`() {
        val progress = ProgressInfo(current = 100, max = 100, isIndeterminate = true)

        assertThat(progress.isComplete).isFalse()
    }
}
