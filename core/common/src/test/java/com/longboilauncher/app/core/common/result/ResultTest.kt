package com.longboilauncher.app.core.common.result

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.common.testing.assertError
import com.longboilauncher.app.core.common.testing.assertErrorType
import com.longboilauncher.app.core.common.testing.assertLoading
import com.longboilauncher.app.core.common.testing.assertSuccessWithData
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResultTest {
    @Test
    fun `Success contains data`() {
        val result: Result<String> = Result.Success("test")

        result.assertSuccessWithData("test")
        assertThat(result.isSuccess).isTrue()
        assertThat(result.isError).isFalse()
        assertThat(result.isLoading).isFalse()
    }

    @Test
    fun `Error contains error and optional data`() {
        val error = LongboiError.Network("Connection failed")
        val result: Result<String> = Result.Error(error, "cached")

        result.assertError()
        assertThat(result.isError).isTrue()
        assertThat(result.getOrNull()).isEqualTo("cached")
    }

    @Test
    fun `Loading state`() {
        val result: Result<String> = Result.Loading

        result.assertLoading()
        assertThat(result.isLoading).isTrue()
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `getOrDefault returns data on success`() {
        val result: Result<String> = Result.Success("test")

        assertThat(result.getOrDefault("default")).isEqualTo("test")
    }

    @Test
    fun `getOrDefault returns default on error`() {
        val result: Result<String> = Result.Error(LongboiError.Unknown())

        assertThat(result.getOrDefault("default")).isEqualTo("default")
    }

    @Test
    fun `map transforms success data`() {
        val result: Result<Int> = Result.Success(5)
        val mapped = result.map { it * 2 }

        mapped.assertSuccessWithData(10)
    }

    @Test
    fun `map preserves error`() {
        val error = LongboiError.Network()
        val result: Result<Int> = Result.Error(error)
        val mapped = result.map { it * 2 }

        mapped.assertError()
    }

    @Test
    fun `recover transforms error to success`() {
        val result: Result<String> = Result.Error(LongboiError.Network())
        val recovered = result.recover { "recovered" }

        recovered.assertSuccessWithData("recovered")
    }

    @Test
    fun `fold handles all cases`() {
        val success: Result<Int> = Result.Success(5)
        val error: Result<Int> = Result.Error(LongboiError.Unknown())
        val loading: Result<Int> = Result.Loading

        assertThat(success.fold({ it * 2 }, { -1 }, { 0 })).isEqualTo(10)
        assertThat(error.fold({ it * 2 }, { -1 }, { 0 })).isEqualTo(-1)
        assertThat(loading.fold({ it * 2 }, { -1 }, { 0 })).isEqualTo(0)
    }

    @Test
    fun `asResult emits Loading then Success`() =
        runTest {
            flowOf("data").asResult().test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                awaitItem().assertSuccessWithData("data")
                awaitComplete()
            }
        }

    @Test
    fun `LongboiError from IOException creates Network error`() {
        val error = LongboiError.from(IOException("timeout"))

        assertThat(error).isInstanceOf(LongboiError.Network::class.java)
    }

    @Test
    fun `LongboiError from SecurityException creates Permission error`() {
        val error = LongboiError.from(SecurityException("denied"))

        assertThat(error).isInstanceOf(LongboiError.Permission::class.java)
    }

    @Test
    fun `LongboiError from unknown exception creates Unknown error`() {
        val error = LongboiError.from(IllegalStateException("weird"))

        assertThat(error).isInstanceOf(LongboiError.Unknown::class.java)
    }

    @Test
    fun `runCatching returns Success on normal execution`() =
        runTest {
            val result =
                com.longboilauncher.app.core.common.result
                    .runCatching { "success" }

            result.assertSuccessWithData("success")
        }

    @Test
    fun `runCatching returns Error on exception`() =
        runTest {
            val result =
                com.longboilauncher.app.core.common.result
                    .runCatching<String> { throw IOException() }

            result.assertErrorType<String, LongboiError.Network>()
        }
}
