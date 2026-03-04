package com.longboilauncher.app.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A generic wrapper for representing the result of an operation.
 * Follows the UDF pattern for state management.
 */
sealed interface Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>

    data class Error<T>(
        val error: LongboiError,
        val data: T? = null,
    ) : Result<T>

    data object Loading : Result<Nothing>
}

/**
 * Domain-specific errors for Longboi Launcher.
 */
sealed class LongboiError(
    open val message: String,
    open val cause: Throwable? = null,
) {
    /** Network-related errors */
    data class Network(
        override val message: String = "Network error",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    /** Permission denied errors */
    data class Permission(
        val permission: String,
        override val message: String = "Permission required: $permission",
    ) : LongboiError(message)

    /** App loading/launching errors */
    data class AppLoad(
        val packageName: String,
        override val message: String = "Failed to load app: $packageName",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    /** App not found errors */
    data class AppNotFound(
        val packageName: String,
        override val message: String = "App not found: $packageName",
    ) : LongboiError(message)

    /** Storage/IO errors */
    data class Storage(
        override val message: String = "Storage error",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    /** Settings/preferences errors */
    data class Settings(
        override val message: String = "Settings error",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    /** Icon pack errors */
    data class IconPack(
        val packName: String,
        override val message: String = "Icon pack error: $packName",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    /** Widget errors */
    data class Widget(
        val widgetId: Int? = null,
        override val message: String = "Widget error",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    /** Generic unknown error */
    data class Unknown(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null,
    ) : LongboiError(message, cause)

    companion object {
        fun from(throwable: Throwable): LongboiError =
            when (throwable) {
                is java.net.UnknownHostException,
                is java.net.SocketTimeoutException,
                is java.io.IOException,
                -> Network(cause = throwable)
                is SecurityException ->
                    Permission(
                        permission = "unknown",
                        message = throwable.message ?: "Permission denied",
                    )
                is android.content.pm.PackageManager.NameNotFoundException ->
                    AppNotFound(
                        packageName = throwable.message ?: "unknown",
                    )
                else ->
                    Unknown(
                        message = throwable.message ?: "Unknown error",
                        cause = throwable,
                    )
            }
    }
}

/** Check if Result is successful */
val <T> Result<T>.isSuccess: Boolean
    get() = this is Result.Success

/** Check if Result is an error */
val <T> Result<T>.isError: Boolean
    get() = this is Result.Error

/** Check if Result is loading */
val <T> Result<T>.isLoading: Boolean
    get() = this is Result.Loading

/** Get data or null */
fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> data
        is Result.Loading -> null
    }

/** Get data or default value */
fun <T> Result<T>.getOrDefault(default: T): T = getOrNull() ?: default

/** Get data or throw */
fun <T> Result<T>.getOrThrow(): T =
    when (this) {
        is Result.Success -> data
        is Result.Error -> throw error.cause ?: IllegalStateException(error.message)
        is Result.Loading -> throw IllegalStateException("Result is still loading")
    }

/** Map success data */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error, data?.let(transform))
        is Result.Loading -> Result.Loading
    }

/** Handle error with recovery */
inline fun <T> Result<T>.recover(block: (LongboiError) -> T): Result<T> =
    when (this) {
        is Result.Success -> this
        is Result.Error -> Result.Success(block(error))
        is Result.Loading -> this
    }

/** Fold result into single value */
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onError: (LongboiError) -> R,
    onLoading: () -> R,
): R =
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(error)
        is Result.Loading -> onLoading()
    }

/** Convert Flow to Result Flow with loading and error handling */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(LongboiError.from(it))) }

/** Run a suspending block and wrap in Result */
suspend fun <T> runCatching(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(LongboiError.from(e))
    }
