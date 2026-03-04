package com.longboilauncher.app.core.common.result

/**
 * Represents UI state for screens that load data.
 * Integrates with [Result] for consistent error handling.
 */
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: LongboiError? = null,
) {
    val isSuccess: Boolean get() = data != null && error == null && !isLoading
    val isError: Boolean get() = error != null
    val isEmpty: Boolean get() = data == null && !isLoading && error == null

    companion object {
        fun <T> loading(): UiState<T> = UiState(isLoading = true)

        fun <T> success(data: T): UiState<T> = UiState(data = data)

        fun <T> error(
            error: LongboiError,
            data: T? = null,
        ): UiState<T> = UiState(error = error, data = data)

        fun <T> empty(): UiState<T> = UiState()

        /** Convert a [Result] to [UiState] */
        fun <T> fromResult(result: Result<T>): UiState<T> =
            when (result) {
                is Result.Loading -> loading()
                is Result.Success -> success(result.data)
                is Result.Error -> error(result.error, result.data)
            }
    }
}

/** Extension to convert [Result] to [UiState] */
fun <T> Result<T>.toUiState(): UiState<T> = UiState.fromResult(this)

/** Update UiState with new data while preserving error state */
fun <T> UiState<T>.withData(data: T): UiState<T> = copy(data = data, isLoading = false)

/** Clear error while preserving data */
fun <T> UiState<T>.clearError(): UiState<T> = copy(error = null)

/** Set loading state while preserving data */
fun <T> UiState<T>.loading(): UiState<T> = copy(isLoading = true, error = null)
