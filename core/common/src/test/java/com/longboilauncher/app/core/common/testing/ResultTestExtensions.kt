package com.longboilauncher.app.core.common.testing

import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.common.result.LongboiError
import com.longboilauncher.app.core.common.result.Result

// Test extensions for [Result] assertions.

/** Assert that result is Success */
fun <T> Result<T>.assertSuccess(): Result.Success<T> {
    assertThat(this).isInstanceOf(Result.Success::class.java)
    return this as Result.Success<T>
}

/** Assert that result is Success with specific data */
fun <T> Result<T>.assertSuccessWithData(expected: T): Result.Success<T> {
    val success = assertSuccess()
    assertThat(success.data).isEqualTo(expected)
    return success
}

/** Assert that result is Error */
fun <T> Result<T>.assertError(): Result.Error<T> {
    assertThat(this).isInstanceOf(Result.Error::class.java)
    return this as Result.Error<T>
}

/** Assert that result is Error of specific type */
inline fun <T, reified E : LongboiError> Result<T>.assertErrorType(): Result.Error<T> {
    val error = assertError()
    assertThat(error.error).isInstanceOf(E::class.java)
    return error
}

/** Assert that result is Loading */
fun <T> Result<T>.assertLoading() {
    assertThat(this).isEqualTo(Result.Loading)
}

/** Assert success and run assertions on data */
inline fun <T> Result<T>.assertSuccessAnd(assertions: (T) -> Unit): Result.Success<T> {
    val success = assertSuccess()
    assertions(success.data)
    return success
}

/** Assert error and run assertions on error */
inline fun <T> Result<T>.assertErrorAnd(assertions: (LongboiError) -> Unit): Result.Error<T> {
    val error = assertError()
    assertions(error.error)
    return error
}
